#!/bin/bash

while true; do
    bash update.sh && bash launch.sh
    echo "Press Ctrl+C to stop"
    sleep 3
done
