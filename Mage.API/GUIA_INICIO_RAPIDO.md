# Guía de Inicio Rápido - MAGE REST API

Esta guía te ayudará a compilar y ejecutar el servidor REST API de MAGE, incluso si nunca has usado Java.

## Requisitos Previos

### 1. Instalar Java JDK 8 o superior

**Windows:**
1. Descarga Java JDK 8 desde: https://adoptium.net/ (busca "Temurin 8" o versión superior)
2. Instala el JDK
3. Verifica la instalación abriendo PowerShell/CMD y ejecutando:
   ```powershell
   java -version
   ```
   Deberías ver algo como: `java version "1.8.0_xxx"`

### 2. Instalar Maven

**Windows:**
1. Descarga Maven desde: https://maven.apache.org/download.cgi
2. Descarga el archivo `apache-maven-x.x.x-bin.zip`
3. Extrae el ZIP en una carpeta (ej: `C:\Program Files\Apache\maven`)
4. Agrega Maven al PATH:
   - Presiona `Windows + R`, escribe `sysdm.cpl` y presiona Enter
   - Ve a la pestaña "Opciones avanzadas"
   - Clic en "Variables de entorno"
   - En "Variables del sistema", busca `Path` y haz clic en "Editar"
   - Clic en "Nuevo" y agrega la ruta: `C:\Program Files\Apache\maven\bin`
   - Acepta todos los cuadros de diálogo
5. Abre una NUEVA ventana de PowerShell/CMD y verifica:
   ```powershell
   mvn -version
   ```
   Deberías ver información sobre Maven

**Alternativa rápida (si tienes Chocolatey):**
```powershell
choco install maven
```

## Pasos para Compilar y Ejecutar

### Paso 1: Compilar el Proyecto

1. Abre PowerShell o CMD
2. Navega al directorio del proyecto:
   ```powershell
   cd "C:\Users\taken\Motor MTG\server_xmage"
   ```
3. Compila todo el proyecto (esto puede tomar varios minutos la primera vez):
   ```powershell
   mvn clean install -DskipTests
   ```
   
   **Nota:** El flag `-DskipTests` omite las pruebas para compilar más rápido. Si quieres ejecutar las pruebas, omítelo.
   
   La primera vez que compiles, Maven descargará muchas dependencias (puede tardar 10-20 minutos).

### Paso 2: Ejecutar el Servidor

Una vez compilado exitosamente, ejecuta el servidor:

```powershell
cd Mage.API
mvn exec:java
```

O si quieres ejecutar directamente desde el JAR compilado:

```powershell
cd Mage.API\target
java -cp "mage-api.jar;../Mage/target/*;../Mage.Common/target/*;../Mage.Server/target/*;../Mage.Sets/target/*" mage.api.Main
```

### Paso 3: Verificar que el Servidor Está Corriendo

Deberías ver en la consola algo como:
```
INFO: Starting MAGE REST API SERVER version: 1.4.58
INFO: Started MAGE REST API server - listening on http://localhost:8080
INFO: REST API available at http://localhost:8080/api
```

## Configuración del Servidor

### Puerto y Host

Puedes cambiar el puerto y host usando propiedades del sistema:

```powershell
mvn exec:java -Dxmage.api.port=9090 -Dxmage.api.host=0.0.0.0
```

Esto iniciará el servidor en el puerto 9090 y escuchará en todas las interfaces de red.

### Configuración del Juego

El servidor usa el mismo archivo de configuración que el servidor original:
- Ruta por defecto: `Mage.Server/config/config.xml`
- Puedes cambiarla con: `-Dxmage.config.path=ruta/a/config.xml`

### Clave Secreta JWT

Por defecto usa una clave insegura. Para producción, configura:
```powershell
mvn exec:java -Djwt.secret=tu-clave-secreta-muy-segura-aqui
```

## Probar la API con Postman

### 1. Importar la Colección

1. Abre Postman
2. Clic en "Import" (esquina superior izquierda)
3. Selecciona el archivo: `MAGE_REST_API.postman_collection.json`
4. La colección se importará con todas las variables configuradas

### 2. Configurar Variables

La colección ya tiene variables predefinidas:
- `baseUrl`: `http://localhost:8080` (ajusta si cambiaste el puerto)
- `token`: Se llena automáticamente al hacer login
- `sessionId`: Se llena automáticamente al hacer login
- `roomId`: Se llena automáticamente al obtener la sala principal
- `tableId`: Se llena automáticamente al listar mesas

### 3. Flujo de Prueba Recomendado

1. **Primero: Registro o Login**
   - Ejecuta "Autenticación > Registro" para crear un usuario
   - O ejecuta "Autenticación > Login" si ya tienes un usuario
   - El token se guardará automáticamente en las variables

2. **Segundo: Obtener Sala Principal**
   - Ejecuta "Salas > Obtener Sala Principal"
   - El `roomId` se guardará automáticamente

3. **Tercero: Listar Mesas**
   - Ejecuta "Salas > Listar Mesas de Sala"
   - Si hay mesas, el `tableId` se guardará automáticamente

4. **Cuarto: Probar Otros Endpoints**
   - Ahora puedes probar los demás endpoints usando las variables guardadas

### 4. Endpoints que Necesitan Datos Específicos

Algunos endpoints necesitan objetos complejos de MAGE:
- **Crear Mesa**: Necesita un objeto `MatchOptions` completo
- **Unirse a Mesa**: Necesita `DeckCardLists` si quieres enviar un mazo
- **Enviar Acción**: Necesita saber qué acciones están disponibles

Para estos casos, es mejor consultar el código fuente o la documentación de MAGE para ver la estructura exacta de los objetos.

## Solución de Problemas

### Error: "No se encuentra java"
- Asegúrate de que Java esté instalado y en el PATH
- Verifica con `java -version`

### Error: "No se encuentra mvn"
- Asegúrate de que Maven esté instalado y en el PATH
- Verifica con `mvn -version`
- Puede que necesites cerrar y reabrir la terminal

### Error: "Failed to start REST API server"
- Verifica que el puerto 8080 no esté en uso
- Revisa los logs para ver el error específico
- Asegúrate de que `config.xml` exista en `Mage.Server/config/`

### Error: "ClassNotFoundException" o errores de compilación
- Limpia y recompila: `mvn clean install -DskipTests`
- Verifica que todas las dependencias se descargaron correctamente

### El servidor no responde
- Verifica que el servidor esté corriendo (deberías ver los logs)
- Prueba abrir en el navegador: `http://localhost:8080/api/rooms/main` (sin autenticación fallará, pero confirma que el servidor está activo)

## Estructura de Archivos Importante

```
server_xmage/
├── Mage.API/                    # Nuevo módulo REST API
│   ├── src/main/java/mage/api/  # Código fuente
│   ├── MAGE_REST_API.postman_collection.json  # Colección Postman
│   └── GUIA_INICIO_RAPIDO.md    # Esta guía
├── Mage.Server/
│   └── config/
│       └── config.xml           # Configuración del servidor
└── pom.xml                      # Configuración Maven principal
```

## Próximos Pasos

1. Familiarízate con los endpoints básicos (login, obtener salas, listar mesas)
2. Revisa `API_DOCUMENTATION.md` para detalles completos de cada endpoint
3. Explora cómo crear y unirte a mesas
4. Experimenta con las acciones del juego

¡Buena suerte con tu servidor MAGE REST API! 🎮

