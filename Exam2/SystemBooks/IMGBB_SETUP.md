# Configuración de imgbb para Upload de Imágenes de Perfil

## Pasos para configurar imgbb:

### 1. Obtener API Key de imgbb
1. Ve a [imgbb.com](https://imgbb.com/)
2. Crea una cuenta gratuita o inicia sesión
3. Ve a [API documentation](https://api.imgbb.com/)
4. Obtén tu API key gratuita

### 2. Configurar API Key en la aplicación
Abre el archivo `ImgbbUploader.java` y reemplaza `"tu_api_key_aqui"` con tu API key real:

```java
private static final String API_KEY = "tu_api_key_real_aqui";
```

### 3. Características de imgbb (plan gratuito)
- Límite de tamaño: 32MB por imagen
- Sin límite de ancho de banda
- Sin límite de solicitudes de API
- Almacenamiento permanente
- URLs públicas directas

### 4. Funcionalidades implementadas
- ✅ Subida de imágenes desde cámara
- ✅ Subida de imágenes desde galería
- ✅ Redimensionamiento automático para optimizar tamaño
- ✅ Compresión JPEG para reducir tamaño de archivo
- ✅ Conversión automática a Base64
- ✅ Manejo de errores robusto
- ✅ URLs públicas directas para mostrar imágenes

### 5. Ventajas de usar imgbb vs Firebase Storage
- ✅ Más fácil de implementar
- ✅ No requiere configuración compleja de Firebase
- ✅ URLs públicas directas
- ✅ Plan gratuito generoso
- ✅ Sin necesidad de autenticación adicional

### 6. Seguridad
- La API key se puede incluir en el código del cliente porque imgbb está diseñado para uso público
- Las imágenes subidas son públicamente accesibles por design
- No se requiere autenticación del usuario para subir imágenes

### 7. Cómo probar
1. Configura tu API key
2. Compila y ejecuta la aplicación
3. Ve al perfil de usuario
4. Toca "Cambiar foto"
5. Selecciona una imagen de la cámara o galería
6. La imagen se subirá automáticamente a imgbb y se mostrará en el perfil
