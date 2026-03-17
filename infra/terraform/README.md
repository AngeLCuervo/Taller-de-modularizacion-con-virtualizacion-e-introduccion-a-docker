# AWS EC2 + Docker deployment with Terraform

This folder provisions an Amazon Linux 2 EC2 instance, configures Docker, pulls a Docker Hub image, and runs it with host/container port mapping.

## Files

- `main.tf`: AWS provider, security group, EC2 instance, and user data bootstrap
- `variables.tf`: configurable inputs (region, instance type, key pair, CIDRs, ports, image, tag, name)
- `outputs.tf`: public IP, public DNS, and service URL
- `terraform.tfvars.example`: sample values

## Usage

1. Copy the example variable file and edit values:

   ```powershell
   Copy-Item terraform.tfvars.example terraform.tfvars
   ```

2. Initialize Terraform:

   ```bash
   terraform init
   ```

3. Validate and preview:

   ```bash
   terraform fmt -recursive
   terraform validate
   terraform plan
   ```

4. Apply:

   ```bash
   terraform apply
   ```

5. After apply, use outputs:

   - `instance_public_ip`
   - `instance_public_dns`
   - `service_url`

## Notes

- Ensure the `key_pair_name` already exists in the target AWS region.
- Restrict `allowed_cidrs` to trusted IP ranges for production.
