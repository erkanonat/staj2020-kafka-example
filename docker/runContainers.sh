#!/usr/bin/env bash
xhost +si:localuser:$USER
xhost +local:docker
export DISPLAY=$DISPLAY
docker-compose up