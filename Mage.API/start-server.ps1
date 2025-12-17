# Script PowerShell para iniciar el servidor MAGE REST API
# Para Windows PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  MAGE REST API Server" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que Maven esté instalado
Write-Host "Verificando Maven..." -NoNewline
try {
    $mvnVersion = & mvn -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Maven no funciona"
    }
    Write-Host " ✓" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "[ERROR] Maven no encontrado. Por favor instala Maven primero." -ForegroundColor Red
    Write-Host ""
    Write-Host "Consulta el archivo INSTALAR_MAVEN_WINDOWS.md para instrucciones detalladas."
    Write-Host "O visita: https://maven.apache.org/download.cgi"
    Write-Host ""
    Read-Host "Presiona Enter para salir"
    exit 1
}
Write-Host ""

# Verificar que Java esté instalado
Write-Host "Verificando Java..." -NoNewline
try {
    $javaVersion = & java -version 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "Java no funciona"
    }
    Write-Host " ✓" -ForegroundColor Green
} catch {
    Write-Host ""
    Write-Host "[ERROR] Java no encontrado. Por favor instala Java JDK 8 o superior." -ForegroundColor Red
    Write-Host ""
    Write-Host "Descarga Java desde: https://adoptium.net/"
    Write-Host ""
    Read-Host "Presiona Enter para salir"
    exit 1
}
Write-Host ""

# Verificar compilación
Write-Host "Verificando compilación..."
Write-Host "(Esto puede tardar varios minutos la primera vez mientras descarga dependencias)" -ForegroundColor Yellow
$rootDir = Split-Path -Parent $PSScriptRoot
Set-Location $rootDir

# Intentar compilación rápida con paralelismo (solo Mage.API y sus dependencias)
Write-Host "Compilando Mage.API con 4 hilos en paralelo..." -ForegroundColor Cyan
$compileOutput = & mvn clean install -pl Mage.API -am -DskipTests -T 4 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "[ERROR] Fallo en la compilación." -ForegroundColor Red
    Write-Host ""
    Write-Host $compileOutput
    Write-Host ""
    Read-Host "Presiona Enter para salir"
    exit 1
}

Write-Host "Compilación exitosa! ✓" -ForegroundColor Green
Write-Host ""

# Iniciar servidor
$port = if ($env:XMAGE_API_PORT) { $env:XMAGE_API_PORT } else { "8080" }
Write-Host "Iniciando servidor..."
Write-Host "El servidor estará disponible en: http://localhost:$port" -ForegroundColor Yellow
Write-Host "Presiona Ctrl+C para detener el servidor"
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Set-Location "$rootDir\Mage.API"
Write-Host "Iniciando servidor en puerto $port..." -ForegroundColor Cyan
& mvn exec:java "-Dxmage.config.path=..\Mage.Server\config\config.xml" "-Dxmage.api.port=$port"

