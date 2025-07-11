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


variable "server_port" {
}

variable "openai_api_key" {
}

variable "tgbot_token" {
}

variable "tgbot_voice_path" {
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