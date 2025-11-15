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

variable "ami" {
  description = "AMI ID for EC2 instance. Default: Ubuntu 24.04 64bit x86."
  type        = string
  default     = "ami-02003f9f0fde924ea"
}

variable "instance_type" {
  description = "EC2 instance type."
  type        = string
  default     = "t2.micro"
}

variable "ingress_cidr_blocks" {
  description = "Ingress CIDR blocks for security group."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "ingress_rules" {
  description = "Ingress rules for security group."
  type        = list(string)
  default     = ["ssh-tcp", "http-80-tcp"]
}

variable "retention_in_days" {
  description = "CloudWatch log group retention in days."
  type        = number
  default     = 30
}

variable "repository_image_tag_mutability" {
  description = "ECR repository image tag mutability. Prevents tags from being overwritten (e.g., 'latest')"
  type        = string
  default     = "MUTABLE"
}

variable "repository_image_scan_on_push" {
  description = "Enable ECR image scan on push. Configuration for image scanning and tag immutability (recommended)"
  type        = bool
  default     = true
}

variable "thumbprint_list" {
  # List of thumbprints for root CAs used by GitHub Actions OIDC provider
  # request it using openssl or find in AWS/GitHub documentation
  # these values are stable but may change over time
  # up-to-date list can be found here:
  # https://docs.github.com/en/actions/deployment/security-hardening-your-deployments/configuring-openid-connect-in-amazon-web-services#configuring-aws-with-openid-connect
  # or get it via 'openssl s_client -showcerts -verify 5 -connect token.actions.githubusercontent.com:443 < /dev/null | awk '/BEGIN CERTIFICATE/,/END CERTIFICATE/{ print $0 }' | openssl x509 -fingerprint -noout'
  description = "List of CA certificate thumbprints for IAM OIDC provider."
  type        = list(string)
  default = [
    "7560d6f40fa55195f740ee2b1b7c0b4836cbe103",
    "a031c46782e0e6c694c7739af3c983a93f00ef16"
  ]
}
