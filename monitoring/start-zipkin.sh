#!/bin/bash
# Day 21 — Start Zipkin standalone (no Docker)
# Download once: wget -O zipkin.jar 'https://search.maven.org/remote_content?g=io.zipkin&a=zipkin-server&v=LATEST&c=exec'

ZIPKIN_JAR="${ZIPKIN_JAR:-./zipkin.jar}"

if [ ! -f "$ZIPKIN_JAR" ]; then
  echo "Downloading Zipkin..."
  curl -sSL https://zipkin.io/quickstart.sh | bash -s
  ZIPKIN_JAR="zipkin.jar"
fi

echo "Starting Zipkin on http://localhost:9411"
STORAGE_TYPE=mem java -jar "$ZIPKIN_JAR" &
echo "Zipkin PID=$!"
echo "Access UI: http://localhost:9411/zipkin"
