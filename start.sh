#!/bin/bash

# This is the shell startup file for PorkBot.
# Input ./start.sh while in the server directory
# to start the server.

#Change this to "true" to 
#loop PorkBot after restart!

DO_LOOP="true"

###############################
# DO NOT EDIT ANYTHING BELOW! #
###############################

clear

NUKKIT_FILE=""

if [ "$NUKKIT_FILE" == "" ]; then
	if [ -f ./porkbot*.jar ]; then
		NUKKIT_FILE="./porkbot-1.0-SNAPSHOT.jar"
	else
		echo "[ERROR] PorkBot JAR not found!"
		exit 1
	fi
fi

LOOPS=0

while [ "$LOOPS" -eq 0 ] || [ "$DO_LOOP" == "true" ]; do
	if [ "$DO_LOOP" == "true" ]; then
		java -Xmx3500M -jar "$NUKKIT_FILE" $@
	else
		exec java -Xmx3500M -jar "$NUKKIT_FILE" $@
	fi
	echo Press Ctrl+c to stop
	sleep 3
        if ./pull.sh | grep -q 'Already up-to-date.'; then
		clear
./compile.sh
#        	echo "Nothing  changed, starting..."
	else
		./compile.sh
	fi
	((LOOPS++))
done

