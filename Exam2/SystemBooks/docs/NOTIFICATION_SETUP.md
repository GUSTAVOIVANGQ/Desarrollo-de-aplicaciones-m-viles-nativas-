# SystemBooks Notification System Setup

Este documento explica cómo configurar y desplegar correctamente el sistema de notificaciones de SystemBooks para garantizar la entrega inmediata de notificaciones push.

## Mejoramientos Implementados

Hemos realizado las siguientes mejoras para optimizar la entrega de notificaciones:

1. **Firebase Cloud Messaging (FCM)**:
   - Configurado AndroidManifest.xml con `directBootAware="true"` para mejor inicio
   - Añadido metadatos para canales y prioridad alta
   - Optimizado manejo de tokens FCM

2. **Mejoras de Código**:
   - Implementado sistema de reintento con backoff exponencial
   - Añadido almacenamiento local de tokens para usuarios no autenticados
   - Mejorado procesamiento de tokens al iniciar sesión
   - Optimizado envío con prioridad alta

3. **Cloud Functions**:
   - Mejorado rendimiento con más memoria y tiempo de ejecución
   - Implementado sistema para limpiar tokens inválidos
   - Añadida alta prioridad para entrega inmediata

## Instrucciones de Despliegue

### Requisitos Previos

- Node.js versión 18 o superior
- Firebase CLI instalado (`npm install -g firebase-tools`)
- Acceso al proyecto Firebase

### Pasos para Desplegar Cloud Functions

1. **Instalar Firebase CLI** (si no está instalado):
   ```bash
   npm install -g firebase-tools
   ```

2. **Autenticarse en Firebase**:
   ```bash
   firebase login
   ```

3. **Seleccionar el proyecto**:
   ```bash
   firebase use [ID-DEL-PROYECTO]
   firebase use system-books
   ```

4. **Instalar dependencias**:
   ```bash
   cd functions
   npm install
   ```

5. **Desplegar las funciones**:
   ```bash
   firebase deploy --only functions
   ```

### Configuración adicional en la consola Firebase

1. **Verificar que el servicio FCM está habilitado**:
   - Ir a la consola de Firebase > Proyecto > Configuración del proyecto > Cloud Messaging
   - Asegurarse de que Firebase Cloud Messaging API está habilitada

2. **Verificar permisos IAM**:
   - Ir a IAM & Admin en Google Cloud Console
   - Asegurarse de que la cuenta de servicio Firebase tiene permisos adecuados

## Verificación

Después de desplegar, verificar que las notificaciones funcionan correctamente:

1. Enviar una notificación de prueba usando la aplicación
2. Comprobar en Firestore que el documento se crea en la colección `notifications`
3. Verificar en los logs de Cloud Functions que la función se ejecuta correctamente
4. Confirmar que la notificación se recibe inmediatamente en el dispositivo

## Solución de problemas

Si las notificaciones siguen llegando con retraso:

1. **Verificar tokens FCM**:
   ```java
   FirebaseManager.getInstance().refreshToken(true);
   ```

2. **Comprobar logs de Firebase Functions**:
   ```bash
   firebase functions:log
   ```

3. **Optimizar conexiones del dispositivo**:
   - Desactivar optimización de batería para la aplicación
   - Verificar que el dispositivo tiene buena conexión a internet
   - Comprobar en Ajustes > Aplicaciones > SystemBooks que las notificaciones están permitidas

4. **Reiniciar servicios**:
   ```bash
   firebase functions:delete sendNotification --force
   firebase deploy --only functions
   ```

Este documento forma parte de la documentación técnica del sistema SystemBooks.
