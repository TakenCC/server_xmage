@echo off
REM Script para iniciar el servidor MAGE REST API
REM Para Windows

echo ========================================
echo   MAGE REST API Server
echo ========================================
echo.

REM Verificar que Maven esté instalado
echo Verificando Maven...
call mvn -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERROR] Maven no encontrado. Por favor instala Maven primero.
    echo.
    echo Consulta el archivo INSTALAR_MAVEN_WINDOWS.md para instrucciones detalladas.
    echo O visita: https://maven.apache.org/download.cgi
    echo.
    pause
    exit /b 1
)
echo Maven encontrado! ✅
echo.

REM Verificar que Java esté instalado
echo Verificando Java...
call java -version >nul 2>&1
if errorlevel 1 (
    echo.
    echo [ERROR] Java no encontrado. Por favor instala Java JDK 8 o superior.
    echo.
    echo Descarga Java desde: https://adoptium.net/
    echo.
    pause
    exit /b 1
)
echo Java encontrado! ✅
echo.

echo Verificando compilacion...
echo (Esto puede tardar varios minutos la primera vez mientras descarga dependencias)
cd ..
mvn clean install -DskipTests -T 4 >nul 2>&1
if errorlevel 1 (
    echo.
    echo ERROR: Fallo en la compilacion. Ejecutando compilacion con salida completa...
    echo.
    mvn clean install -DskipTests
    pause
    exit /b 1
)

echo Compilacion exitosa!
echo.
echo Iniciando servidor...
echo El servidor estara disponible en: http://localhost:8080
echo Presiona Ctrl+C para detener el servidor
echo.
echo ========================================
echo.

cd Mage.API
mvn exec:java

