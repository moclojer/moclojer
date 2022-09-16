FROM docker.io/clojure:openjdk-19-tools-deps-bullseye

COPY bashrc /root/.bashrc

ENV DEBIAN_FRONTEND=noninteractive
RUN apt-get update && \
    apt-get -yq install --no-install-recommends \
    apt-utils git procps lsb-release netcat && \
	apt-get autoremove -y && apt-get clean -y && \
    rm -rf /var/lib/apt/lists/*
ENV DEBIAN_FRONTEND=dialog

# Allow for a consistant java home location for settings - image is changing over time
RUN if [ ! -d "/docker-java-home" ]; then ln -s "${JAVA_HOME}" /docker-java-home; fi

# install joker
ENV JOKER_VERSION=0.12.4
RUN wget https://github.com/candid82/joker/releases/download/v${JOKER_VERSION}/joker-${JOKER_VERSION}-linux-amd64.zip \
    && unzip joker-${JOKER_VERSION}-linux-amd64.zip \
    && rm joker-${JOKER_VERSION}-linux-amd64.zip \
    && chmod a+x joker && mv joker /usr/bin

# Set the default shell to bash rather than sh
ENV SHELL /bin/bash

# moclojer initial build: download all packages
RUN clj -A:dev -M
