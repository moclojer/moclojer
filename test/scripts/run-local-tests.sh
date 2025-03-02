#!/usr/bin/env bash
set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}🚀 Running Moclojer Integration Tests Locally${NC}"
echo -e "${YELLOW}Setting up environment...${NC}"

# Ensure the script is executable
chmod +x ./test/scripts/integration-tests.sh

# Run the integration tests
echo -e "${GREEN}Starting integration tests...${NC}"
./test/scripts/integration-tests.sh

# Check the exit status
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Integration tests completed successfully!${NC}"
else
    echo -e "${YELLOW}❌ Integration tests failed. Please check the output above for details.${NC}"
    exit 1
fi
