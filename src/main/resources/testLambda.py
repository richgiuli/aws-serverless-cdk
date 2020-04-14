from __future__ import print_function

import boto3
import json

print('Loading function')


def handler(event, context):
  print("Received event: " + json.dumps(event, indent=2))
  return {
    'statusCode': 200,
    'body': json.dumps({'input': event,
                        'status': 'Lambda is working'})
  }