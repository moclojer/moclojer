#!/usr/bin/env bash
set -e

# Default configuration
SERVER_HOST=${SERVER_HOST:-"localhost"}
SERVER_PORT=${SERVER_PORT:-"8000"}
CONFIG_PATH=${CONFIG_PATH:-"./test/com/moclojer/resources/moclojer.yml"}
LOG_FILE=${LOG_FILE:-"moclojer.log"}

# Function to start the moclojer server
start_server() {
    echo "Starting moclojer server with config: $CONFIG_PATH"
    clojure -M:run -c "$CONFIG_PATH" >"$LOG_FILE" 2>&1 &
    SERVER_PID=$!
    echo "Server started with PID: $SERVER_PID"

    # Give it a moment to start up
    echo "Waiting for moclojer server to start..."
    sleep 10

    # Check if server is running
    if ! ps -p $SERVER_PID >/dev/null; then
        echo "❌ moclojer server failed to start. Check logs:"
        cat "$LOG_FILE"
        exit 1
    fi

    # Check if server is responding
    MAX_RETRIES=5
    RETRY_COUNT=0
    SERVER_READY=false

    while [ $RETRY_COUNT -lt $MAX_RETRIES ] && [ "$SERVER_READY" = false ]; do
        if curl -s -o /dev/null -w "%{http_code}" "http://$SERVER_HOST:$SERVER_PORT/hello/testuser" | grep -q "200"; then
            SERVER_READY=true
            echo "✅ moclojer server is up and running!"
        else
            echo "Waiting for server to be ready... (attempt $((RETRY_COUNT + 1))/$MAX_RETRIES)"
            RETRY_COUNT=$((RETRY_COUNT + 1))
            sleep 5
        fi
    done

    if [ "$SERVER_READY" = false ]; then
        echo "❌ moclojer server did not respond in time. Check logs:"
        cat "$LOG_FILE"
        exit 1
    fi

    echo $SERVER_PID
}

# Function to stop the server
stop_server() {
    local pid=$1
    if [ -n "$pid" ]; then
        echo "Stopping moclojer server (PID: $pid)..."
        kill $pid || true
    else
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
    # Start the server
    SERVER_PID=$(start_server)

    # Ensure server is stopped on exit
    trap "stop_server $SERVER_PID" EXIT

    # Run tests
    test_basic_endpoints
    test_additional_endpoints

    echo "✅ All integration tests passed successfully!"
}

# Run the main function
main
