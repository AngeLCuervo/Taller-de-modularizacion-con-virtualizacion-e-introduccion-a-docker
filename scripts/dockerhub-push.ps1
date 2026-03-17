<#
Usage:
  .\scripts\dockerhub-push.ps1
  .\scripts\dockerhub-push.ps1 -SourceImage 'arem-docker-aws-lab:local' -Repository 'dockeruser/arem-docker-aws-lab' -Tag 'v1'
  .\scripts\dockerhub-push.ps1 -LoginFirst
  .\scripts\dockerhub-push.ps1 -SkipTag

Notes:
  - Repository defaults to $env:DOCKERHUB_REPOSITORY.
  - By default this script tags first, then pushes to Docker Hub.
  - Use -LoginFirst to run interactive docker login before push.
#>
param(
    [string]$SourceImage = 'arem-docker-aws-lab:local',
    [string]$Repository = $env:DOCKERHUB_REPOSITORY,
    [string]$Tag = 'latest',
    [switch]$SkipTag,
    [switch]$LoginFirst
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    throw "Docker CLI was not found in PATH. Install/start Docker Desktop and reopen the terminal."
}

if ([string]::IsNullOrWhiteSpace($Repository)) {
    $Repository = Read-Host 'Docker Hub repository (user/repo)'
}

if ([string]::IsNullOrWhiteSpace($Repository)) {
    throw "A Docker Hub repository is required (format: user/repo)."
}

$targetImage = "${Repository}:$Tag"

if (-not $SkipTag) {
    & (Join-Path $PSScriptRoot 'dockerhub-tag.ps1') -SourceImage $SourceImage -Repository $Repository -Tag $Tag
}

if ($LoginFirst) {
    & (Join-Path $PSScriptRoot 'dockerhub-login.ps1')
} else {
    Write-Host "If needed, run .\scripts\dockerhub-login.ps1 before pushing."
}

Write-Host "Pushing '$targetImage' to Docker Hub..."
docker push $targetImage
