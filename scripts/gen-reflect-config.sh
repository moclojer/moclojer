#!/usr/bin/env sh

echo "[]" >moclojer.yml

java -agentlib:native-image-agent=caller-filter-file=filter.json,config-output-dir=. \
  -jar ../moclojer.jar &

PID=$!

## Wait startup

while ! curl -s localhost:8000 -o /dev/null; do
  echo waiting
  sleep 3
done

curl -s localhost:8000 -D -
kill $PID
