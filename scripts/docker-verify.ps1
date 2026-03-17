Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$hostPorts = @(6101, 6102, 6103)

docker ps --filter "name=arem-app-" --filter "status=running" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

foreach ($port in $hostPorts) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$port/hello?name=Docker" -UseBasicParsing -TimeoutSec 5
        Write-Host "localhost:$port -> HTTP $($response.StatusCode)"
    } catch {
        Write-Warning "localhost:$port -> request failed ($($_.Exception.Message))"
    }
}

