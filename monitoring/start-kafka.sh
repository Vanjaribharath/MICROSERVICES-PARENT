#!/bin/bash
# Day 23 — Start Kafka standalone (no Docker)
# Download Confluent Platform: https://www.confluent.io/installation/
# OR Apache Kafka: https://kafka.apache.org/downloads
# Extract to C:\kafka (Windows) or /opt/kafka (Linux/Mac)

KAFKA_HOME="${KAFKA_HOME:-/opt/kafka}"

if [ ! -d "$KAFKA_HOME" ]; then
  echo "ERROR: KAFKA_HOME not found at $KAFKA_HOME"
  echo "Download from: https://kafka.apache.org/downloads"
  echo "Extract to /opt/kafka and set KAFKA_HOME=/opt/kafka"
  exit 1
fi

# Windows: use .bat files instead of .sh
echo "Step 1: Starting Zookeeper..."
$KAFKA_HOME/bin/zookeeper-server-start.sh \
  $KAFKA_HOME/config/zookeeper.properties &
ZK_PID=$!
echo "Zookeeper PID=$ZK_PID"
sleep 5

echo "Step 2: Starting Kafka broker..."
$KAFKA_HOME/bin/kafka-server-start.sh \
  $KAFKA_HOME/config/server.properties &
KAFKA_PID=$!
echo "Kafka PID=$KAFKA_PID"
sleep 5

echo "Step 3: Creating topics..."
bash "$(dirname "$0")/../k8s/kafka/kafka-topics.sh"

echo ""
echo "Kafka ready on localhost:9092"
echo "Zookeeper on localhost:2181"
echo ""
echo "To monitor topics:"
echo "$KAFKA_HOME/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic product-events --from-beginning"
