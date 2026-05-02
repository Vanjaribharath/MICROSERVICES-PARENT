#!/bin/bash
# Day 23 — Create Kafka topics after broker starts (no Docker)
# Run ONCE after: bin/kafka-server-start.sh config/server.properties

KAFKA_HOME=${KAFKA_HOME:-/opt/kafka}
BOOTSTRAP="localhost:9092"

echo "Creating Kafka topics..."

$KAFKA_HOME/bin/kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP \
  --topic product-events \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

$KAFKA_HOME/bin/kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP \
  --topic saga-events \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists

$KAFKA_HOME/bin/kafka-topics.sh --create \
  --bootstrap-server $BOOTSTRAP \
  --topic error-events \
  --partitions 1 \
  --replication-factor 1 \
  --if-not-exists

echo "Topics created:"
$KAFKA_HOME/bin/kafka-topics.sh --list --bootstrap-server $BOOTSTRAP
