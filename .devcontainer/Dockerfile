FROM docker.io/clojure:openjdk-19-tools-deps-bullseye

COPY bashrc /root/.bashrc

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get -yq install --no-install-recommends \
    apt-utils git procps lsb-release netcat sudo apt-utils && \
    apt-get autoremove -y && \
    apt-get clean -y && \
    rm -rf /var/lib/apt/lists/*

# Allow for a consistant java home location for settings - image is changing over time
RUN if [ ! -d "/docker-java-home" ]; then ln -s "${JAVA_HOME}" /docker-java-home; fi

# install joker
ENV JOKER_VERSION=1.1.0
RUN wget https://github.com/candid82/joker/releases/download/v${JOKER_VERSION}/joker-${JOKER_VERSION}-linux-amd64.zip && \
    unzip joker-${JOKER_VERSION}-linux-amd64.zip && \
    rm joker-${JOKER_VERSION}-linux-amd64.zip && \
    chmod a+x joker && \
    mv joker /usr/bin

# A Language Server for Clojure(script). Taking a Cursive-like approach of statically analyzing code
RUN curl -O https://raw.githubusercontent.com/clojure-lsp/clojure-lsp/master/install && \
    chmod a+x ./install && \
    sudo ./install && \
    rm ./install

# Set the default shell to bash rather than sh
ENV SHELL /bin/bash

# moclojer initial build: download all packages
RUN clj -A:dev -M
