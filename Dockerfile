FROM docker.io/clojure:openjdk-17-tools-deps-alpine AS jar-build
RUN apk add git
RUN adduser -D moclojer
USER moclojer
WORKDIR /home/moclojer
COPY deps.edn .
RUN clojure -P && clojure -A:dev -P
COPY . .
RUN clojure -A:dev -M --report stderr -m moclojer.build


FROM ghcr.io/graalvm/native-image:ol8-java17 AS native-image-build
RUN adduser moclojer
USER moclojer
WORKDIR /home/moclojer
COPY --from=jar-build --chown=moclojer /home/moclojer .
RUN cd target/native \
    && ../../scripts/gen-reflect-config.sh \
    && native-image @native-image-args \
                -H:DashboardDump=report/moclojer \
                -jar ../moclojer.jar \
                -H:ReflectionConfigurationFiles=reflect-config.json \
                -H:ResourceConfigurationFiles=resource-config.json

FROM container-registry.oracle.com/os/oraclelinux:8-slim
LABEL org.opencontainers.image.source https://github.com/avelino/moclojer
COPY --from=native-image-build --chown=root /home/moclojer/target/native/moclojer /usr/bin
RUN chmod +x /usr/bin/moclojer && chmod 0755 /usr/bin/moclojer
RUN adduser moclojer
USER moclojer
WORKDIR /home/moclojer

ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/home/moclojer"
EXPOSE ${PORT}
VOLUME ${CONFIG}

CMD ["moclojer"]
