variable "app_name" {
  description = "Application name"
  type        = string
  default     = "tgbot-gpt"
}

variable "aws_region" {
  description = "AWS region for the ECR repository and IAM role."
  type        = string
  default     = "eu-central-1"
}

variable "aws_account" {
  description = "AWS account"
  type        = string
}

variable "key_name" {
  description = "SSH key name"
  type        = string
  default     = "tgbot-gpt-aws-keypair"
}

variable "alert_email" {
  description = "budget alert email"
  type        = string
}

variable "server_port" {
}

variable "tgbot_token" {
}

variable "tgbot_voice_path" {
}

variable "tgbot_allowed_user_names" {
  description = "List of admin"
  type        = list(string)
}

variable "tgbot_proxy_hostname" {
}

variable "tgbot_proxy_port" {
  default = 42567
}

variable "tgbot_proxy_username" {
}

variable "tgbot_proxy_password" {
}

variable "openai_api_key" {
}

variable "github_repository_owner" {
  description = "The owner (organization or user) of the GitHub repository."
  type        = string
  default     = "webcane"
}

variable "github_environment_name" {
  description = "The github action environment name."
  type        = string
  default     = "aws"
}

variable "google_cloud_project_id" {
}

variable "google_cloud_region" {
}
