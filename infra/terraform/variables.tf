variable "aws_region" {
  description = "AWS region where infrastructure will be created."
  type        = string
  default     = "us-east-1"
}

variable "instance_type" {
  description = "EC2 instance type for the Docker host."
  type        = string
  default     = "t3.micro"
}

variable "key_pair_name" {
  description = "Existing AWS EC2 key pair name for SSH access."
  type        = string

  validation {
    condition     = trimspace(var.key_pair_name) != ""
    error_message = "key_pair_name must be provided."
  }
}

variable "allowed_cidrs" {
  description = "CIDR blocks allowed to access SSH (22) and the application port."
  type        = list(string)
  default     = ["0.0.0.0/0"]

  validation {
    condition     = length(var.allowed_cidrs) > 0 && alltrue([for cidr in var.allowed_cidrs : can(cidrhost(cidr, 0))])
    error_message = "allowed_cidrs must include at least one valid CIDR block."
  }
}

variable "host_port" {
  description = "Port exposed on the EC2 host and opened in the security group."
  type        = number
  default     = 42000

  validation {
    condition     = var.host_port >= 1 && var.host_port <= 65535
    error_message = "host_port must be between 1 and 65535."
  }
}

variable "container_port" {
  description = "Container port mapped from host_port."
  type        = number
  default     = 6000

  validation {
    condition     = var.container_port >= 1 && var.container_port <= 65535
    error_message = "container_port must be between 1 and 65535."
  }
}

variable "docker_image_name" {
  description = "Docker Hub image name (for example, youruser/yourapp)."
  type        = string

  validation {
    condition     = trimspace(var.docker_image_name) != ""
    error_message = "docker_image_name must be provided."
  }
}

variable "docker_image_tag" {
  description = "Docker image tag to deploy."
  type        = string
  default     = "latest"

  validation {
    condition     = trimspace(var.docker_image_tag) != ""
    error_message = "docker_image_tag cannot be empty."
  }
}

variable "instance_name" {
  description = "Optional EC2 Name tag. If omitted, a default name is used."
  type        = string
  default     = null

  validation {
    condition     = var.instance_name == null || trimspace(var.instance_name) != ""
    error_message = "instance_name must be null or a non-empty string."
  }
}
