FROM docker.io/clojure:openjdk-11-tools-deps-slim-buster AS jar
WORKDIR /app
COPY . .
RUN clojure -A:dev -M --report stderr -m moclojer.build && \
    rm /app/moclojer.*
ENV PORT="8000"
ENV HOST="0.0.0.0"
ENV CONFIG="/app/moclojer.yml"
EXPOSE ${PORT}
VOLUME ${CONFIG}
CMD ["java", "-jar", "target/moclojer.jar"]
