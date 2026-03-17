Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
Set-Location $projectRoot

$imageTag = if ($args.Count -gt 0) { $args[0] } else { 'arem-docker-aws-lab:local' }
$hostPorts = @(6101, 6102, 6103)
$containerPort = 6000

foreach ($index in 0..2) {
    $name = "arem-app-$($index + 1)"
    $hostPort = $hostPorts[$index]

    docker rm -f $name 2>$null | Out-Null
    docker run -d --name $name -e PORT=$containerPort -p "${hostPort}:${containerPort}" $imageTag | Out-Host
}

docker ps --filter "name=arem-app-" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

