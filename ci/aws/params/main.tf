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
    key    = "tgbot-gpt-params.tfstate"
  }
}

provider "aws" {
  region = "eu-central-1"
}

resource "aws_ssm_parameter" "google_credentials_json" {
  name        = "/tgbot-gpt/google_credentials_json"
  description = "Google Cloud credentials file in JSON format"
  type        = "SecureString"
  value       = file("${pathexpand("~")}/.config/google/google-credentials.json")
}

resource "aws_ssm_parameter" "docker_compose_yml" {
  name        = "/tgbot-gpt/docker_compose_yml"
  description = "Docker Compose file"
  type        = "SecureString"
  value       = file("${path.module}/../../../compose.yaml")
}

resource "aws_ssm_parameter" "deploy_sh" {
  name        = "/tgbot-gpt/deploy_sh"
  description = "tgbot-gpt deploy script"
  type        = "SecureString"
  value       = file("${path.module}/../../../deploy.sh")
}

resource "aws_ssm_parameter" "dot_env" {
  name        = "/tgbot-gpt/dot_env"
  description = "tgbot-gpt environment variables file"
  type        = "SecureString"
  value       = file("${path.module}/../../../aws.env")
}
