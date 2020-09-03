#!/bin/bash
#
# Copyright © 2018-2019 Havelsan.Inc
#


until nmap $POSTGRES_HOST -p $POSTGRES_PORT | grep "$POSTGRES_PORT/tcp open"
do
  echo "Waiting for postgres db to start..."
  sleep 10
done

until nmap $KAFKA_HOST -p $KAFKA_PORT | grep "$KAFKA_PORT/tcp open"
do
  echo "Waiting for kafka to start..."
  sleep 10
done

echo "Starting 'KKM Connector'..."
sleep 5
java -agentlib:jdwp=transport=dt_socket,server=y,address=4040,suspend=n -jar /kkm-connector.jar

