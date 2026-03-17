output "instance_public_ip" {
  description = "Public IP address of the EC2 instance."
  value       = aws_instance.docker_host.public_ip
}

output "instance_public_dns" {
  description = "Public DNS name of the EC2 instance."
  value       = aws_instance.docker_host.public_dns
}

output "service_url" {
  description = "URL to access the deployed containerized application."
  value       = "http://${aws_instance.docker_host.public_dns}:${var.host_port}"
}
