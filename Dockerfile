FROM docker.io/clojure:openjdk-17-tools-deps-alpine AS jar-build
RUN apk add git
WORKDIR /app
COPY . .
RUN clojure -P && clojure -A:dev -P && \
    clojure -A:dev -M --report stderr -m moclojer.build

FROM ghcr.io/graalvm/native-image:ol8-java17 AS native-image-build
COPY --from=jar-build /app /app
WORKDIR /app/target
RUN native-image @native/native-image-args \
        -H:Name=moclojer \
        -H:DashboardDump=report/moclojer \
        -H:EnableURLProtocols=http,https \
        -jar /app/target/moclojer.jar \
        -H:ReflectionConfigurationFiles=/app/META-INF/native-image/reflect-config.json \
        -H:ResourceConfigurationFiles=/app/META-INF/native-image/resource-config.json && \
    chmod +x /app/target/moclojer

FROM container-registry.oracle.com/os/oraclelinux:8-slim
LABEL org.opencontainers.image.source https://github.com/moclojer/moclojer
WORKDIR /app
COPY --from=native-image-build /app/target/moclojer /app/moclojer
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
EXPOSE ${PORT}
VOLUME ${CONFIG}
CMD ["/app/moclojer"]
