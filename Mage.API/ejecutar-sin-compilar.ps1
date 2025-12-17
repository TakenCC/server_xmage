# Script para ejecutar el servidor SIN recompilar
# Útil si ya compilaste antes y solo quieres ejecutar

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MAGE REST API Server" -ForegroundColor Cyan
Write-Host "  (Ejecutando sin recompilar)" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$rootDir = Split-Path -Parent $PSScriptRoot

# Verificar si ya está compilado
$jarPath = Join-Path $PSScriptRoot "target\mage-api.jar"
if (-not (Test-Path $jarPath)) {
    Write-Host "[ADVERTENCIA] No se encontró el JAR compilado." -ForegroundColor Yellow
    Write-Host "¿Deseas compilar ahora? (S/N)" -ForegroundColor Yellow
    $response = Read-Host
    if ($response -eq "S" -or $response -eq "s" -or $response -eq "Y" -or $response -eq "y") {
        Write-Host ""
        Write-Host "Compilando..."
        Set-Location $rootDir
        & mvn clean install -DskipTests -T 4
        if ($LASTEXITCODE -ne 0) {
            Write-Host "[ERROR] Fallo en la compilación." -ForegroundColor Red
            Read-Host "Presiona Enter para salir"
            exit 1
        }
        Set-Location $PSScriptRoot
    } else {
        Write-Host "Saliendo..."
        exit 0
    }
}

# Configurar puerto (por defecto 8080, pero se puede cambiar con variable de entorno)
$port = if ($env:XMAGE_API_PORT) { $env:XMAGE_API_PORT } else { "8080" }

Write-Host "Iniciando servidor..." -ForegroundColor Green
Write-Host "El servidor estará disponible en: http://localhost:$port" -ForegroundColor Yellow
Write-Host "Presiona Ctrl+C para detener el servidor"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location $PSScriptRoot
& mvn exec:java "-Dxmage.config.path=..\Mage.Server\config\config.xml" "-Dxmage.api.port=$port"

