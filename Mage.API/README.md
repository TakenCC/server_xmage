# MAGE REST API

API REST para el servidor MAGE de Magic: The Gathering Commander.

## Inicio Rápido

### Prerrequisitos
- Java JDK 8 o superior
- Maven 3.6 o superior

### Ejecutar el Servidor

**⚠️ IMPORTANTE: Si usas PowerShell (recomendado), usa el script `.ps1`**

**Opción 1: Script PowerShell (Recomendado)**
```powershell
cd "C:\Users\taken\Motor MTG\server_xmage\Mage.API"
.\start-server.ps1
```

**Opción 2: Script CMD/Batch**
```cmd
cd "C:\Users\taken\Motor MTG\server_xmage\Mage.API"
start-server.bat
```

**Opción 3: Desde Maven directamente**
```powershell
cd "C:\Users\taken\Motor MTG\server_xmage"
mvn clean install -DskipTests
cd Mage.API
mvn exec:java
```

**Opción 4: Ejecutar sin recompilar (si ya compilaste antes)**
```powershell
.\ejecutar-sin-compilar.ps1
```

**Opción 5: Puerto personalizado**
```powershell
mvn exec:java -Dxmage.api.port=9090
```

### Nota sobre compilación

- **Primera vez**: Puede tardar 15-30 minutos (descarga dependencias)
- **Siguientes veces**: 2-5 minutos normalmente
- **Para acelerar**: Los scripts usan compilación paralela (`-T 4`)
- **Consulta**: `ACELERAR_COMPILACION.md` para más opciones

### Probar la API

1. Importa `MAGE_REST_API.postman_collection.json` en Postman
2. Ejecuta "Autenticación > Login" o "Autenticación > Registro"
3. El token JWT se guardará automáticamente
4. Explora los demás endpoints

## Documentación

- `GUIA_INICIO_RAPIDO.md` - Guía completa paso a paso para principiantes
- `API_DOCUMENTATION.md` - Documentación completa de todos los endpoints
- `POSTMAN_FORMATOS_IMPORTACION.md` - Formatos y métodos de importación soportados por Postman
- `INSTALAR_MAVEN_WINDOWS.md` - Cómo instalar Maven en Windows
- `COMANDOS_RAPIDOS.md` - Referencia rápida de comandos

## Configuración

El servidor usa el mismo archivo de configuración que el servidor original:
- `../Mage.Server/config/config.xml`

### Variables de Sistema

- `xmage.config.path` - Ruta al archivo de configuración (default: `config/config.xml`)
- `xmage.api.port` - Puerto HTTP (default: `8080`)
- `xmage.api.host` - Host (default: `localhost`)
- `jwt.secret` - Clave secreta para JWT (default: `mage-secret-key-change-in-production`)

## Endpoints Principales

### Autenticación
- `POST /api/auth/login` - Iniciar sesión
- `POST /api/auth/register` - Registro de usuario
- `POST /api/auth/reset-password` - Restablecer contraseña

### Salas
- `GET /api/rooms/main` - Obtener sala principal
- `GET /api/rooms/{id}/tables` - Listar mesas
- `POST /api/rooms/{id}/tables` - Crear mesa

### Mesas
- `GET /api/tables/{id}` - Detalles de mesa
- `POST /api/tables/{id}/join` - Unirse a mesa
- `POST /api/tables/{id}/watch` - Observar mesa

### Juegos
- `GET /api/games/{id}` - Estado del juego
- `POST /api/games/{id}/actions` - Enviar acción
- `POST /api/games/{id}/watch` - Observar juego

## Notas

- Los tokens JWT expiran después de 24 horas
- Todos los endpoints (excepto `/api/auth/*`) requieren autenticación
- Usa el header `Authorization: Bearer <token>` para autenticarte
- Si tienes problemas con el script .bat desde PowerShell, usa el script .ps1
