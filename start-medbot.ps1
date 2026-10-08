Set-Location $PSScriptRoot

Write-Host "Starting MedBot..." -ForegroundColor Cyan
Write-Host "MedBot will use port 8081." -ForegroundColor Yellow
Write-Host ""

mvn.cmd spring-boot:run
