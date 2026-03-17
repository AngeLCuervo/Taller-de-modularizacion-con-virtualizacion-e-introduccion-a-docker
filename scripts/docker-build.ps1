Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $projectRoot

$imageTag = if ($args.Count -gt 0) { $args[0] } else { 'arem-docker-aws-lab:local' }

mvn --no-transfer-progress clean package
docker build --build-arg APP_PORT=6000 -t $imageTag .

