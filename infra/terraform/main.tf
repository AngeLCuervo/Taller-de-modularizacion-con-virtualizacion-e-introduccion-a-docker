provider "aws" {
  region = var.aws_region
}

locals {
  image_ref = "${var.docker_image_name}:${var.docker_image_tag}"
  ec2_name  = var.instance_name != null && trimspace(var.instance_name) != "" ? var.instance_name : "arem-docker-host"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }

  filter {
    name   = "root-device-type"
    values = ["ebs"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

resource "aws_security_group" "docker_host" {
  name_prefix = "arem-docker-sg-"
  description = "Allow SSH and application access"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidrs
  }

  ingress {
    description = "Application port"
    from_port   = var.host_port
    to_port     = var.host_port
    protocol    = "tcp"
    cidr_blocks = var.allowed_cidrs
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name      = "${local.ec2_name}-sg"
    ManagedBy = "terraform"
  }
}

resource "aws_instance" "docker_host" {
  ami                         = data.aws_ami.amazon_linux_2.id
  instance_type               = var.instance_type
  key_name                    = var.key_pair_name
  vpc_security_group_ids      = [aws_security_group.docker_host.id]
  associate_public_ip_address = true
  user_data_replace_on_change = true

  user_data = <<-EOF
    #!/bin/bash
    set -euxo pipefail

    yum update -y
    amazon-linux-extras install docker -y || yum install -y docker
    systemctl enable docker
    systemctl start docker
    usermod -aG docker ec2-user

    docker pull "${local.image_ref}"

    if docker ps -a --format '{{.Names}}' | grep -Eq '^app$'; then
      docker rm -f app
    fi

    docker run -d --restart unless-stopped \
      --name app \
      -p ${var.host_port}:${var.container_port} \
      "${local.image_ref}"
  EOF

  tags = {
    Name      = local.ec2_name
    ManagedBy = "terraform"
  }
}
