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

if git pull | grep -q 'Already up-to-date.'; then
    clear
    echo "Nothing  changed, starting..."
else
    ./compile.sh
    clear
    echo "Compiled, starting..." 
fi

sleep 2

clear 

while [ "$DO_LOOP" == "true" ]; do
	mvn exec:java -Dexec.args="-Dfile.encoding=UTF-8 -XX:+UseG1GC -Xmx600M" -Dexec.mainClass="net.daporkchop.porkbot.PorkBot" -Dexec.classpathScope=runtime
	echo "Press Ctrl+c to stop" 
	sleep 3
    if git pull | grep -q 'Already up-to-date.'; then
		clear
                echo "Nothing  changed, starting..."
	else
		./compile.sh
		sleep 2
		clear
		echo "Compiled, starting..." 
	fi
done
