# Changelog

Todos los cambios notables en este proyecto serán documentados en este archivo.

## [1.4.59] - 2024-XX-XX

### Agregado
- **Nuevo módulo Mage.API**: Sistema de API REST completo con autenticación JWT
  - Endpoints de autenticación (login, registro, reset de contraseña)
  - Endpoints para gestión de salas (rooms)
  - Endpoints para gestión de mesas (tables)
  - Endpoints para gestión de juegos (games)
  - Filtro de autenticación JWT para proteger endpoints
  - Soporte CORS para aplicaciones web
  - Integración con el sistema de sesiones existente
- **Autenticación JWT**: Sistema de autenticación basado en tokens JSON Web Tokens
  - Generación y validación de tokens JWT
  - Tokens con expiración de 24 horas
  - Integración con el sistema de autenticación existente (Apache Shiro)
- **Servidor HTTP Grizzly**: Servidor HTTP embebido usando Grizzly y Jersey
  - Puerto configurable (default: 8080)
  - Host configurable (default: localhost)
  - Soporte para inicialización completa del servidor MAGE

### Cambiado
- **Arquitectura de comunicación**: Migración de JBoss Remoting a REST API
  - Eliminada dependencia de JBoss Remoting para la API REST
  - Mantenida compatibilidad con la lógica de negocio existente
  - Nueva capa de abstracción para comunicación REST

### Eliminado
- **Módulo Mage.Client**: Removido del proyecto (preparado para interfaz web separada)
  - Módulo eliminado del pom.xml raíz
  - Código cliente ya no es parte del build del servidor

### Técnico
- Agregadas dependencias:
  - `jersey-container-grizzly2-http` (v2.40)
  - `jersey-hk2` (v2.40)
  - `jersey-media-json-jackson` (v2.40)
  - `java-jwt` (v4.4.0) para autenticación JWT
- Nueva estructura de paquetes:
  - `mage.api.config`: Configuración de la API (JAX-RS, filtros)
  - `mage.api.dto`: Data Transfer Objects para requests/responses
  - `mage.api.resources`: Recursos REST (endpoints)
  - `mage.api.service`: Servicios de negocio (JWT, autenticación)
  - `mage.api.util`: Utilidades (callback handler para REST)
- Nuevo punto de entrada: `mage.api.Main` para iniciar el servidor REST

### Notas
- El servidor REST mantiene toda la lógica de negocio del servidor original
- Los callbacks push no están disponibles en REST; se requiere polling para actualizaciones
- La configuración del servidor se mantiene igual (mismo `config.xml`)
- El sistema de autenticación existente se mantiene intacto para validación de credenciales

