# API Documentation

## Hello Endpoint

**URL:** `http://localhost:8080/api/hello`

**Method:** GET

**Response Format:** JSON

**Example Response:**
```json
{
  "message": "¡Hola Mundo desde Spring Boot!",
  "timestamp": 1686145200000
}
```

**Fields:**
- `message`: A greeting message string
- `timestamp`: Unix timestamp (milliseconds) when the response was generated

**Error Handling:**
The API will return standard HTTP status codes:
- 200 OK: Request was successful
- 500 Internal Server Error: Server-side error occurred

## Android Connection:
For Android Emulator, use `10.0.2.2` instead of `localhost` to connect to your host machine:
`http://10.0.2.2:8080/api/hello`
