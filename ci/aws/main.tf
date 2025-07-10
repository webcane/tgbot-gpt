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

# elastic IP
resource "aws_eip" "this" {
  vpc = true
}

# set elastic ip to ec2 instance
resource "aws_eip_association" "this" {
  instance_id   = module.tgbot-ec2.id
  allocation_id = aws_eip.this.id
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
  user_data                   = file("conf/user_data.sh")
  tags                        = {
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