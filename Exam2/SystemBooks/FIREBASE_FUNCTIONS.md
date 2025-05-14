# Guía de Implementación de Cloud Functions para FCM

Este documento describe cómo implementar las Cloud Functions necesarias para el sistema de notificaciones de SystemBooks.

## Requisitos previos

1. Node.js instalado
2. Firebase CLI instalado (`npm install -g firebase-tools`)
3. Un proyecto de Firebase con FCM habilitado

## Pasos para configurar Firebase Functions

### 1. Inicializar Firebase Functions en tu proyecto

```bash
# Navegar al directorio del proyecto
cd /path/to/project

# Iniciar sesión en Firebase
firebase login

# Inicializar Firebase Functions (selecciona JavaScript y ESLint)
firebase init functions
```

### 2. Implementación de la función para enviar notificaciones

Edita el archivo `functions/index.js` e implementa la siguiente función:

```javascript
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

// Función que escucha cambios en la colección de notificaciones
exports.sendNotification = functions.firestore
    .document('notifications/{notificationId}')
    .onCreate(async (snap, context) => {
        try {
            const notification = snap.data();
            
            if (!notification) {
                console.log('No notification data found');
                return null;
            }
            
            const title = notification.title || 'SystemBooks';
            const body = notification.body || '';
            const data = notification.data || {};
            
            // Si es para todos los usuarios
            if (notification.toAll) {
                console.log('Sending notification to all users');
                
                // Obtener todos los tokens de FCM
                const tokensSnapshot = await admin.firestore()
                    .collection('fcm_tokens')
                    .get();
                    
                if (tokensSnapshot.empty) {
                    console.log('No devices found');
                    return null;
                }
                
                // Extraer los tokens
                const tokens = [];
                tokensSnapshot.forEach(doc => {
                    const token = doc.data().token;
                    if (token) tokens.push(token);
                });
                
                if (tokens.length === 0) {
                    console.log('No tokens found');
                    return null;
                }
                
                // Preparar mensaje
                const message = {
                    notification: { title, body },
                    data: data,
                    tokens: tokens
                };
                
                // Enviar la notificación en grupos de 500 tokens (límite de FCM)
                const tokensChunks = chunkArray(tokens, 500);
                const messagesPromises = tokensChunks.map(tokensChunk => {
                    const chunkMessage = { ...message, tokens: tokensChunk };
                    return admin.messaging().sendMulticast(chunkMessage);
                });
                
                // Esperar a que todas las notificaciones se envíen
                const results = await Promise.all(messagesPromises);
                
                // Reportar resultados
                let successCount = 0;
                let failureCount = 0;
                
                results.forEach(result => {
                    successCount += result.successCount;
                    failureCount += result.failureCount;
                });
                
                console.log(`Successfully sent notifications to ${successCount} devices`);
                if (failureCount > 0) {
                    console.log(`Failed to send notifications to ${failureCount} devices`);
                }
                
                return { success: true, successCount, failureCount };
            } 
            // Si es para un usuario específico
            else if (notification.targetUserId) {
                console.log(`Sending notification to user: ${notification.targetUserId}`);
                
                // Obtener el token del usuario
                const tokenDoc = await admin.firestore()
                    .collection('fcm_tokens')
                    .doc(notification.targetUserId)
                    .get();
                
                if (!tokenDoc.exists || !tokenDoc.data().token) {
                    console.log('User has no FCM token');
                    return { success: false, error: 'No FCM token found for user' };
                }
                
                const token = tokenDoc.data().token;
                
                // Preparar mensaje
                const message = {
                    notification: { title, body },
                    data: data,
                    token: token
                };
                
                // Enviar la notificación
                const result = await admin.messaging().send(message);
                console.log('Successfully sent message:', result);
                
                return { success: true };
            }
            
            return { success: false, error: 'Invalid notification type' };
        } catch (error) {
            console.error('Error sending notification:', error);
            return { success: false, error: error.message };
        }
    });

// Función para dividir un array en chunks (para enviar notificaciones en lotes)
function chunkArray(array, size) {
    const chunks = [];
    for (let i = 0; i < array.length; i += size) {
        chunks.push(array.slice(i, i + size));
    }
    return chunks;
}
```

### 3. Desplegar la función

```bash
firebase deploy --only functions
```

### 4. Configuración adicional

- Asegúrate de que tu proyecto tenga Firebase Cloud Messaging habilitado
- Verifica que la autenticación esté configurada correctamente
- Otorga los permisos necesarios para Firestore

## Testing

Para probar la función localmente antes de desplegarla:

```bash
firebase emulators:start --only functions,firestore
```

Luego puedes crear un documento en la colección `notifications` con los campos adecuados:

- Para todos los usuarios:
  ```json
  {
    "title": "Título de prueba",
    "body": "Cuerpo del mensaje",
    "toAll": true,
    "timestamp": 1621234567890,
    "data": {
      "type": "test",
      "action": "open_app"
    }
  }
  ```

- Para un usuario específico:
  ```json
  {
    "title": "Título de prueba",
    "body": "Cuerpo del mensaje",
    "targetUserId": "usuario_uid_aqui",
    "timestamp": 1621234567890,
    "data": {
      "type": "test",
      "action": "open_app"
    }
  }
  ```
