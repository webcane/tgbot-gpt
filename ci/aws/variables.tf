variable "app_name" {
  description = "Application name"
  type        = string
  default     = "tgbot-gpt"
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