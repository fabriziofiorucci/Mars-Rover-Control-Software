#!/bin/bash

AMOUNT="10 20 30 40 50 60 70 80 90 100 90 80 70 50 40 30 20 10 0"

#
# Forward drive at increasing speed
#
for A in $AMOUNT
do
	
curl -o /dev/null -s -X POST \
  http://192.168.1.7:8080/1.0/drive \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic cmVzdHVzZXJuYW1lOnJlc3RwYXNzd29yZA==' \
  -H 'Content-Type: application/json' \
  -d '{
	"drive": {
		"mode": "forward",
		"speed": "'$A'"
	}
}'

sleep 0.1
done

#
# Right steering at increasing angle
#
for A in $AMOUNT
do
	
curl -o /dev/null -s -X POST \
  http://192.168.1.7:8080/1.0/drive \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic cmVzdHVzZXJuYW1lOnJlc3RwYXNzd29yZA==' \
  -H 'Content-Type: application/json' \
  -d '{
	"steering": {
		"mode": "right",
		"amount": "'$A'"
	}
}'

sleep 0.1
done

#
# Stop wheels and reset steering
#
curl -o /dev/null -s -X POST \
  http://192.168.1.7:8080/1.0/drive \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic cmVzdHVzZXJuYW1lOnJlc3RwYXNzd29yZA==' \
  -H 'Content-Type: application/json' \
  -d '{
	"drive": {
		"mode": "stop",
		"speed": "0"
	},
	"steering": {
		"mode": "none",
		"amount": "0"
	}
}'