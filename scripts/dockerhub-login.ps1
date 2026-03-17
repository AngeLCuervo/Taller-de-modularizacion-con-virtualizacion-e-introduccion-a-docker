<#
Usage:
  .\scripts\dockerhub-login.ps1
  .\scripts\dockerhub-login.ps1 -Username 'dockeruser'
  .\scripts\dockerhub-login.ps1 -ShowCommandOnly

Notes:
  - Login is always interactive; credentials are never stored in this repository.
  - Prefer a Docker Hub Personal Access Token when prompted for password.
#>
param(
    [string]$Username = $env:DOCKERHUB_USER,
    [string]$Registry = 'docker.io',
    [switch]$ShowCommandOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI was not found in PATH. Install/start Docker Desktop and reopen the terminal."
}

$commandText = if ([string]::IsNullOrWhiteSpace($Username)) {
    "docker login $Registry"
} else {
    "docker login $Registry --username $Username"
}

Write-Host 'Docker login is interactive.'
Write-Host 'Use your Docker Hub username + password/PAT at the prompt.'
Write-Host "Command: $commandText"

if ($ShowCommandOnly) {
    return
}

if ([string]::IsNullOrWhiteSpace($Username)) {
    docker login $Registry
} else {
    docker login $Registry --username $Username
}
