# Comandos Rápidos - MAGE REST API

## Verificar Instalación

### Verificar Java
```powershell
java -version
```
**Deberías ver**: `java version "1.8.0_xxx"` o superior

### Verificar Maven
```powershell
mvn -version
```
**Deberías ver**: `Apache Maven 3.x.x`

Si alguno falla, consulta:
- **Java**: https://adoptium.net/
- **Maven**: Ver archivo `INSTALAR_MAVEN_WINDOWS.md`

---

## Compilar el Proyecto

```powershell
cd "C:\Users\taken\Motor MTG\server_xmage"
mvn clean install -DskipTests
```

**Tiempo estimado**: 10-20 minutos la primera vez (descarga dependencias)

---

## Ejecutar el Servidor

### Opción 1: Usando el script (una vez Maven esté instalado)
```powershell
cd "C:\Users\taken\Motor MTG\server_xmage\Mage.API"
.\start-server.bat
```

### Opción 2: Manualmente
```powershell
cd "C:\Users\taken\Motor MTG\server_xmage\Mage.API"
mvn exec:java
```

### Opción 3: Puerto personalizado
```powershell
mvn exec:java -Dxmage.api.port=9090
```

**El servidor estará en**: `http://localhost:8080`

---

## Probar la API

1. Importa `MAGE_REST_API.postman_collection.json` en Postman
2. Ejecuta "Autenticación > Login" o "Autenticación > Registro"
3. El token se guardará automáticamente
4. Prueba los demás endpoints

---

## Problemas Comunes

### "mvn no se reconoce como comando"
- Maven no está instalado o no está en el PATH
- Consulta `INSTALAR_MAVEN_WINDOWS.md`

### "java no se reconoce como comando"
- Java no está instalado
- Descarga desde: https://adoptium.net/

### "Failed to start REST API server"
- Verifica que el puerto 8080 no esté en uso
- Revisa los logs para ver el error específico

---

## Estructura de Comandos Maven

```powershell
mvn clean              # Limpiar archivos compilados
mvn compile            # Compilar el proyecto
mvn test               # Ejecutar pruebas
mvn package            # Crear el JAR
mvn install            # Instalar en repositorio local
mvn clean install      # Limpiar, compilar e instalar (lo que usamos)
```

