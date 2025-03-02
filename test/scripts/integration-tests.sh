#!/usr/bin/env bash
set -e
set -o pipefail # Fail if any command in a pipe fails

# Default configuration
SERVER_HOST=${SERVER_HOST:-"localhost"}
SERVER_PORT=${SERVER_PORT:-"8000"}
CONFIG_PATH=${CONFIG_PATH:-"./test/com/moclojer/resources/moclojer.yml"}
LOG_FILE=${LOG_FILE:-"moclojer.log"}
TIMEOUT=${TIMEOUT:-5} # Timeout in seconds for curl requests

# Check required commands
check_requirements() {
    local required_commands=("curl" "lsof" "clojure" "grep" "ps" "kill" "jq")
    local missing_commands=()

    for cmd in "${required_commands[@]}"; do
        if ! command -v "$cmd" >/dev/null 2>&1; then
            missing_commands+=("$cmd")
        fi
    done

    if [ ${#missing_commands[@]} -gt 0 ]; then
        echo "❌ Required commands not found:"
        for cmd in "${missing_commands[@]}"; do
            echo "   - $cmd"
        done
        echo "Please install the missing commands and try again."
        exit 1
    fi
}

# Function to cleanup on exit
cleanup() {
    local exit_code=$?
    local pid=$1

    echo "🧹 Cleaning up..."
    stop_server "$pid"

    # Remove log file if tests passed
    if [ $exit_code -eq 0 ] && [ -f "$LOG_FILE" ]; then
        rm "$LOG_FILE"
    fi

    exit $exit_code
}

# Function to start the moclojer server
start_server() {
    echo "🚀 Starting moclojer server with config: $CONFIG_PATH" >&2

    # Check if config file exists
    if [ ! -f "$CONFIG_PATH" ]; then
        echo "❌ Configuration file not found: $CONFIG_PATH" >&2
        exit 1
    fi

    # Check if port is available
    if lsof -i:$SERVER_PORT >/dev/null 2>&1; then
        echo "❌ Port $SERVER_PORT is already in use" >&2
        exit 1
    fi

    clojure -M:run -c "$CONFIG_PATH" >"$LOG_FILE" 2>&1 &
    local pid=$!
    echo "📝 Server started with PID: $pid" >&2

    # Give it a moment to start up
    echo "⏳ Waiting for moclojer server to start..." >&2
    sleep 10

    # Check if server is running
    if ! ps -p $pid >/dev/null; then
        echo "❌ moclojer server failed to start. Check logs:" >&2
        cat "$LOG_FILE" >&2
        exit 1
    fi

    # Check if server is responding
    MAX_RETRIES=5
    RETRY_COUNT=0
    SERVER_READY=false

    while [ $RETRY_COUNT -lt $MAX_RETRIES ] && [ "$SERVER_READY" = false ]; do
        if curl -s -o /dev/null -w "%{http_code}" --max-time $TIMEOUT "http://$SERVER_HOST:$SERVER_PORT/hello/testuser" | grep -q "200"; then
            SERVER_READY=true
            echo "✅ moclojer server is up and running at http://$SERVER_HOST:$SERVER_PORT!" >&2
        else
            echo "⏳ Waiting for server to be ready... (attempt $((RETRY_COUNT + 1))/$MAX_RETRIES)" >&2
            RETRY_COUNT=$((RETRY_COUNT + 1))
            sleep 5
        fi
    done

    if [ "$SERVER_READY" = false ]; then
        echo "❌ moclojer server did not respond in time. Check logs:" >&2
        cat "$LOG_FILE" >&2
        exit 1
    fi

    # Return only the PID, nothing else
    echo "$pid"
}

# Function to stop the server
stop_server() {
    local pid=$1
    if [[ -n "$pid" && "$pid" =~ ^[0-9]+$ ]]; then
        echo "🛑 Stopping moclojer server (PID: $pid)..."
        kill "$pid" 2>/dev/null || true

        # Wait for process to actually stop
        local max_wait=5
        local waited=0
        while ps -p "$pid" >/dev/null 2>&1 && [ $waited -lt $max_wait ]; do
            sleep 1
            waited=$((waited + 1))
        done

        # Force kill if still running
        if ps -p "$pid" >/dev/null 2>&1; then
            echo "⚠️ Server did not stop gracefully, force killing..."
            kill -9 "$pid" 2>/dev/null || true
        fi
    else
        echo "⚠️ Invalid PID: '$pid'. Trying to kill by process name..."
        pkill -f "clojure -M:run -c $CONFIG_PATH" || true
    fi

    # Print server logs for debugging
    if [ -f "$LOG_FILE" ]; then
        echo "📋 moclojer server logs:"
        cat "$LOG_FILE"
    fi
}

# Generic function to test endpoints
test_endpoint() {
    local endpoint="$1"
    local method="${2:-GET}"
    local data="$3"
    local expected_output="$4"
    local query_params="${5:-}"

    echo "🔍 Testing $method $endpoint endpoint..."

    # Build the full URL with query parameters if provided
    local url="http://$SERVER_HOST:$SERVER_PORT$endpoint"
    if [ -n "$query_params" ]; then
        url="$url?$query_params"
    fi

    # Execute the request based on the method
    local response_code
    local response_body
    local curl_opts=(-s --max-time "$TIMEOUT" -w "\n%{http_code}")

    if [ "$method" = "GET" ]; then
        response=$(curl "${curl_opts[@]}" "$url")
    elif [ "$method" = "POST" ]; then
        response=$(curl "${curl_opts[@]}" -X POST -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "PUT" ]; then
        response=$(curl "${curl_opts[@]}" -X PUT -H "Content-Type: application/json" -d "$data" "$url")
    elif [ "$method" = "DELETE" ]; then
        response=$(curl "${curl_opts[@]}" -X DELETE "$url")
    else
        echo "❌ Unsupported method: $method"
        return 1
    fi

    # Split response into body and status code
    response_body=$(echo "$response" | head -n 1)
    response_code=$(echo "$response" | tail -n 1)

    # Check status code
    if [ "$response_code" -eq 200 ]; then
        echo "✅ $method $url returned 200 OK"
    else
        echo "❌ $method $url failed with status $response_code"
        echo "   Response body: $response_body"
        return 1
    fi

    # Check response body if expected output is provided
    if [ -n "$expected_output" ]; then
        echo "   Response body: $response_body"
        # For each key-value pair in expected_output (format: "key1:value1,key2:value2")
        IFS=',' read -ra PAIRS <<<"$expected_output"
        for pair in "${PAIRS[@]}"; do
            # Use different delimiter to handle values with spaces
            key=$(echo "$pair" | cut -d':' -f1 | sed 's/^ *//; s/ *$//')
            value=$(echo "$pair" | cut -d':' -f2- | sed 's/^ *//; s/ *$//')

            # Debug output
            echo "   Debug: Testing key='$key' value='$value'"

            # Try to parse the value as JSON if it looks like a JSON value
            if [[ "$value" =~ ^[0-9]+$ ]] || [[ "$value" == "true" ]] || [[ "$value" == "false" ]]; then
                # For numbers and booleans, use direct comparison
                if ! echo "$response_body" | jq -e --arg k "$key" --argjson v "$value" '. | has($k) and .[$k] == $v' >/dev/null 2>&1; then
                    echo "❌ Response does not contain expected key-value pair"
                    echo "   Expected key: $key"
                    echo "   Expected value: $value"
                    echo "   Received: $response_body"
                    return 1
                fi
            else
                # For strings, handle quoted values
                # Remove surrounding quotes if present
                value=${value#\"}
                value=${value%\"}
                # Debug output
                echo "   Debug: After quote removal value='$value'"
                if ! echo "$response_body" | jq -e --arg k "$key" --arg v "$value" '. | has($k) and .[$k] == $v' >/dev/null 2>&1; then
                    echo "❌ Response does not contain expected key-value pair"
                    echo "   Expected key: $key"
                    echo "   Expected value: $value"
                    echo "   Actual value: $(echo "$response_body" | jq -r --arg k "$key" '.[$k]')"
                    echo "   Received: $response_body"
                    return 1
                fi
            fi
            echo "✅ Response contains expected key-value pair: $key:$value"
        done
    fi

    return 0
}

# Function to run all tests using the generic test function
run_all_tests() {
    echo "🧪 Running all endpoint tests..."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Define test cases as arrays with endpoint, method, data, expected output, and query params
    local test_cases=(
        "/hello/testuser|GET||hello:\"testuser!\"|"
        "/hello-world|GET||hello:Hello, World!|"
        "/with-params/testparam|GET||path-params:\"testparam\",query-params:\"queryvalue\"|param1=queryvalue"
        "/first-post-route|POST|{\"project\":\"moclojer\"}|project:\"moclojer\"|"
        "/v1/hello/test/testuser|GET||hello-v1:\"testuser!\",sufix:false|"
        "/v1/hello/test/testuser/with-sufix|GET||hello-v1:\"testuser!\",sufix:true|"
        "/v1/hello|GET||hello-v1:\"hello world!\"|"
        "/multi-path-param/testuser/more/30|GET||username:\"testuser\",age:30|"
    )

    local total_tests=${#test_cases[@]}
    local passed_tests=0
    local failed_tests=()

    # Run each test case
    for test_case in "${test_cases[@]}"; do
        IFS='|' read -ra TEST <<<"$test_case"
        local endpoint="${TEST[0]}"
        local method="${TEST[1]}"
        local data="${TEST[2]}"
        local expected_output="${TEST[3]}"
        local query_params="${TEST[4]}"

        echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
        if test_endpoint "$endpoint" "$method" "$data" "$expected_output" "$query_params"; then
            passed_tests=$((passed_tests + 1))
        else
            failed_tests+=("$method $endpoint")
        fi
    done

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📊 Test Summary:"
    echo "   Total tests: $total_tests"
    echo "   Passed: $passed_tests"
    echo "   Failed: $((total_tests - passed_tests))"

    if [ ${#failed_tests[@]} -gt 0 ]; then
        echo "❌ Failed tests:"
        for failed in "${failed_tests[@]}"; do
            echo "   - $failed"
        done
        return 1
    fi

    echo "✅ All tests passed successfully!"
    return 0
}

# Main execution
main() {
    echo "🚀 Starting integration tests for moclojer"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📋 Configuration:"
    echo "   Server: $SERVER_HOST:$SERVER_PORT"
    echo "   Config: $CONFIG_PATH"
    echo "   Log file: $LOG_FILE"
    echo "   Timeout: ${TIMEOUT}s"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Check required commands
    check_requirements

    # Start the server and capture only the PID
    local server_pid
    server_pid=$(start_server)

    # Validate PID is a number
    if [[ ! "$server_pid" =~ ^[0-9]+$ ]]; then
        echo "❌ Failed to get valid server PID: '$server_pid'"
        cat "$LOG_FILE"
        exit 1
    fi

    echo "📝 Captured server PID: $server_pid"

    # Ensure server is stopped on exit
    trap "cleanup $server_pid" EXIT INT TERM

    # Run tests using the generic function
    run_all_tests
}

# Run the main function
main
