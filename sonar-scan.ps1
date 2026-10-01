$ErrorActionPreference = 'Stop'

if ([string]::IsNullOrWhiteSpace($env:SONAR_HOST_URL)) {
    throw 'Set SONAR_HOST_URL to your SonarQube server URL.'
}
if ([string]::IsNullOrWhiteSpace($env:SONAR_TOKEN)) {
    throw 'Set SONAR_TOKEN in the environment. Do not put it in a repository file.'
}
if (-not (Get-Command sonar-scanner -ErrorAction SilentlyContinue)) {
    throw 'SonarScanner CLI was not found on PATH. Install it before running this script.'
}

$workspace = Split-Path -Parent $MyInvocation.MyCommand.Path
$mavenWrapper = Join-Path $workspace 'order-service\mvnw.cmd'
$services = @(
    'api-gateway',
    'authentication-service',
    'config-server',
    'crop-service',
    'eureka-server',
    'notification-service',
    'order-service',
    'payment-service',
    'profile-service',
    'subscription-service'
)

foreach ($service in $services) {
    Write-Host "Generating tests and JaCoCo coverage for $service"
    $pom = Join-Path (Join-Path $workspace $service) 'pom.xml'
    & $mavenWrapper -f $pom `
        'org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent' `
        test `
        'org.jacoco:jacoco-maven-plugin:0.8.15:report'
    if ($LASTEXITCODE -ne 0) {
        throw "Tests or coverage generation failed for $service (exit code $LASTEXITCODE)."
    }
}

Write-Host 'Scanning all CropDeal services with SonarQube'
Push-Location $workspace
try {
    & sonar-scanner
    if ($LASTEXITCODE -ne 0) {
        throw "SonarScanner failed (exit code $LASTEXITCODE)."
    }
}
finally {
    Pop-Location
}
