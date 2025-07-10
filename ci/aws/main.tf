terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.95.0"
    }
  }
}

provider "aws" {
  region = "eu-central-1"
}

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
  tags   = {
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
  cloud_init_data = templatefile("${path.module}/user_data.sh.tpl", {
    app_name = var.app_name
    email    = var.alert_email
  })
}

module "tgbot-ec2" {
  source                      = "terraform-aws-modules/ec2-instance/aws"
  version                     = ">= 5.8.0"
  name                        = "${var.app_name}-ec2"
  ami                         = "ami-02003f9f0fde924ea" # Ubuntu 24.04 64bit x86
  instance_type               = "t2.micro"
  key_name                    = var.key_name
  vpc_security_group_ids      = [module.tgbot-sg.security_group_id]
  associate_public_ip_address = false
  subnet_id                   = data.aws_subnets.this.ids[0]
  user_data                   = local.cloud_init_data
  tags = {
    Terraform = "true"
    Project   = var.app_name
  }
}

module "tgbot-sg" {
  source                   = "terraform-aws-modules/security-group/aws"
  version                  = ">= 5.3.0"
  name                     = "${var.app_name}-sg"
  description              = "Security group for telegram bot deployed on EC2. Allow public access"
  vpc_id                   = data.aws_vpc.default.id
  ingress_cidr_blocks      = ["0.0.0.0/0"]
  ingress_rules            = ["ssh-tcp", "http-80-tcp"]
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
    name   = "Service"
    values = [
      "Amazon Elastic Compute Cloud - Compute",
    ]
  }
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 80
    threshold_type             = "PERCENTAGE"
    notification_type          = "ACTUAL"
    subscriber_email_addresses = [
      var.alert_email
    ]
  }
  notification {
    comparison_operator        = "GREATER_THAN"
    threshold                  = 100
    threshold_type             = "PERCENTAGE"
    notification_type          = "FORECASTED"
    subscriber_email_addresses = [
      var.alert_email
    ]
  }
  tags = {
    Terraform = "true"
    Project   = var.app_name
  }
}
