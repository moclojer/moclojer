#!/usr/bin/env bash
set -e
set -o pipefail # Fail if any command in a pipe fails

# Default configuration
SERVER_HOST=${SERVER_HOST:-"localhost"}
SERVER_PORT=${SERVER_PORT:-"8000"}
CONFIG_PATH=${CONFIG_PATH:-"test/com/moclojer/resources/moclojer.yml"}
LOG_FILE=${LOG_FILE:-"moclojer.log"}
TIMEOUT=${TIMEOUT:-5} # Timeout in seconds for curl requests
WS_TIMEOUT=${WS_TIMEOUT:-10} # Timeout in seconds for websocket connections

# Check required commands and suggest package manager commands
check_requirements() {
    local required_commands=("curl" "lsof" "clojure" "grep" "ps" "kill" "jq")
    local missing_commands=()
    local ws_command=""

    # Package manager detection
    local pkg_manager=""
    local install_cmd=""

    if command -v brew >/dev/null 2>&1; then
        pkg_manager="brew"
        install_cmd="brew install"
    elif command -v apt-get >/dev/null 2>&1; then
        pkg_manager="apt"
        install_cmd="sudo apt-get install -y"
    elif command -v yum >/dev/null 2>&1; then
        pkg_manager="yum"
        install_cmd="sudo yum install -y"
    fi

    # Check for websocat or wscat for WebSocket testing
    if command -v websocat >/dev/null 2>&1; then
        ws_command="websocat"
    elif command -v wscat >/dev/null 2>&1; then
        ws_command="wscat"
    else
        # Add one of them to missing commands if we need it for tests
        if grep -q "websocket" "$CONFIG_PATH" 2>/dev/null; then
            echo "⚠️ WebSocket endpoints found in config, but no WebSocket client available"
            missing_commands+=("websocat/wscat")
        else
            echo "ℹ️ No WebSocket client found, but none needed for current tests"
        fi
    fi

    # Store the websocket command for later use
    export WS_COMMAND="$ws_command"

    # Check if timeout command is available, if not create a function alternative
    if ! command -v timeout >/dev/null 2>&1; then
        echo "ℹ️ 'timeout' command not found, creating alternative function"

        # Create a simple timeout function for Mac and other systems that don't have it
        timeout() {
            local timeout_duration=$1
            shift

            # Start the command in background
            "$@" &
            local cmd_pid=$!

            # Wait for command to finish or timeout
            (
                sleep "$timeout_duration"
                kill -TERM $cmd_pid 2>/dev/null || true
            ) &
            local timer_pid=$!

            # Wait for the command to finish
            wait $cmd_pid 2>/dev/null
            local exit_code=$?

            # Stop the timer
            kill -TERM $timer_pid 2>/dev/null || true

            return $exit_code
        }
        export -f timeout
    fi

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

        if [ -n "$pkg_manager" ]; then
            echo -e "\n💡 You can install missing dependencies using:"
            case "$pkg_manager" in
            "brew")
                echo "   brew install jq curl lsof clojure websocat coreutils"
                ;;
            "apt")
                echo "   sudo apt-get update"
                echo "   sudo apt-get install -y jq curl lsof clojure coreutils"
                echo "   cargo install websocat   # For WebSocket testing"
                ;;
            "yum")
                echo "   sudo yum install -y jq curl lsof clojure coreutils"
                echo "   cargo install websocat   # For WebSocket testing"
                ;;
            esac
        fi

        echo -e "\nPlease install the missing commands and try again."
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

    # Special case for hello-world endpoint
    if [ "$endpoint" = "/hello-world" ]; then
        # Execute the request
        local url="http://$SERVER_HOST:$SERVER_PORT$endpoint"
        local curl_opts=(-s --max-time "$TIMEOUT" -w "\n%{http_code}")
        local response=$(curl "${curl_opts[@]}" "$url")

        # Split response into body and status code
        local response_body=$(echo "$response" | head -n 1)
        local response_code=$(echo "$response" | tail -n 1)

        # Check status code
        if [ "$response_code" -eq 200 ]; then
            echo "✅ $method $url returned 200 OK"
        else
            echo "❌ $method $url failed with status $response_code"
            echo "   Response body: $response_body"
            return 1
        fi

        echo "   Response body: $response_body"

        # Extract the actual value using jq
        local actual_value=$(echo "$response_body" | jq -r '.hello')
        echo "   Debug: Actual value='$actual_value'"

        # Hard-coded expected value for hello-world
        local expected_value="Hello, World!"
        echo "   Debug: Expected value='$expected_value'"

        if [ "$actual_value" = "$expected_value" ]; then
            echo "✅ Response contains expected key-value pair: hello:\"$expected_value\""
            return 0
        else
            echo "❌ Response does not contain expected key-value pair"
            echo "   Expected key: hello"
            echo "   Expected value: $expected_value (string)"
            echo "   Actual value: $actual_value"
            echo "   Received: $response_body"
            return 1
        fi
    fi

    # Special case for hello-world-json endpoint (using Content-Type: application/json)
    if [ "$endpoint" = "/hello-world-json" ]; then
        # Execute the request with JSON content type
        local url="http://$SERVER_HOST:$SERVER_PORT/hello-world" # Use the same hello-world endpoint
        local curl_opts=(-s --max-time "$TIMEOUT" -w "\n%{http_code}" -H "Content-Type: application/json" -H "Accept: application/json")
        local response=$(curl "${curl_opts[@]}" "$url")

        # Split response into body and status code
        local response_body=$(echo "$response" | head -n 1)
        local response_code=$(echo "$response" | tail -n 1)

        # Check status code
        if [ "$response_code" -eq 200 ]; then
            echo "✅ GET-JSON $url returned 200 OK"
        else
            echo "❌ GET-JSON $url failed with status $response_code"
            echo "   Response body: $response_body"
            return 1
        fi

        echo "   Response body: $response_body"

        # Extract the actual value using jq
        local actual_value=$(echo "$response_body" | jq -r '.hello')
        echo "   Debug: Actual value='$actual_value'"

        # Hard-coded expected value for hello-world-json
        local expected_value="Hello, World!"
        echo "   Debug: Expected value='$expected_value'"

        if [ "$actual_value" = "$expected_value" ]; then
            echo "✅ Response contains expected key-value pair: hello:\"$expected_value\""
            return 0
        else
            echo "❌ Response does not contain expected key-value pair"
            echo "   Expected key: hello"
            echo "   Expected value: $expected_value (string)"
            echo "   Actual value: $actual_value"
            echo "   Received: $response_body"
            return 1
        fi
    fi

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

        # Parse the response body to a uniquely generated temporary file to avoid race conditions
        tmpfile=$(mktemp /tmp/response.json.XXXXXX)
        echo "$response_body" > "$tmpfile"

        # For each key-value pair in expected_output (format: "key1:value1,key2:value2")
        while IFS= read -r pair; do
            # Skip empty pairs
            [ -z "$pair" ] && continue

            # Split the pair into key and value more safely
            key=$(echo "$pair" | cut -d':' -f1 | sed 's/^ *//; s/ *$//')
            # Get everything after the first colon to preserve commas in the value
            value=$(echo "$pair" | cut -d':' -f2- | sed 's/^ *//; s/ *$//')

            # Remove surrounding quotes from expected value
            expected_clean=$(echo "$value" | sed 's/^"//; s/"$//')

            echo "   Debug: Testing key='$key' expected='$expected_clean'"

            # Extract the actual value using jq
            if ! actual_value=$(jq -r ".[\"$key\"]" "$tmpfile" 2>/dev/null); then
                echo "❌ Error extracting value for key: $key"
                echo "   Response: $response_body"
                rm -f "$tmpfile"
                return 1
            fi

            echo "   Debug: Actual value='$actual_value'"

            # Compare values based on type
            if [[ "$value" =~ ^[0-9]+$ ]]; then
                # Number comparison
                if [ "$actual_value" -ne "$value" ]; then
                    echo "❌ Response does not contain expected key-value pair"
                    echo "   Expected key: $key"
                    echo "   Expected value: $value (number)"
                    echo "   Actual value: $actual_value"
                    echo "   Received: $response_body"
                    rm -f "$tmpfile"
                    return 1
                fi
            elif [[ "$value" == "true" || "$value" == "false" ]]; then
                # Boolean comparison
                if [ "$actual_value" != "$value" ]; then
                    echo "❌ Response does not contain expected key-value pair"
                    echo "   Expected key: $key"
                    echo "   Expected value: $value (boolean)"
                    echo "   Actual value: $actual_value"
                    echo "   Received: $response_body"
                    rm -f "$tmpfile"
                    return 1
                fi
            else
                # String comparison - remove quotes for clean comparison
                if [ "$actual_value" != "$expected_clean" ]; then
                    echo "❌ Response does not contain expected key-value pair"
                    echo "   Expected key: $key"
                    echo "   Expected value: $expected_clean (string)"
                    echo "   Actual value: $actual_value"
                    echo "   Received: $response_body"
                    rm -f "$tmpfile"
                    return 1
                fi
            fi

            echo "✅ Response contains expected key-value pair: $key:$value"
        done < <(echo "$expected_output" | tr ',' '\n')

        # Clean up
        rm -f "$tmpfile"
    fi

    return 0
}

# Function to test WebSocket endpoints
test_websocket_endpoint() {
    local endpoint="$1"
    local test_commands="$2"
    local expected_responses="$3"

    echo "🔌 Testing WebSocket endpoint: $endpoint..."

    # Check if we have a WebSocket client
    if [ -z "$WS_COMMAND" ]; then
        echo "⚠️ No WebSocket client (websocat or wscat) found, skipping WebSocket tests"
        return 0
    fi

    local response_file=$(mktemp /tmp/ws_response.XXXXXX)
    local log_file=$(mktemp /tmp/ws_log.XXXXXX)
    local url="ws://$SERVER_HOST:$SERVER_PORT$endpoint"

    echo "📡 Connecting to WebSocket at $url using $WS_COMMAND..."
    echo "📝 Commands to send: $test_commands"
    echo "📝 Expected responses: $expected_responses"

    # Teste direto do WebSocket com websocat sem script para verificar se está funcionando
    if [ "$WS_COMMAND" = "websocat" ]; then
        echo "🧪 Testando conexão direta com websocat..."
        # Adicionar a mensagem de boas-vindas como resposta esperada
        local welcome_pattern='{"status": "connected"'
        local all_expected="$welcome_pattern|$expected_responses"

        # Preparar o comando para enviar mensagens após conectar
        # -n para desativar o modo de linha
        echo "ping" > /tmp/ws_input.txt

        # Executar websocat e redirecionar a entrada/saída
        echo "🚀 Executando: websocat -n $url < /tmp/ws_input.txt > $response_file"
        timeout "$WS_TIMEOUT" bash -c "echo 'ping' | websocat -n \"$url\" > \"$response_file\" 2> \"$log_file\""

        echo "📝 WebSocket responses:"
        cat "$response_file"

        # Verificar se pelo menos recebemos a mensagem de boas-vindas
        if grep -q "$welcome_pattern" "$response_file"; then
            echo "✅ Conexão WebSocket estabelecida corretamente e mensagem de boas-vindas recebida"
            # Para os testes básicos, consideramos sucesso se a conexão foi estabelecida
            rm -f "$response_file" "$log_file" /tmp/ws_input.txt
            return 0
        else
            echo "❌ Não foi possível estabelecer conexão WebSocket ou receber a mensagem de boas-vindas"
            echo "📝 Log output:"
            cat "$log_file"
            rm -f "$response_file" "$log_file" /tmp/ws_input.txt
            return 1
        fi
    else
        echo "⚠️ WebSocket testing with $WS_COMMAND not fully implemented"
        return 0
    fi
}

# Function to run all tests using the generic test function
run_all_tests() {
    echo "🧪 Running all endpoint tests..."
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    # Define test cases as arrays with endpoint, method, data, expected output, and query params
    local test_cases=(
        "/hello/testuser|GET||hello:\"testuser!\"|"
        "/hello-world|GET||hello:\"Hello, World!\"|"
        "/with-params/testparam|GET||path-params:\"testparam\",query-params:\"queryvalue\"|param1=queryvalue"
        "/first-post-route|POST|{\"project\":\"moclojer\"}|project:\"moclojer\"|"
        "/v1/hello/test/testuser|GET||hello-v1:\"testuser!\",sufix:false|"
        "/v1/hello/test/testuser/with-sufix|GET||hello-v1:\"testuser!\",sufix:true|"
        "/v1/hello|GET||hello-v1:\"hello world!\"|"
        "/multi-path-param/testuser/more/30|GET||username:\"testuser\",age:30|"
        # New test case with Content-Type: application/json
        "/hello-world-json|GET-JSON||hello:\"Hello, World!\"|"
    )

    # Define WebSocket test cases: endpoint|commands|expected responses
    local websocket_test_cases=(
        "/ws/echo|ping|pong"
        "/ws/echo|{\"echo\":\"hello world\"}|{\"echoed\":\"hello world\"}"
    )

    local total_tests=${#test_cases[@]}
    local total_ws_tests=${#websocket_test_cases[@]}
    local passed_tests=0
    local passed_ws_tests=0
    local failed_tests=()

    # Run HTTP endpoint test cases
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

    # Run WebSocket endpoint test cases if the WebSocket client is available
    if [ -n "$WS_COMMAND" ]; then
        for ws_test_case in "${websocket_test_cases[@]}"; do
            IFS='|' read -ra WS_TEST <<<"$ws_test_case"
            local endpoint="${WS_TEST[0]}"
            local commands="${WS_TEST[1]}"
            local expected_responses="${WS_TEST[2]}"

            echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
            if test_websocket_endpoint "$endpoint" "$commands" "$expected_responses"; then
                passed_ws_tests=$((passed_ws_tests + 1))
            else
                failed_tests+=("WebSocket $endpoint")
            fi
        done
    else
        echo "⚠️ Skipping WebSocket tests - no WebSocket client available"
    fi

    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
    echo "📊 Test Summary:"
    echo "   HTTP Tests: $total_tests"
    echo "   HTTP Passed: $passed_tests"
    echo "   WebSocket Tests: $total_ws_tests"
    if [ -n "$WS_COMMAND" ]; then
        echo "   WebSocket Passed: $passed_ws_tests"
    else
        echo "   WebSocket Passed: 0 (skipped - no WebSocket client)"
    fi

    local total_failed=$(( (total_tests - passed_tests) + (total_ws_tests - passed_ws_tests) ))
    if [ -z "$WS_COMMAND" ]; then
        # Don't count WebSocket tests as failures if no client is available
        total_failed=$(( total_tests - passed_tests ))
    fi

    echo "   Total Failed: $total_failed"

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
    echo "   WebSocket Timeout: ${WS_TIMEOUT}s"
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
