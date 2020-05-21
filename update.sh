#!/bin/bash

echo "Downloading latest PorkBot jar..."
if [ -f porkbot-1.0.0.jar ]; then rm porkbot-1.0.0.jar || exit 1; fi
wget https://jenkins.daporkchop.net/job/DaPorkchop_/job/PorkBot/job/master/lastSuccessfulBuild/artifact/target/porkbot-1.0.0.jar || exit 1
