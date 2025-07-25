output "aws_ec2_id" {
  value = module.tgbot-ec2.id
}

output "aws_elastic_public_ip" {
  value = aws_eip.this.public_ip
}

output "aws_subnet_id" {
  value = data.aws_subnets.this.ids[0]
}

output "aws_vpc_id" {
  value = data.aws_vpc.default.id
}

output "aws_repository_arn" {
  value = module.ecr_repository.repository_arn
}

output "aws_repository_name" {
  value = module.ecr_repository.repository_name
}

output "github_actions_to_ecr_role_arn" {
  value = aws_iam_role.github_actions_ecr.arn
}

output "aws_iam_openid_connect_provider_arn" {
  value = aws_iam_openid_connect_provider.github_actions.arn
}