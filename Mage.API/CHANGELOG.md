# Changelog
Registro de todos los cambios realizados en el proyecto Mage.API

---

## [v1.0.6] - 2025-12-16

### Corrección
- **Creación de mesas**: Se corrigió el endpoint `POST /api/rooms/{roomId}/tables` para resolver el error de deserialización de `MatchOptions`. Se creó el DTO `CreateTableRequest` que permite recibir los datos del JSON y construir `MatchOptions` manualmente, ya que la clase `MatchOptions` no tiene constructor sin parámetros. El endpoint ahora acepta un JSON con los campos: `name`, `gameType`, `deckType`, `limited`, `multiPlayer`, `password`, `winsNeeded`, `freeMulligans`.
- **Unirse a mesas**: Se mejoró el endpoint `POST /api/tables/{tableId}/join` con:
  - Validaciones previas del estado de la mesa antes de intentar unirse
  - Validación de que el deck no sea null para mesas no-limited
  - Validación del estado de la mesa (debe estar en WAITING)
  - Carga previa del deck para capturar errores de formato antes de intentar unirse
  - Validaciones adicionales para `name` y `playerType`
  - Manejo de errores mejorado con mensajes más descriptivos y específicos
  - Logging detallado con información de la mesa (estado, asientos, tipo de deck)
  - Manejo de excepciones más robusto

---

## [v1.0.5] - 2025-12-16

### Mejora
- **Configuración de puerto**: Los scripts `start-server.ps1` y `ejecutar-sin-compilar.ps1` ahora soportan configurar el puerto del servidor mediante la variable de entorno `XMAGE_API_PORT`. Si no se especifica, usa el puerto 8080 por defecto. Ejemplo: `$env:XMAGE_API_PORT="8081"; .\start-server.ps1`

### Configuración
- **Autenticación activada**: Se activó la autenticación en `config.xml` (`authenticationActivated="true"`). Ahora los usuarios deben registrarse y autenticarse antes de usar los endpoints protegidos de la API REST.

### Corrección
- **Registro de usuarios en API REST**: Se corrigió el endpoint `/api/auth/register` para que:
  - Use la contraseña proporcionada por el usuario (en lugar de generar una automáticamente)
  - No requiera configuración de email para registrar usuarios
  - Incluya validaciones completas de username, password y email
  - Permita login inmediato después del registro usando la contraseña proporcionada
  - Retorne mensajes de error descriptivos en caso de validación fallida

---

## [v1.0.4] - 2025-12-16

### Corrección
- **Dependencias JAX-RS**: Se corrigió el error `NoSuchMethodError: javax.ws.rs.core.Application.getProperties()` mediante:
  - Reordenamiento de dependencias: JAX-RS API 2.1.1 y Jersey 2.40 ahora se declaran PRIMERO en el pom.xml
  - Exclusiones explícitas: Se agregaron exclusions en las dependencias mage-server, mage y mage-common para evitar que traigan versiones antiguas conflictivas de javax.ws.rs-api y Jersey
  - Dependencia adicional: Se agregó jersey-common 2.40 para completar el stack de Jersey
  - Esto resuelve el conflicto de dependencias transitivas que impedía el inicio correcto del servidor con Java 21
- **Script start-server.ps1**: Se modificó para compilar solo el módulo Mage.API y sus dependencias (`-pl Mage.API -am`) en lugar de todo el proyecto, evitando fallos por módulos no relacionados (como mage-verify).

---

## [v1.0.3] - 2025-12-16

### Documentación
- **Formatos de Postman**: Se creó el documento `POSTMAN_FORMATOS_IMPORTACION.md` que documenta todos los formatos y métodos de importación soportados por Postman, incluyendo colecciones nativas, especificaciones OpenAPI/Swagger, formatos de otros clientes API (Insomnia, SoapUI, Hoppscotch, Thunder Client), comandos cURL, importación desde Git, y más.

### Corrección
- **Colección de Postman**: Se corrigió el archivo `MAGE_REST_API.postman_collection.json` para cumplir completamente con el estándar Postman Collection v2.1.0:
  - Se estandarizó el formato de todos los objetos `url` (host y path en formato multilínea consistente)
  - Se agregó el campo `type: "text"` a todos los headers que no lo tenían (Authorization y Content-Type) para consistencia
  - Se corrigió el escape de caracteres en expresiones regulares dentro de scripts de test
  - El archivo ahora es completamente compatible con el estándar y puede importarse sin problemas en Postman

---

## [v1.0.2] - 2025-12-16

### Corrección
- **Scripts de ejecución**: Se corrigió la ruta del archivo de configuración en `start-server.ps1` y `ejecutar-sin-compilar.ps1` para apuntar correctamente a `Mage.Server\config\config.xml`.

---

## [v1.0.1] - 2025-12-16

### Corrección
- **RestCallbackHandler**: Se agregó el método faltante `handleCallback(Callback callback)` requerido por la interfaz `InvokerCallbackHandler` para corregir error de compilación.

---

## [v1.0.0] - 2025-12-16

### Inicial
- Creación del archivo CHANGELOG para documentar todos los cambios del proyecto.

