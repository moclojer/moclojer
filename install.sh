#!/usr/bin/env bash

set -euo pipefail

BIN=moclojer
REPO='moclojer/moclojer'
INSTALL_DIR="/usr/local/bin"
DOWNLOAD_DIR=''
VERSION=''

# Args
print_help() {
    echo "Installs latest (or specific) version of ${BIN}. Installation directory defaults to /usr/local/bin."
    echo -e
    echo "Usage:"
    echo "install [--dir <dir>] [--download-dir <download-dir>] [--version <version>]"
    echo -e
    echo "Defaults:"
    echo " * Installation directory: ${INSTALL_DIR}"
    echo " * Download directory: temporary"
    echo " * Version: <Latest release on github>"
    exit 1
}

while [[ $# -gt 0 ]]
do
    key="$1"
    case "$key" in
        --dir)
            INSTALL_DIR="$2"
            shift
            shift
            ;;
        --download-dir)
            DOWNLOAD_DIr="$2"
            shift
            shift
            ;;
        --version)
            VERSION="$2"
            shift
            shift
            ;;
        *)  # unknown option
            print_help
            shift
            ;;
    esac
done

if [[ -z "$DOWNLOAD_DIR" ]]; then
    DOWNLOAD_DIR="$(mktemp -d)"
    trap 'rm -rf "$DOWNLOAD_DIR"' EXIT
fi

if [[ "$VERSION" == "" ]]; then
    VERSION=$(curl -s https://api.github.com/repos/${REPO}/releases/latest | grep -o "\"tag_name\": \".*\"" | cut -d'"' -f4)
fi

# Local enviroment
case "$(uname -s)" in
    Linux*)  PLATFORM=Linux;;
    Darwin*) PLATFORM=macOS;;
esac

# Build file name and download link
FILENAME=${BIN}_${PLATFORM}

DOWNLOAD_URL="https://github.com/${REPO}/releases/download/${VERSION}/${FILENAME}"

# Download
# Running this part in a subshell so when it finishes we go back to the previous directory
mkdir -p "$DOWNLOAD_DIR" && (
    cd "$DOWNLOAD_DIR"
    echo -e "Downloading $DOWNLOAD_URL to $DOWNLOAD_DIR"
    curl -o "$FILENAME" -sL "$DOWNLOAD_URL"
)

# Install
if [[ "$DOWNLOAD_DIR" != "$INSTALL_DIR" ]]
then
    mkdir -p "$INSTALL_DIR"
    if [ -f "$INSTALL_DIR/$BIN" ]; then
        echo "Moving $INSTALL_DIR/$BIN to $INSTALL_DIR/$BIN.old"
        mv -f "$INSTALL_DIR/$BIN" "$INSTALL_DIR/$BIN.old"
    fi
    mv -f "$DOWNLOAD_DIR/$FILENAME" "$INSTALL_DIR/$BIN"
    chmod +x "$INSTALL_DIR/$BIN"
fi

echo "Successfully installed $BIN in $INSTALL_DIR"
