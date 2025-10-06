FROM docker.io/clojure:temurin-23-tools-deps-alpine AS jar-build
RUN apk add --no-cache git
WORKDIR /app
COPY . .
RUN clojure -M:dev --report stderr -m com.moclojer.build --uberjar

FROM ghcr.io/graalvm/native-image-community:23.0.2-ol9 AS native-build
WORKDIR /workspace
COPY --from=jar-build /app/target ./target
RUN microdnf install -y zlib-devel && microdnf clean all
RUN native-image \
    @target/native-image-args \
    -jar target/moclojer.jar \
    -cp target/classes:target/moclojer.jar

FROM gcr.io/distroless/base-debian12:nonroot
LABEL org.opencontainers.image.source https://github.com/moclojer/moclojer
WORKDIR /app
COPY --from=native-build /workspace/moclojer /app/moclojer
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
EXPOSE ${PORT}
ENTRYPOINT ["/app/moclojer"]
CMD ["-c", "/app/moclojer.yml"]
