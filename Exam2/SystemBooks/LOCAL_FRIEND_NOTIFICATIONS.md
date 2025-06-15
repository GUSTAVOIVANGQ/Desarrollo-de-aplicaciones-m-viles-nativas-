# Notificaciones Locales para Solicitudes de Amistad

## Funcionalidad Implementada

Se ha añadido un sistema de notificaciones push locales que se activa automáticamente cuando el usuario ingresa a la sección de "Amigos" (`FriendsFragment`) y hay solicitudes de amistad pendientes.

## Características

### 1. **Detección Automática**
- La notificación se dispara automáticamente cuando el usuario navega al fragmento de amigos
- Detecta tanto solicitudes recibidas como solicitudes enviadas
- Prioriza las solicitudes recibidas sobre las enviadas

### 2. **Tipos de Notificaciones**

#### Solicitudes Recibidas (Prioridad Alta)
- **Título**: "Solicitudes de amistad"
- **Mensaje**: 
  - 1 solicitud: "Tienes 1 solicitud de amistad pendiente"
  - Múltiples: "Tienes X solicitudes de amistad pendientes"

#### Solicitudes Enviadas (Prioridad Baja)
- **Título**: "Solicitudes enviadas"
- **Mensaje**: 
  - 1 solicitud: "Tienes 1 solicitud enviada esperando respuesta"
  - Múltiples: "Tienes X solicitudes enviadas esperando respuesta"

### 3. **Control Anti-Spam**
- **Cooldown de 5 minutos**: No se envían notificaciones repetidas en menos de 5 minutos
- **Una por sesión**: Solo se muestra una notificación por sesión del fragmento
- **Prioridad**: Solo se muestra una notificación a la vez (priorizando las recibidas)

### 4. **Persistencia**
- Usa `SharedPreferences` para recordar la última vez que se envió una notificación
- El cooldown persiste entre sesiones de la aplicación

## Implementación Técnica

### Archivos Modificados

#### `FriendsFragment.java`
- Añadido `LocalNotificationHelper` como dependencia
- Implementados métodos del ciclo de vida (`onResume`, `setUserVisibleHint`, `onPause`, `onDestroyView`)
- Añadidos métodos de control de notificaciones:
  - `sendLocalNotificationsForPendingRequests()`
  - `checkAndSendNotificationsOnResume()`
  - `loadRequestsForNotificationCheck()`
  - `shouldSendNotification()`
  - `markNotificationSent()`

### Flujo de Funcionamiento

1. **Usuario ingresa al fragmento**: Se detecta en `onResume()` y `setUserVisibleHint()`
2. **Carga de datos**: Se cargan las solicitudes pendientes
3. **Verificación de cooldown**: Se verifica si ha pasado suficiente tiempo desde la última notificación
4. **Envío de notificación**: Se envía la notificación apropiada con un retraso de 1.5-3 segundos
5. **Marcado como enviada**: Se actualiza el estado para evitar duplicados

### Configuración

#### Cooldown Period
```java
private static final long NOTIFICATION_COOLDOWN = 300000; // 5 minutos en milisegundos
```

#### Delays
- **Carga inicial**: 3 segundos después de cargar el fragmento
- **On resume**: 1.5-2 segundos después de hacerse visible

## Uso

La funcionalidad es completamente automática. No requiere configuración adicional del usuario.

### Escenarios de Activación

1. **Navegación directa**: Usuario navega a la sección "Amigos" desde el menú lateral
2. **Notificación de Firebase**: Usuario abre la app desde una notificación de amistad
3. **Regreso a la app**: Usuario regresa a la sección después de estar en otra parte de la app

### Comportamiento Esperado

- ✅ Se muestra notificación cuando hay solicitudes pendientes
- ✅ No se muestran notificaciones repetidas en poco tiempo
- ✅ Prioriza solicitudes recibidas sobre enviadas
- ✅ Funciona sin conexión a Firebase (es local)
- ✅ Se limpia al salir del fragmento

## Testing

### Casos de Prueba

1. **Primera visita con solicitudes**:
   - Envía solicitud desde otra cuenta
   - Navega a "Amigos"
   - Debe aparecer notificación local

2. **Cooldown**:
   - Envía notificación
   - Sale y regresa inmediatamente
   - No debe aparecer otra notificación

3. **Múltiples solicitudes**:
   - Envía varias solicitudes desde diferentes cuentas
   - Navega a "Amigos"
   - Debe mostrar el número correcto

4. **Sin solicitudes**:
   - No tener solicitudes pendientes
   - Navegar a "Amigos"
   - No debe aparecer notificación

### Logs de Debug

Para verificar el funcionamiento, revisar logs con filtros:
```
tag:FriendsFragment | grep notification
tag:LocalNotificationHelper
```

## Limitaciones

- Solo funciona cuando el usuario navega activamente al fragmento
- No funciona en segundo plano (por diseño)
- Cooldown global (no por tipo de solicitud)
- Una sola notificación por vez

## Beneficios

- **UX mejorada**: Usuario informado inmediatamente sobre solicitudes pendientes
- **No intrusivo**: Solo cuando está en la sección relevante
- **Local**: No depende de conexión a internet
- **Controlado**: Evita spam de notificaciones
