#!/bin/bash

java -Dfile.encoding=UTF-8 -Xmx600M -XX:+UseG1GC -XX:+UseStringDeduplication -jar porkbot-1.0.0.jar
