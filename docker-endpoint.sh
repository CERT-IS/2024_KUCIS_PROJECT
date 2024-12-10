#!/bin/bash

echo '[SYSTEM] : Start SIEMple Application'
java -jar /app/siem-app.jar &

echo '[SYSTEM] : Start sLM machine'
python3 /app/ai/ai.py &

wait $(jobs -p)
