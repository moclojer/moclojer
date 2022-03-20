FROM docker.io/clojure:openjdk-19-tools-deps-slim-bullseye
WORKDIR /app
COPY . .
RUN clojure -A:dev -M --report stderr -m moclojer.build
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
EXPOSE ${PORT}
VOLUME ${CONFIG}
CMD ["java", "-jar", "target/moclojer.jar"]
