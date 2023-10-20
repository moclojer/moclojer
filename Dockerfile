FROM docker.io/clojure:temurin-21-tools-deps-alpine AS jar-build
RUN apk add git
WORKDIR /app
COPY . .
RUN clojure -M:dev --report stderr -m moclojer.build

FROM docker.io/clojure:temurin-21-tools-deps-alpine
LABEL org.opencontainers.image.source https://github.com/moclojer/moclojer
WORKDIR /app
COPY --from=jar-build /app/target/moclojer.jar /app/moclojer.jar
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
EXPOSE ${PORT}
VOLUME ${CONFIG}
ENTRYPOINT "java" "-jar" "/app/moclojer.jar"
