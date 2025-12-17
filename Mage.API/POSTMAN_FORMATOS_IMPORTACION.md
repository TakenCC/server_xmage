# Formatos de Importación de Postman

Este documento describe los formatos y métodos que Postman soporta para importar datos, según la [documentación oficial de Postman](https://learning.postman.com/docs/getting-started/importing-and-exporting/importing-and-exporting-overview/#importing-data-into-postman).

## Formatos Nativos de Postman

Postman puede importar directamente los siguientes formatos nativos:

### 1. Colecciones de Postman
- **Formato**: JSON (Collection v2.1.0 o superior)
- **Extensión**: `.postman_collection.json` o `.json`
- **Ejemplo en este proyecto**: `MAGE_REST_API.postman_collection.json`
- **Contenido**: Requests, folders, variables, scripts de pre-request y tests

### 2. Entornos de Postman
- **Formato**: JSON (Environment)
- **Extensión**: `.postman_environment.json` o `.json`
- **Contenido**: Variables de entorno con valores específicos

### 3. Data Dumps de Postman
- **Formato**: JSON
- **Contenido**: Exportación completa de colecciones, entornos, globals y header presets

## Formatos de Especificaciones de API

### 4. OpenAPI (Swagger)
- **Formatos soportados**: 
  - OpenAPI 3.0 (YAML o JSON)
  - Swagger 2.0 (YAML o JSON)
- **Extensión**: `.yaml`, `.yml`, `.json`
- **Uso**: Postman puede generar una colección completa desde una especificación OpenAPI

### 5. RAML
- **Formato**: YAML
- **Extensión**: `.raml`
- **Uso**: Importación de especificaciones RAML

### 6. GraphQL
- **Formato**: GraphQL Schema
- **Extensión**: `.graphql`, `.gql`
- **Uso**: Importación de esquemas GraphQL

## Formatos de Otros Clientes API

Postman puede importar colecciones desde otros clientes API populares:

### 7. Insomnia
- **Formato**: JSON (Insomnia Export)
- **Método**: Importar desde archivo de exportación de Insomnia

### 8. SoapUI
- **Formato**: XML (SoapUI Project)
- **Método**: Importar proyecto completo de SoapUI

### 9. Hoppscotch
- **Formato**: JSON (Hoppscotch Collection)
- **Método**: Importar colección exportada desde Hoppscotch

### 10. Thunder Client
- **Formato**: JSON (Thunder Client Collection)
- **Método**: Importar colección exportada desde Thunder Client

## Formatos de Comandos

### 11. cURL
- **Formato**: Comando cURL en texto plano
- **Método**: 
  - Pegar el comando cURL directamente en Postman
  - Importar desde archivo de texto
- **Ejemplo**:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'
```

## Importación desde Repositorios Git

### 12. Git Repository
Postman puede importar directamente desde:
- **GitHub**: Repositorios públicos o privados (con autenticación)
- **GitLab**: Repositorios públicos o privados
- **Bitbucket**: Repositorios públicos o privados
- **Azure DevOps**: Repositorios con autenticación

**Tipos de archivos soportados desde Git**:
- Colecciones de Postman (`.postman_collection.json`)
- Entornos de Postman (`.postman_environment.json`)
- Especificaciones OpenAPI/Swagger
- Archivos de configuración

## Importación desde Servicios

### 13. New Relic
- **Disponibilidad**: Solo planes gratuitos de Postman
- **Método**: Importar endpoints desde servicios de New Relic

## Métodos de Importación

Postman ofrece varias formas de importar datos:

### 1. Arrastrar y Soltar (Drag & Drop)
- Arrastra el archivo directamente a la ventana de Postman
- Postman detectará automáticamente el formato

### 2. Importar desde Archivo
- Clic en "Import" en la esquina superior izquierda
- Selecciona "File" y busca el archivo
- Postman detectará el formato automáticamente

### 3. Pegar Texto o URL
- Clic en "Import" → "Raw text"
- Pega el contenido JSON/YAML o una URL
- Postman procesará el contenido

### 4. Importar desde URL
- Clic en "Import" → "Link"
- Ingresa la URL del archivo
- Postman descargará e importará el contenido

### 5. Importar desde Git
- Clic en "Import" → "Git repository"
- Ingresa la URL del repositorio
- Selecciona el archivo a importar
- Autentica si es necesario

## Exportación desde Postman

Postman puede exportar en los siguientes formatos:

### Formatos de Exportación
- **Colección**: JSON (Collection v2.1.0 o v2.0)
- **Entorno**: JSON (Environment)
- **Data Dump**: JSON completo (colecciones, entornos, globals, header presets)

### Uso de Exportaciones
- Compartir colecciones con el equipo
- Versionar colecciones en Git
- Usar con Postman CLI o Newman para automatización
- Migrar a otros clientes API que soporten el formato

## Recomendaciones para este Proyecto

### Colección Actual
El proyecto incluye `MAGE_REST_API.postman_collection.json` que:
- ✅ Usa el formato Collection v2.1.0
- ✅ Incluye variables de colección
- ✅ Contiene scripts de test para guardar tokens automáticamente
- ✅ Está lista para importar directamente en Postman

### Mejoras Futuras Posibles
1. **Entorno de Postman**: Crear un archivo `.postman_environment.json` para diferentes ambientes (desarrollo, producción)
2. **OpenAPI Specification**: Generar una especificación OpenAPI desde la colección para documentación automática
3. **Versionado en Git**: Mantener la colección en el repositorio para versionado

## Referencias

- [Documentación Oficial de Postman - Importación y Exportación](https://learning.postman.com/docs/getting-started/importing-and-exporting/importing-and-exporting-overview/#importing-data-into-postman)
- [Postman Collection Format v2.1.0](https://schema.getpostman.com/json/collection/v2.1.0/docs/index.html)
- [Postman VS Code Extension](https://learning.postman.com/docs/developer-resources/vs-code-extension/) - También soporta importación

