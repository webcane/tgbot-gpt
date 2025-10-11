output "parameter_google_credentials_json_arn" {
  value = aws_ssm_parameter.google_credentials_json.arn
}

output "parameter_docker_compose_yml_arn" {
  value = aws_ssm_parameter.docker_compose_yml.arn
}

output "parameter_deploy_sh_arn" {
  value = aws_ssm_parameter.deploy_sh.arn
}

output "parameter_dot_env_arn" {
  value = aws_ssm_parameter.dot_env.arn
}
