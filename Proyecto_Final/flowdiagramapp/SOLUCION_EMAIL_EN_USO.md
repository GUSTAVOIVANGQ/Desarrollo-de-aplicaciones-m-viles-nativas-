# Solución al Problema: "Email already in use"

## 🎯 Problema Identificado

El error `firebase_auth/email-already-in-use` indica que hay usuarios registrados en Firebase Authentication que no están sincronizados correctamente con Firestore Database, o hay registros "fantasma" de pruebas anteriores.

## 🔧 Herramientas de Diagnóstico Implementadas

### 1. Pantalla de Debug
- **Ubicación**: Botón "Debug Firebase" en la pantalla de login
- **Función**: Diagnostica el estado actual de:
  - Firebase Authentication
  - Firestore Database  
  - Usuario actual en la app
  - Cache local

### 2. Métodos de Limpieza
- **Cerrar Sesión Completa**: Limpia todos los estados
- **Sincronizar Usuario**: Crea registro en Firestore para usuario existente en Auth
- **Eliminar Usuario**: Elimina completamente un usuario problemático (PELIGRO)

## 📱 Pasos para Resolver el Problema

### Paso 1: Diagnosticar
1. Abrir la aplicación
2. Presionar "Debug Firebase" en la pantalla de login
3. Revisar el diagnóstico que aparece

### Paso 2: Soluciones según el diagnóstico

#### Caso A: Usuario existe en Authentication pero NO en Firestore
```
⚠️ PROBLEMA: Usuario existe en Authentication pero NO en Firestore
Solución: Usar "Sincronizar Usuario"
```
**Acción**: Presionar "Sincronizar Usuario" para crear el registro en Firestore.

#### Caso B: Error de email ya en uso pero diagnóstico muestra sin usuarios
```
✅ OK: No hay usuarios registrados
```
**Acción**: 
1. Presionar "Cerrar Sesión Completa"
2. Intentar registrarse nuevamente
3. Si persiste, usar "Eliminar Usuario Actual" (PELIGRO)

#### Caso C: Usuarios sincronizados correctamente
```
✅ OK: Usuario sincronizado entre Authentication y Firestore
```
**Acción**: El problema puede estar en el cache. Presionar "Cerrar Sesión Completa".

### Paso 3: Intentar registro nuevamente
1. Después de aplicar la solución, volver a la pantalla de login
2. Ir a "¿No tienes cuenta? Regístrate aquí"
3. Intentar el registro con un email nuevo

## 🔥 Limpieza Manual en Firebase Console (Si es necesario)

Si las herramientas automáticas no funcionan:

### Firebase Authentication:
1. Ir a [Firebase Console](https://console.firebase.google.com/)
2. Seleccionar proyecto "flowdiagram-app"
3. Ir a Authentication > Users
4. Eliminar usuarios problemáticos manualmente

### Firestore Database:
1. Ir a Firestore Database
2. Ir a la colección "users"
3. Eliminar documentos huérfanos

## 🚨 Mensajes de Error Mejorados

El sistema ahora muestra errores más específicos:

- ✅ `El email ya está registrado. Intenta iniciar sesión en su lugar.`
- ✅ `El formato del email no es válido.`
- ✅ `La contraseña es muy débil. Debe tener al menos 6 caracteres.`
- ✅ `El registro con email/contraseña no está habilitado.`

## 🛠️ Para Desarrolladores

### Verificar antes del registro:
```dart
final existingMethods = await _auth.fetchSignInMethodsForEmail(email);
if (existingMethods.isNotEmpty) {
  // Email ya registrado
}
```

### Manejo de excepciones específicas:
```dart
} on FirebaseAuthException catch (e) {
  switch (e.code) {
    case 'email-already-in-use':
      // Manejo específico
    case 'invalid-email':
      // Manejo específico
    // etc...
  }
}
```

## 📞 Contacto de Emergencia

Si ninguna solución funciona:
1. Usar el botón "🗑️ ELIMINAR Usuario Actual (PELIGRO)" en Debug
2. Verificar reglas de seguridad en Firestore
3. Revisar configuración de Firebase Authentication

---

**Nota**: Las herramientas de debug están incluidas solo para desarrollo. En producción deberían removerse o protegerse con autenticación de administrador.
