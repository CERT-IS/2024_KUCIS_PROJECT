provider "aws" {
  region = "ap-northeast-2"
}

data "aws_caller_identity" "current" {}

# S3 버킷 생성
resource "aws_s3_bucket" "example" {
  bucket = "bucket-test-3f98sddj30sdjvejw0zdsjf203jefs"
}

# S3 버킷 IAM 역할 생성
resource "aws_s3_bucket_policy" "cloudtrail_bucket_policy" {
  bucket = aws_s3_bucket.example.bucket

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "cloudtrail.amazonaws.com"
        }
        Action = "s3:PutObject"
        Resource = "${aws_s3_bucket.example.arn}/AWSLogs/${data.aws_caller_identity.current.account_id}/*"
        Condition = {
          StringEquals = {
            "s3:x-amz-acl" = "bucket-owner-full-control"
          }
        }
      },
      {
        Effect = "Allow"
        Principal = {
          Service = "cloudtrail.amazonaws.com"
        }
        Action = "s3:GetBucketAcl"
        Resource = aws_s3_bucket.example.arn
      }
    ]
  })
}



# CloudWatch Log 그룹 생성
resource "aws_cloudwatch_log_group" "cloudtrail_log_group" {
  name = "cloudtrail_log_group"
}

resource "aws_cloudwatch_log_group" "lambda_log_group" {
  name = "/aws/lambda/my_lambda_function"
}

# IAM 역할 생성
resource "aws_iam_role" "cloudwatch_logs_role" {
  name = "cloudwatch_logs_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Principal = {
          Service = "logs.amazonaws.com"
        }
        Effect = "Allow"
        Sid    = ""
      },
    ]
  })
}

resource "aws_iam_policy" "cloudwatch_logs_policy" {
  name        = "cloudwatch_logs_policy"
  description = "Policy for CloudWatch Logs to send data to OpenSearch"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = "es:Bulk",
        Resource = aws_opensearch_domain.example.arn
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cloudwatch_logs_attachment" {
  policy_arn = aws_iam_policy.cloudwatch_logs_policy.arn
  role       = aws_iam_role.cloudwatch_logs_role.name
}

# IAM 역할 생성 - CloudTrail
resource "aws_iam_role" "cloudtrail_role" {
  name = "cloudtrail-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "cloudtrail.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
    ]
  })
}

resource "aws_iam_policy" "cloudtrail_logs_policy" {
  name        = "cloudtrail-logs-policy"
  description = "Policy for CloudTrail to write logs to CloudWatch Logs"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = [
          aws_cloudwatch_log_group.cloudtrail_log_group.arn,
          "${aws_cloudwatch_log_group.cloudtrail_log_group.arn}:*"
        ]
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "cloudtrail_logs_policy_attachment" {
  role       = aws_iam_role.cloudtrail_role.name
  policy_arn = aws_iam_policy.cloudtrail_logs_policy.arn
}


# CloudTrail 생성
resource "aws_cloudtrail" "example" {
  depends_on = [aws_cloudwatch_log_group.cloudtrail_log_group,
    aws_iam_role_policy_attachment.cloudtrail_logs_policy_attachment]

  name                          = "example"
  s3_bucket_name                = aws_s3_bucket.example.bucket
  is_multi_region_trail         = true
  enable_log_file_validation    = true
  cloud_watch_logs_group_arn    = "${aws_cloudwatch_log_group.cloudtrail_log_group.arn}:*"
  cloud_watch_logs_role_arn     = aws_iam_role.cloudtrail_role.arn
}

# IAM 역할 생성 - Lambda
resource "aws_iam_role" "lambda_exec" {
  name = "lambda_exec_role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action    = "sts:AssumeRole"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
        Effect    = "Allow"
        Sid       = ""
      }
    ]
  })
}

# IAM 정책 생성 - Lambda 로그
resource "aws_iam_policy" "lambda_logging" {
  name        = "lambda_logging_policy"
  description = "IAM policy for Lambda function logging"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = "logs:CreateLogGroup",
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "ec2:DescribeSecurityGroups",
          "ec2:DescribeSubnets",
          "ec2:DescribeVpcs",
          "ec2:DescribeNetworkInterfaces"
        ],
        Resource = "*"
      }
    ]
  })
}

# IAM 정책 생성 - Lambda 호출
resource "aws_iam_policy" "lambda_invoke" {
  name        = "lambda_invoke_policy"
  description = "IAM policy for allowing invocation of the Lambda function"
  policy      = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "lambda:InvokeFunction"
        ],
        Resource = "*"
      }
    ]
  })
}

# IAM 역할에 정책 연결
resource "aws_iam_role_policy_attachment" "lambda_logging_attachment" {
  policy_arn = aws_iam_policy.lambda_logging.arn
  role       = aws_iam_role.lambda_exec.name
}

resource "aws_iam_role_policy_attachment" "lambda_invoke_attachment" {
  policy_arn = aws_iam_policy.lambda_invoke.arn
  role       = aws_iam_role.lambda_exec.name
}

# Lambda 함수 생성
resource "aws_lambda_function" "my_lambda" {
  depends_on = [aws_iam_role.lambda_exec]

  function_name = "my_lambda_function"
  runtime       = "python3.8"
  role          = aws_iam_role.lambda_exec.arn
  filename      = "./lambda_handler.zip"
  handler       = "lambda_handler.lambda_handler"
  timeout       = "600"
  source_code_hash = filebase64sha256("./lambda_handler.zip")
}

# API Gateway 생성
resource "aws_api_gateway_rest_api" "my_api" {
  name        = "my_api"
  description = "My API Gateway"
}

# 프록시 리소스 및 메서드 생성
resource "aws_api_gateway_resource" "proxy" {
  rest_api_id = aws_api_gateway_rest_api.my_api.id
  parent_id   = aws_api_gateway_rest_api.my_api.root_resource_id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "proxy_any" {
  rest_api_id   = aws_api_gateway_rest_api.my_api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "ANY"
  authorization = "NONE"
}

resource "aws_api_gateway_method" "options" {
  rest_api_id   = aws_api_gateway_rest_api.my_api.id
  resource_id   = aws_api_gateway_resource.proxy.id
  http_method   = "OPTIONS"
  authorization = "NONE"
}

# Lambda 통합
resource "aws_api_gateway_integration" "proxy_integration" {
  rest_api_id             = aws_api_gateway_rest_api.my_api.id
  resource_id             = aws_api_gateway_resource.proxy.id
  http_method             = aws_api_gateway_method.proxy_any.http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.my_lambda.invoke_arn
}

# OPTIONS 메서드에 대한 통합 추가
resource "aws_api_gateway_integration" "options_integration" {
  rest_api_id             = aws_api_gateway_rest_api.my_api.id
  resource_id             = aws_api_gateway_resource.proxy.id
  http_method             = aws_api_gateway_method.options.http_method
  integration_http_method = "OPTIONS"
  type                    = "MOCK"  # OPTIONS 메서드는 MOCK 타입으로 설정
}

# CORS 활성화
resource "aws_api_gateway_method_response" "options_response" {
  rest_api_id = aws_api_gateway_rest_api.my_api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.options.http_method
  status_code = "200"

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = true
    "method.response.header.Access-Control-Allow-Methods" = true
    "method.response.header.Access-Control-Allow-Headers" = true
  }
}

resource "aws_api_gateway_integration_response" "options_integration_response" {
  depends_on = [aws_api_gateway_integration.options_integration]
  rest_api_id = aws_api_gateway_rest_api.my_api.id
  resource_id = aws_api_gateway_resource.proxy.id
  http_method = aws_api_gateway_method.options.http_method
  status_code = "200"

  response_parameters = {
    "method.response.header.Access-Control-Allow-Origin"  = "'*'"
    "method.response.header.Access-Control-Allow-Methods" = "'OPTIONS,GET,POST,PUT,DELETE'"
    "method.response.header.Access-Control-Allow-Headers" = "'Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token'"
  }
}

# 배포 생성
resource "aws_api_gateway_deployment" "my_deployment" {
  depends_on = [
    aws_api_gateway_integration.proxy_integration,
    aws_api_gateway_integration.options_integration  # OPTIONS 메서드 통합도 포함
  ]
  rest_api_id = aws_api_gateway_rest_api.my_api.id
  stage_name  = "prod"
}


# OpenSearch 도메인 생성
resource "aws_opensearch_domain" "example" {
  domain_name           = "example-domain"
  engine_version = "OpenSearch_1.0" # 2.13

  cluster_config {
    instance_type = "t2.small.search"
  }

  ebs_options {
    ebs_enabled  = true
    volume_size  = 10
  }
}

output "cloudtrail_log_group_arn" {
  value = aws_cloudwatch_log_group.cloudtrail_log_group.arn
}