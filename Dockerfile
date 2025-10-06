FROM docker.io/clojure:temurin-23-tools-deps AS jar-build
RUN apt-get update \
 && apt-get install -y git \
 && rm -rf /var/lib/apt/lists/*
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

ARG VERSION=unknown
ARG VCS_REF=unknown
ARG BUILD_DATE=unknown
ARG REF_NAME=prod

FROM gcr.io/distroless/base-debian12:nonroot
ARG VERSION
ARG VCS_REF
ARG BUILD_DATE
ARG REF_NAME
LABEL org.opencontainers.image.source="https://github.com/moclojer/moclojer" \
      org.opencontainers.image.title="moclojer" \
      org.opencontainers.image.description="Simple and efficient HTTP mock server compiled with GraalVM native-image." \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${VCS_REF}" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.licenses="MIT" \
      org.opencontainers.image.url="https://moclojer.com" \
      org.opencontainers.image.documentation="https://github.com/moclojer/moclojer#readme" \
      org.opencontainers.image.vendor="moclojer" \
      org.opencontainers.image.ref.name="${REF_NAME}"
WORKDIR /app
COPY --from=native-build /workspace/moclojer /app/moclojer
COPY --from=native-build /usr/lib64/libz.so.1 /app/lib/libz.so.1
COPY --from=native-build /usr/lib64/libz.so /app/lib/libz.so
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
ENV LD_LIBRARY_PATH="/app/lib"
ENV MOCLOJER_ENV="prod"
EXPOSE ${PORT}
ENTRYPOINT ["/app/moclojer"]
CMD ["-c", "/app/moclojer.yml"]