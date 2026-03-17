<#
Usage:
  .\scripts\dockerhub-tag.ps1
  .\scripts\dockerhub-tag.ps1 -SourceImage 'arem-docker-aws-lab:local' -Repository 'dockeruser/arem-docker-aws-lab' -Tag 'v1'

Notes:
  - Repository defaults to $env:DOCKERHUB_REPOSITORY.
  - If repository is missing, the script asks for user/repo interactively.
#>
param(
    [string]$SourceImage = 'arem-docker-aws-lab:local',
    [string]$Repository = $env:DOCKERHUB_REPOSITORY,
    [string]$Tag = 'latest'
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

Write-Host "Tagging '$SourceImage' as '$targetImage'..."
docker image tag $SourceImage $targetImage
Write-Host "Created Docker Hub tag: $targetImage"
