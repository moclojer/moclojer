FROM docker.io/clojure:openjdk-19-tools-deps-slim-bullseye AS build
WORKDIR /app
COPY deps.edn .
RUN clojure -P && clojure -A:dev -P
COPY . .
RUN clojure -A:dev -M --report stderr -m moclojer.build

FROM docker.io/openjdk:19-jdk-alpine
WORKDIR /app
COPY --from=build /app/target/moclojer.jar .
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
EXPOSE ${PORT}
VOLUME ${CONFIG}
CMD ["java", "-jar", "moclojer.jar"]
