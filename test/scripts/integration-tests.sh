#!/usr/bin/env bash
set -e

# Default configuration
SERVER_HOST=${SERVER_HOST:-"localhost"}
SERVER_PORT=${SERVER_PORT:-"8000"}
CONFIG_PATH=${CONFIG_PATH:-"./test/com/moclojer/resources/moclojer.yml"}
LOG_FILE=${LOG_FILE:-"moclojer.log"}

# Function to start the moclojer server
start_server() {
    echo "Starting moclojer server with config: $CONFIG_PATH" >&2
    clojure -M:run -c "$CONFIG_PATH" >"$LOG_FILE" 2>&1 &
    local pid=$!
    echo "Server started with PID: $pid" >&2

    # Give it a moment to start up
    echo "Waiting for moclojer server to start..." >&2
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
        if curl -s -o /dev/null -w "%{http_code}" "http://$SERVER_HOST:$SERVER_PORT/hello/testuser" | grep -q "200"; then
            SERVER_READY=true
            echo "✅ moclojer server is up and running!" >&2
        else
            echo "Waiting for server to be ready... (attempt $((RETRY_COUNT + 1))/$MAX_RETRIES)" >&2
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
        echo "Stopping moclojer server (PID: $pid)..."
        kill $pid || true
    else
        echo "Invalid PID: '$pid'. Trying to kill by process name..."
        pkill -f "clojure -M:run -c $CONFIG_PATH" || true
    fi

    # Print server logs for debugging
    if [ -f "$LOG_FILE" ]; then
        echo "moclojer server logs:"
        cat "$LOG_FILE"
    fi
}

# Function to run basic endpoint tests
test_basic_endpoints() {
    # Test the /hello/:username endpoint
    echo "Testing /hello/:username endpoint..."
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://$SERVER_HOST:$SERVER_PORT/hello/testuser")
    echo "Status code: $RESPONSE"
    if [ "$RESPONSE" -eq 200 ]; then
        echo "✅ GET /hello/testuser returned 200 OK"
    else
        echo "❌ GET /hello/testuser failed with status $RESPONSE"
        return 1
    fi

    # Test the response body
    echo "Testing response body..."
    BODY=$(curl -s "http://$SERVER_HOST:$SERVER_PORT/hello/testuser")
    EXPECTED_KEY="hello"
    EXPECTED_VALUE="testuser!"
    echo "Received body: $BODY"

    # Extract and compare values ignoring whitespace
    if echo "$BODY" | grep -q "\"$EXPECTED_KEY\":\"$EXPECTED_VALUE\""; then
        echo "✅ Response body contains expected key-value pair"
    else
        echo "❌ Response body does not contain expected key-value pair"
        echo "Expected key: $EXPECTED_KEY"
        echo "Expected value: $EXPECTED_VALUE"
        echo "Received: $BODY"
        return 1
    fi

    return 0
}

# Function to test additional endpoints
test_additional_endpoints() {
    echo "Testing additional endpoints from the configuration..."

    # Test /hello-world endpoint
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://$SERVER_HOST:$SERVER_PORT/hello-world")
    if [ "$RESPONSE" -eq 200 ]; then
        echo "✅ GET /hello-world returned 200 OK"
    else
        echo "❌ GET /hello-world failed with status $RESPONSE"
        return 1
    fi

    # Test /v1/hello endpoint
    RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "http://$SERVER_HOST:$SERVER_PORT/v1/hello")
    if [ "$RESPONSE" -eq 200 ]; then
        echo "✅ GET /v1/hello returned 200 OK"
    else
        echo "❌ GET /v1/hello failed with status $RESPONSE"
        return 1
    fi

    return 0
}

# Main execution
main() {
    # Start the server and capture only the PID
    local server_pid
    server_pid=$(start_server)

    # Validate PID is a number
    if [[ ! "$server_pid" =~ ^[0-9]+$ ]]; then
        echo "❌ Failed to get valid server PID: '$server_pid'"
        cat "$LOG_FILE"
        exit 1
    fi

    echo "Captured server PID: $server_pid"

    # Ensure server is stopped on exit
    trap "stop_server $server_pid" EXIT

    # Run tests
    test_basic_endpoints
    test_additional_endpoints

    echo "✅ All integration tests passed successfully!"
}

# Run the main function
main
