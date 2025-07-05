output "aws_ec2_id" {
  value = module.tgbot-ec2.id
}

output "aws_ec2_public_ip" {
  value = module.tgbot-ec2.public_ip
}

output "aws_subnet_id" {
  value = data.aws_subnets.this.ids[0]
}

output "aws_vpc_id" {
  value = data.aws_vpc.default.id
}