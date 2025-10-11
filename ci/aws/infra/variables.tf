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

variable "key_name" {
  description = "SSH key name"
  type        = string
  default     = "tgbot-gpt-aws-keypair"
}

variable "alert_email" {
  description = "budget alert email"
  type        = string
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
  description = "Google Cloud project ID"
  type        = string
}