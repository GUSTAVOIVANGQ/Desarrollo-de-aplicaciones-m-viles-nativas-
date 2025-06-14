# Índices de Firestore Requeridos

Para que el sistema de amigos funcione correctamente, necesitas configurar los siguientes índices compuestos en Firestore:

## 1. Índice para Solicitudes de Amistad Recibidas

**Colección:** `friend_requests`

**Campos:**
- `receiverId` (Ascending)
- `status` (Ascending)
- `createdAt` (Descending) - *Opcional para ordenamiento*

## 2. Índice para Solicitudes de Amistad Enviadas

**Colección:** `friend_requests`

**Campos:**
- `senderId` (Ascending)
- `status` (Ascending)
- `createdAt` (Descending) - *Opcional para ordenamiento*

## Cómo Configurar los Índices

### Opción 1: Automáticamente (Recomendado)

1. Ejecuta la aplicación y navega a la sección "Amigos"
2. Firestore detectará automáticamente las consultas que requieren índices
3. En la consola de Firebase, verás errores que incluyen enlaces para crear los índices automáticamente
4. Haz clic en los enlaces para crear los índices

### Opción 2: Manualmente

1. Ve a la [Consola de Firebase](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a Firestore Database > Índices
4. Haz clic en "Crear índice"
5. Configura cada índice con los campos mencionados arriba

### Opción 3: Usando Firebase CLI

Crea un archivo `firestore.indexes.json` en la raíz del proyecto:

```json
{
  "indexes": [
    {
      "collectionGroup": "friend_requests",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "receiverId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "status",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "createdAt",
          "order": "DESCENDING"
        }
      ]
    },
    {
      "collectionGroup": "friend_requests",
      "queryScope": "COLLECTION",
      "fields": [
        {
          "fieldPath": "senderId",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "status",
          "order": "ASCENDING"
        },
        {
          "fieldPath": "createdAt",
          "order": "DESCENDING"
        }
      ]
    }
  ]
}
```

Luego ejecuta:
```bash
firebase deploy --only firestore:indexes
```

## Verificación

Después de configurar los índices:

1. Espera unos minutos para que se construyan
2. Reinicia la aplicación
3. Navega a la sección "Amigos"
4. Las listas deberían mostrarse correctamente

## Problemas Comunes

- **Los índices tardan en construirse**: Pueden tomar varios minutos dependiendo de la cantidad de datos
- **Error "FAILED_PRECONDITION"**: Indica que falta un índice requerido
- **Las consultas fallan silenciosamente**: Revisa los logs de Android para ver errores de Firestore

## Logs de Debug

Para verificar que todo funciona, revisa los logs de Android con el filtro:
```
tag:FriendshipRepository OR tag:FriendsFragment
```
