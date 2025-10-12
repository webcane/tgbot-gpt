terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.95.0"
    }
  }
  backend "s3" {
    bucket = "tgbot-gpt-tf"
    region = "eu-central-1"
    key    = "tgbot-gpt-infra.tfstate"
  }
}

provider "aws" {
  region = "eu-central-1"
}

#data "aws_region" "current" {}
data "aws_caller_identity" "current" {}

# default vpc
data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "this" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
  filter {
    name   = "availability-zone"
    values = ["eu-central-1a"]
  }
}

# define elastic ip for use in vpc
resource "aws_eip" "this" {
  domain = "vpc"
  tags = {
    Terraform = "true"
    Project   = var.app_name
  }
}

# set elastic ip to ec2 instance
resource "aws_eip_association" "this" {
  instance_id   = module.tgbot-ec2.id
  allocation_id = aws_eip.this.id
}

locals {
  registry_prefix = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com/"

  # generate cloud-init script from template
  # script will be executed by root user
  cloud_init_data = templatefile("${path.module}/templates/user_data.sh.tpl", {
    arch                    = "amd64"
    codename                = "noble"
    app_name                = var.app_name
    registry_prefix         = local.registry_prefix
    google_cloud_project_id = var.google_cloud_project_id
  })
  ecr_repository_name    = var.app_name
  github_repository_name = var.app_name

  # aws_iam_role.ec2_ssm_role.name
  iam_role_name = "${var.app_name}-ec2-ssm-role"
}

module "tgbot-ec2" {
  source                      = "terraform-aws-modules/ec2-instance/aws"
  version                     = ">= 5.8.0"
  name                        = "${var.app_name}-ec2"
  ami                         = "ami-02003f9f0fde924ea" # Ubuntu 24.04 64bit x86
  instance_type               = "t3.small"
  key_name                    = var.key_name
  vpc_security_group_ids      = [module.tgbot-sg.security_group_id]
  associate_public_ip_address = true
  subnet_id                   = data.aws_subnets.this.ids[0]
  user_data                   = local.cloud_init_data
  # Enable creation of EC2 IAM instance profile
  create_iam_instance_profile = true
  # role to deal with another AWS services. see aws_iam_role_policy_attachment.ec2_ssm_policy_attachment
  iam_role_name = local.iam_role_name
  root_block_device = {
    delete_on_termination = true
    encrypted             = false
    size                  = 16
    type                  = "gp3"
  }
  tags = {
    Terraform = "true"
    Project   = var.app_name
  }
}

module "tgbot-sg" {
  source              = "terraform-aws-modules/security-group/aws"
  version             = ">= 5.3.0"
  name                = "${var.app_name}-sg"
  description         = "Security group for telegram bot deployed on EC2. Allow public access"
  vpc_id              = data.aws_vpc.default.id
  ingress_cidr_blocks = ["0.0.0.0/0"]
  ingress_rules       = ["ssh-tcp", "http-80-tcp"]
  ingress_with_cidr_blocks = [
    {
      from_port   = 8080
      to_port     = 8080
      protocol    = "tcp"
      cidr_blocks = "0.0.0.0/0"
    }
  ]
  egress_rules = ["all-all"]
}

resource "aws_budgets_budget" "free_tier" {
  name         = "tgbot-freetier-monthly-budget"
  budget_type  = "COST"
  limit_amount = "1.00"
  limit_unit   = "USD"
  time_unit    = "MONTHLY"
  cost_filter {
    name = "Service"
    values = [
      "Amazon Elastic Compute Cloud - Compute",
    ]
  }
  notification {
    comparison_operator = "GREATER_THAN"
    threshold           = 80
    threshold_type      = "PERCENTAGE"
    notification_type   = "ACTUAL"
    subscriber_email_addresses = [
      var.alert_email
    ]
  }
  notification {
    comparison_operator = "GREATER_THAN"
    threshold           = 100
    threshold_type      = "PERCENTAGE"
    notification_type   = "FORECASTED"
    subscriber_email_addresses = [
      var.alert_email
    ]
  }
  tags = {
    Terraform = "true"
    Project   = var.app_name
  }
}

# ECR
module "ecr_repository" {
  source            = "terraform-aws-modules/ecr/aws"
  version           = "~> 2.4.0"
  repository_name   = local.ecr_repository_name
  create_repository = true
  # Configuration for image scanning and tag immutability (recommended)
  repository_image_scan_on_push = true
  # Prevents tags from being overwritten (e.g., 'latest')
  repository_image_tag_mutability = "MUTABLE"
  repository_lifecycle_policy = jsonencode({
    rules = [
      {
        rulePriority = 1,
        description  = "Keep the latest 5 tagged images",
        selection = {
          tagStatus     = "tagged",
          tagPrefixList = ["latest"],
          countType     = "imageCountMoreThan",
          countNumber   = 5
        },
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# --- IAM Role for GitHub Actions (using OpenID Connect OIDC) ---
# This is the most secure way to authenticate GitHub Actions with AWS.
# It avoids storing long-lived AWS Access Key IDs and Secret Access Keys in GitHub Secrets.

# 1. Get the OIDC provider for GitHub
resource "aws_iam_openid_connect_provider" "github_actions" {
  url = "https://token.actions.githubusercontent.com"
  client_id_list = [
    "sts.amazonaws.com"
  ]
  # Список отпечатков (thumbprints) для корневых сертификатов провайдера OIDC.
  # Их можно получить, используя команду openssl или из документации AWS/GitHub.
  # Эти значения стабильны, но могут меняться со временем.
  # Актуальный список можно найти здесь:
  # https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-amazon-web-services#configuring-aws-with-openid-connect
  # или получить через 'openssl s_client -showcerts -verify 5 -connect token.actions.githubusercontent.com:443 < /dev/null | awk '/BEGIN CERTIFICATE/,/END CERTIFICATE/{ print $0 }' | openssl x509 -fingerprint -noout'
  thumbprint_list = [
    "7560d6f40fa55195f740ee2b1b7c0b4836cbe103",
    "a031c46782e0e6c694c7739af3c983a93f00ef16"
  ]

  tags = {
    Terraform = "true"
    Project   = var.app_name
    Name      = "GitHubActions-OIDC-Provider"
  }
}

# 2. Create the IAM role
resource "aws_iam_role" "github_actions_ecr" {
  name = "${local.ecr_repository_name}-github-actions-ecr-role"

  # Policy that allows OIDC authentication from GitHub Actions
  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Principal = {
          Federated = aws_iam_openid_connect_provider.github_actions.arn
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
            # Limit the role to a specific GitHub repository for enhanced security
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_repository_owner}/${local.github_repository_name}:environment:${var.github_environment_name}"
          }
        }
      }
    ]
  })
}

# 3. Create an IAM policy with ECR permissions
resource "aws_iam_policy" "github_actions_ecr_policy" {
  name        = "${local.ecr_repository_name}-github-actions-ecr-policy"
  description = "Policy to allow GitHub Actions to push/pull from ECR for ${local.ecr_repository_name}"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:GetAuthorizationToken", # Needed for authentication
          "ecr:DescribeImages"
        ],
        Resource = module.ecr_repository.repository_arn # Apply permissions only to our specific ECR repo
      },
      # The GetAuthorizationToken action is a global action and does not support resource-level permissions.
      # It must be allowed for all resources (*).
      {
        Effect   = "Allow",
        Action   = "ecr:GetAuthorizationToken",
        Resource = "*"
      }
    ]
  })
}

# 4. Attach the policy to the role
resource "aws_iam_role_policy_attachment" "github_actions_ecr_attachment" {
  role       = aws_iam_role.github_actions_ecr.name
  policy_arn = aws_iam_policy.github_actions_ecr_policy.arn
}

# --- Присоединение управляемой политики SSM к EC2 ---
# Политика 'AmazonSSMManagedInstanceCore' предоставляет все необходимые разрешения
# для SSM Agent, чтобы он мог регистрироваться в SSM и выполнять команды.
resource "aws_iam_role_policy_attachment" "ec2_ssm_policy_attachment" {
  role       = module.tgbot-ec2.iam_role_name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_ecr_policy_attachment" {
  role       = module.tgbot-ec2.iam_role_name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryPowerUser"
}

# IAM Policy для чтения параметра SSM
resource "aws_iam_policy" "kms_policy" {
  name        = "ssm-google-credentials-decrypt-policy"
  description = "Allows EC2 to decrypt Google credentials from SSM Parameter Store"
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow",
        Action = [
          # Требуется, если ваш SecureString зашифрован KMS ключом (по умолчанию это AWS managed key)
          "kms:Decrypt"
        ],
        # Можно ограничить конкретным KMS ключом, если используете свой
        Resource = "arn:aws:kms:${var.aws_region}:${data.aws_caller_identity.current.account_id}:key/*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ec2_kms_policy_attachment" {
  role       = module.tgbot-ec2.iam_role_name
  policy_arn = aws_iam_policy.kms_policy.arn
}



