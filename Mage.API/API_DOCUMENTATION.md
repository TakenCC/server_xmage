# MAGE REST API Documentation

## Overview

Este documento describe la API REST del servidor MAGE para MTG Commander. La API proporciona endpoints para autenticación, gestión de salas, mesas, juegos y más.

## Autenticación

La API utiliza JWT (JSON Web Tokens) para autenticación. Todos los endpoints (excepto `/api/auth/*`) requieren un token JWT válido en el header `Authorization`.

### Formato del Header
```
Authorization: Bearer <token>
```

## Endpoints

### Autenticación

#### POST /api/auth/login
Inicia sesión y obtiene un token JWT.

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response 200 OK:**
```json
{
  "token": "jwt_token_string",
  "username": "string",
  "sessionId": "string"
}
```

**Response 401 Unauthorized:**
```json
{
  "error": "UNAUTHORIZED",
  "message": "Invalid username or password"
}
```

#### POST /api/auth/register
Registra un nuevo usuario.

**Request Body:**
```json
{
  "username": "string",
  "password": "string",
  "email": "string"
}
```

**Response 200 OK:**
```json
{
  "token": "jwt_token_string",
  "username": "string",
  "sessionId": "string"
}
```

#### POST /api/auth/send-reset-token
Envía un token de recuperación de contraseña por email.

**Form Parameters:**
- `email`: string

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Reset token sent to email"
}
```

#### POST /api/auth/reset-password
Restablece la contraseña usando un token.

**Form Parameters:**
- `email`: string
- `authToken`: string
- `newPassword`: string

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Password reset successfully"
}
```

### Salas (Rooms)

#### GET /api/rooms/main
Obtiene el ID de la sala principal.

**Response 200 OK:**
```
"uuid-string"
```

#### GET /api/rooms/{roomId}/users
Obtiene la lista de usuarios en una sala.

**Response 200 OK:**
```json
[
  {
    // RoomUsersView object
  }
]
```

#### GET /api/rooms/{roomId}/tables
Obtiene todas las mesas en una sala.

**Response 200 OK:**
```json
[
  {
    // TableView object
  }
]
```

#### POST /api/rooms/{roomId}/tables
Crea una nueva mesa.

**Request Body:**
```json
{
  // MatchOptions object
}
```

**Response 201 Created:**
```json
{
  // TableView object
}
```

#### POST /api/rooms/{roomId}/tournaments
Crea un nuevo torneo.

**Request Body:**
```json
{
  // TournamentOptions object
}
```

**Response 201 Created:**
```json
{
  // TableView object
}
```

#### GET /api/rooms/{roomId}/finished-matches
Obtiene las partidas finalizadas en una sala.

**Response 200 OK:**
```json
[
  {
    // MatchView object
  }
]
```

### Mesas (Tables)

#### GET /api/tables/{tableId}?roomId={roomId}
Obtiene los detalles de una mesa.

**Response 200 OK:**
```json
{
  // TableView object
}
```

#### POST /api/tables/{tableId}/join?roomId={roomId}
Se une a una mesa.

**Request Body:**
```json
{
  "name": "string",
  "playerType": "HUMAN|AI|...",
  "skill": 0,
  "deckList": {
    // DeckCardLists object
  },
  "password": "string"
}
```

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Successfully joined table"
}
```

#### POST /api/tables/{tableId}/watch?roomId={roomId}
Observa una mesa.

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Successfully watching table"
}
```

#### DELETE /api/tables/{tableId}?roomId={roomId}
Elimina una mesa (solo si eres el dueño).

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Table removed successfully"
}
```

#### GET /api/tables/{tableId}/is-owner?roomId={roomId}
Verifica si el usuario es dueño de la mesa.

**Response 200 OK:**
```
true|false
```

#### POST /api/tables/{tableId}/deck
Envía un mazo a una mesa.

**Request Body:**
```json
{
  // DeckCardLists object
}
```

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Deck submitted successfully"
}
```

#### PUT /api/tables/{tableId}/deck
Guarda un mazo.

**Request Body:**
```json
{
  // DeckCardLists object
}
```

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Deck saved successfully"
}
```

### Juegos (Games)

#### GET /api/games/{gameId}?playerId={playerId}
Obtiene el estado de un juego.

**Response 200 OK:**
```json
{
  // GameView object
}
```

#### POST /api/games/{gameId}/join
Se une a un juego.

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Successfully joined game"
}
```

#### POST /api/games/{gameId}/watch
Observa un juego.

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Successfully watching game"
}
```

#### DELETE /api/games/{gameId}/watch
Deja de observar un juego.

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Stopped watching game"
}
```

#### POST /api/games/{gameId}/actions
Envía una acción del jugador.

**Request Body:**
```json
{
  "playerAction": "PASS|CONCEDE|...",
  "data": {},
  "playerId": "uuid-string"
}
```

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Action sent successfully"
}
```

#### POST /api/games/{gameId}/quit
Abandona un juego.

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Quit game successfully"
}
```

#### POST /api/games/{gameId}/start?roomId={roomId}&tableId={tableId}
Inicia una partida.

**Response 200 OK:**
```json
{
  "error": "SUCCESS",
  "message": "Match started successfully"
}
```

## Códigos de Error

- `200 OK`: Operación exitosa
- `201 Created`: Recurso creado exitosamente
- `400 BAD_REQUEST`: Solicitud inválida
- `401 UNAUTHORIZED`: No autorizado (token faltante o inválido)
- `404 NOT_FOUND`: Recurso no encontrado
- `500 INTERNAL_SERVER_ERROR`: Error interno del servidor

## Notas de Implementación

- Los tokens JWT expiran después de 24 horas
- La sesión se crea automáticamente al hacer login
- Los callbacks push del sistema original no están disponibles en REST; se requiere polling para actualizaciones en tiempo real
- El servidor debe estar configurado con el mismo `config.xml` que el servidor original

## Configuración del Servidor

El servidor REST API puede configurarse mediante propiedades del sistema:

- `xmage.config.path`: Ruta al archivo de configuración (default: `config/config.xml`)
- `xmage.api.port`: Puerto HTTP del servidor (default: `8080`)
- `xmage.api.host`: Host del servidor (default: `localhost`)
- `jwt.secret`: Clave secreta para JWT (default: `mage-secret-key-change-in-production`)

## Ejemplo de Uso

```bash
# 1. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'

# Response: {"token":"eyJ...","username":"testuser","sessionId":"..."}

# 2. Obtener salas (usando el token)
curl -X GET http://localhost:8080/api/rooms/main \
  -H "Authorization: Bearer eyJ..."

# 3. Crear mesa
curl -X POST http://localhost:8080/api/rooms/{roomId}/tables \
  -H "Authorization: Bearer eyJ..." \
  -H "Content-Type: application/json" \
  -d '{...MatchOptions...}'
```

