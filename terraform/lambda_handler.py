import json
import urllib.request
import logging
import base64

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def lambda_handler(event, context):
    logger.info(f"Received event: {json.dumps(event)}")

    headers = event.get('headers', {})
    method = event['httpMethod']

    path = event['path']
    query_params = event.get('queryStringParameters', {})
    alb_base_url = "http://web-instance-alb-1145570667.ap-northeast-2.elb.amazonaws.com"

    if query_params:
        query_string = '&'.join([f"{key}={value}" for key, value in query_params.items()])
        alb_url = f"{alb_base_url}{path}?{query_string}"
    else:
        alb_url = f"{alb_base_url}{path}"

    logger.info("alb_url : "+alb_url)

    body = event.get('body','')
    if body is not None and body != "":
        if event.get('isBase64Encoded'):
            body = base64.b64decode(body)
        else:
            body = body.encode('utf-8')

    req = urllib.request.Request(alb_url, data=body if body else None, headers=headers, method=method)

    try:
        with urllib.request.urlopen(req) as response:
            response_body = response.read()
            logger.info(f"Response from ALB : {response.status} - {len(response_body)} bytes")
            content_type = response.getheader('Content-Type')

            if 'image' in content_type:
                logger.info(f"Image size before encoding: {len(response_body)} bytes")
                base64_image = f"data:{content_type};base64,{base64.b64encode(response_body).decode('utf-8')}"  # 데이터 스킴 추가

                logger.info(f"Base64 encoded image size: {len(base64_image)} characters")
                return {
                    'statusCode': response.status,
                    'headers': {
                        'Content-Type': content_type,
                        'Access-Control-Allow-Origin': '*',
                        'Access-Control-Allow-Methods': 'GET, OPTIONS',
                        'Content-Length': len(base64_image)
                    },
                    'isBase64Encoded': True,
                    'body': base64_image
                }
            else:
                logger.info("Data received: "+response_body.decode('utf-8'))
                return {
                    'statusCode': response.status,
                    'body': response_body.decode('utf-8'),
                    'headers': dict(response.getheaders())
                }
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        logger.error(f"HTTPError: {e.code} - {error_body}")
        return {
            'statusCode': e.code,
            'body': json.dumps({'error': error_body})
        }
    except Exception as e:
        logger.error(f"Error occurred: {type(e).__name__} - {str(e)}")
        return {
            'statusCode': 500,
            'body': json.dumps({'error': str(e)})
        }
