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