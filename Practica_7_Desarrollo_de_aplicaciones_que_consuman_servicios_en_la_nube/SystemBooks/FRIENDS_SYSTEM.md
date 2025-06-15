# Sistema de Amigos - SystemBooks

## Descripción General

Este documento describe la implementación del sistema de amigos en la aplicación SystemBooks, que permite a los usuarios conectarse entre sí mediante solicitudes de amistad y recibir notificaciones push cuando ocurren eventos relacionados con amistades.

## Características Implementadas

### 1. Gestión de Amigos
- **Búsqueda de usuarios**: Los usuarios pueden buscar otros usuarios por nombre de usuario o email
- **Envío de solicitudes**: Capacidad de enviar solicitudes de amistad a otros usuarios
- **Aceptar/Rechazar solicitudes**: Los usuarios pueden aceptar o rechazar solicitudes recibidas
- **Lista de amigos**: Visualización de la lista completa de amigos
- **Historial de solicitudes**: Ver solicitudes enviadas y su estado

### 2. Notificaciones Push
- **Solicitud recibida**: Notificación cuando alguien envía una solicitud de amistad
- **Solicitud aceptada**: Notificación cuando alguien acepta una solicitud enviada
- **Navegación directa**: Las notificaciones abren directamente la sección de amigos

### 3. Estructura de Datos en Firestore

#### Colección: `friend_requests`
```json
{
  "senderId": "string",
  "senderName": "string", 
  "senderEmail": "string",
  "senderPhotoUrl": "string",
  "receiverId": "string",
  "receiverName": "string",
  "receiverEmail": "string", 
  "receiverPhotoUrl": "string",
  "status": "pending|accepted|rejected",
  "createdAt": "timestamp",
  "updatedAt": "timestamp"
}
```

#### Colección: `friendships`
```json
{
  "user1Id": "string",
  "user1Name": "string",
  "user1Email": "string",
  "user1PhotoUrl": "string",
  "user2Id": "string", 
  "user2Name": "string",
  "user2Email": "string",
  "user2PhotoUrl": "string",
  "createdAt": "timestamp"
}
```

## Clases Principales

### 1. Modelos de Datos
- **`FriendRequest.java`**: Modelo para solicitudes de amistad
- **`Friendship.java`**: Modelo para amistades establecidas

### 2. Repositorio
- **`FriendshipRepository.java`**: Maneja todas las operaciones relacionadas con amistades
  - Búsqueda de usuarios
  - Envío de solicitudes
  - Aceptación/Rechazo de solicitudes
  - Obtención de listas de amigos
  - Validaciones de estado

### 3. Interfaz de Usuario
- **`FriendsFragment.java`**: Fragmento principal que contiene:
  - Búsqueda de usuarios
  - Lista de solicitudes recibidas
  - Lista de solicitudes enviadas
  - Lista de amigos
  - Adaptadores para RecyclerViews

### 4. Layouts
- **`fragment_friends.xml`**: Layout principal del sistema de amigos
- **`item_user_search.xml`**: Item para resultados de búsqueda
- **`item_friend_request.xml`**: Item para solicitudes recibidas
- **`item_sent_request.xml`**: Item para solicitudes enviadas
- **`item_friend.xml`**: Item para lista de amigos

## Flujo de Funcionamiento

### 1. Envío de Solicitud de Amistad
1. Usuario busca otros usuarios por nombre o email
2. Selecciona "Enviar solicitud" en un usuario
3. Sistema verifica que no existe solicitud previa o amistad
4. Crea registro en `friend_requests` con estado "pending"
5. Envía notificación push al receptor
6. Actualiza UI mostrando la solicitud en "Solicitudes enviadas"

### 2. Aceptación de Solicitud
1. Usuario recibe notificación de nueva solicitud
2. Ve la solicitud en "Solicitudes recibidas"
3. Presiona "Aceptar"
4. Sistema actualiza el estado de la solicitud a "accepted"
5. Crea nuevo registro en `friendships`
6. Envía notificación al emisor original
7. Actualiza UI mostrando la nueva amistad

### 3. Notificaciones Push
1. **Firebase Cloud Functions** detecta cambios en las colecciones
2. Envía notificaciones FCM basadas en el tipo de evento
3. **FCMService** recibe las notificaciones y las procesa
4. Muestra notificaciones locales con iconos y acciones específicas
5. Al tocar la notificación, abre la aplicación en la sección de amigos

## Validaciones Implementadas

### 1. Validaciones de Envío
- No puede enviarse solicitud a uno mismo
- No puede enviarse solicitud si ya existe una pendiente
- No puede enviarse solicitud si ya son amigos
- Usuario debe estar autenticado

### 2. Validaciones de Estado
- Solo pueden aceptarse/rechazarse solicitudes pendientes
- Solo el receptor puede aceptar/rechazar
- Las solicitudes aceptadas crean automáticamente la amistad

### 3. Validaciones de Búsqueda
- Query no puede estar vacío
- Excluye al usuario actual de los resultados
- Limita resultados para evitar sobrecarga

## Acceso y Permisos

### 1. Disponibilidad
- **Usuarios normales**: Acceso completo al sistema de amigos
- **Administradores**: Acceso completo al sistema de amigos
- **Invitados**: Sin acceso (requiere autenticación)

### 2. Ubicación en la App
- Menú principal del drawer: "Amigos"
- Disponible solo para usuarios autenticados
- Navega directamente desde notificaciones

## Características de UX

### 1. Interfaz Intuitiva
- Búsqueda en tiempo real
- Estados claros (pendiente, aceptado, rechazado)
- Iconos y colores distintivos
- Mensajes de confirmación

### 2. Retroalimentación
- Toasts informativos para acciones
- Indicadores de progreso
- Estados vacíos informativos
- Tiempo relativo ("Hace 2 horas")

### 3. Optimizaciones
- RecyclerViews eficientes
- Carga lazy de datos
- Refresh automático después de acciones
- Gestión de estados de error

## Integración con Sistema Existente

### 1. Autenticación
- Compatible con autenticación Firebase y API REST
- Utiliza los mismos mecanismos de sesión
- Respeta roles y permisos existentes

### 2. Notificaciones
- Extiende el sistema FCM existente
- Utiliza los mismos canales de notificación
- Compatible con NotificationHelper existente

### 3. Base de Datos
- Utiliza Firestore para consistencia
- Integra con colección `users` existente
- Mantiene referencias a datos de usuario

## Posibles Extensiones Futuras

### 1. Funcionalidades Adicionales
- Chat entre amigos
- Compartir libros favoritos con amigos
- Recomendaciones basadas en amigos
- Estados de conexión (online/offline)

### 2. Mejoras de UX
- Fotos de perfil en tiempo real
- Búsqueda avanzada con filtros
- Agrupación de amigos
- Estadísticas de amistad

### 3. Características Sociales
- Feed de actividades de amigos
- Grupos de lectura
- Eventos compartidos
- Sistema de logros compartidos

## Consideraciones de Seguridad

### 1. Privacidad
- Solo información básica es visible en búsquedas
- Los usuarios controlan sus solicitudes
- No se expone información sensible

### 2. Prevención de Spam
- Límites en frecuencia de solicitudes
- Validación de solicitudes duplicadas
- Posibilidad de bloquear usuarios (futura)

### 3. Validación de Datos
- Sanitización de inputs de búsqueda
- Validación de IDs de usuario
- Verificación de permisos en cada operación
