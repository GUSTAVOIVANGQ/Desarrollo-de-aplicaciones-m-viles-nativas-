# BlueWeb Navigator - Guía de Configuración de Permisos

## Problema: "Permisos insuficientes para hacer el dispositivo descubrible"

Si ves este mensaje en los logs del servidor, sigue estos pasos:

## Configuración del Dispositivo Servidor (Dispositivo A)

### Paso 1: Otorgar Permisos de la Aplicación
1. Abre **Configuración** en tu dispositivo
2. Ve a **Aplicaciones** o **Apps**
3. Busca y selecciona **BlueWeb Navigator**
4. Toca en **Permisos**
5. Asegúrate de que estén **ACTIVADOS**:
   - ✅ **Ubicación** (Necesario para Bluetooth en Android 6+)
   - ✅ **Dispositivos cercanos** (Android 12+)
   - ✅ **Bluetooth** (si aparece como opción)

### Paso 2: Hacer el Dispositivo Descubrible
Cuando aparezca el diálogo pidiendo hacer el dispositivo descubrible:
1. **ACEPTA** la solicitud
2. Si no aparece automáticamente, ve a **Configuración → Bluetooth**
3. Toca en el **nombre de tu dispositivo** (en la parte superior)
4. Activa **"Visible para otros dispositivos"** o **"Descubrible"**
5. Mantén esta pantalla abierta por unos minutos

### Paso 3: Alternativa Manual
Si el método automático no funciona:
1. Ve a **Configuración → Bluetooth**
2. Toca el **icono de configuración** ⚙️ junto al Bluetooth
3. Busca la opción **"Hacer dispositivo visible"** o **"Descubrible"**
4. Actívala por **5 minutos**

## Configuración del Dispositivo Cliente (Dispositivo B)

### Paso 1: Otorgar Permisos
1. Abre **Configuración**
2. Ve a **Aplicaciones → BlueWeb Navigator → Permisos**
3. Activa:
   - ✅ **Ubicación**
   - ✅ **Dispositivos cercanos**

### Paso 2: Buscar Dispositivos
1. En la app cliente, presiona **"Buscar Servidores"**
2. Si hay dispositivos emparejados, aparecerán primero
3. Si no, iniciará búsqueda automática de dispositivos cercanos

## Verificación de Funcionamiento

### En el Servidor:
Los logs deben mostrar:
```
[Hora] Permisos otorgados correctamente
[Hora] BLUETOOTH_ADVERTISE: true
[Hora] BLUETOOTH_CONNECT: true
[Hora] Solicitando hacer el dispositivo descubrible por 300 segundos...
[Hora] Dispositivo configurado como descubrible por 300 segundos
[Hora] Modo de escaneo: Conectable y descubrible
```

### En el Cliente:
Debes ver:
```
Encontrados X dispositivos emparejados
```
O:
```
Buscando dispositivos...
Búsqueda completada
```

## Troubleshooting

### Si el servidor sigue sin ser descubrible:
1. **Reinicia Bluetooth** en ambos dispositivos
2. **Desinstala y reinstala** la aplicación
3. Ve a **Configuración → Apps → BlueWeb → Almacenamiento → Borrar datos**
4. **Empareja los dispositivos manualmente** primero:
   - Configuración → Bluetooth → Buscar dispositivos
   - Empareja los dos celulares

### Si el cliente no encuentra dispositivos:
1. Asegúrate de que el **servidor esté descubrible**
2. **Acerca los dispositivos** (máximo 10 metros)
3. **Reinicia la búsqueda** varias veces
4. Verifica que el **GPS esté activado** (requerido para Bluetooth en Android)

### Permisos en Android 12+:
Si usas Android 12 o superior, la aplicación pedirá permisos adicionales:
- **"Buscar, conectar y determinar la posición relativa de dispositivos cercanos"**
- **ACEPTA** este permiso

## Instrucciones Rápidas

**Servidor:**
1. Abrir app → Servidor → Iniciar Servidor
2. Aceptar hacer descubrible
3. Mantener pantalla activa

**Cliente:**
1. Abrir app → Cliente → Buscar Servidores
2. Seleccionar dispositivo servidor
3. ¡Conectar!

## Contacto
Si sigues teniendo problemas, verifica que ambos dispositivos tengan Android 7.0+ y Bluetooth 4.0+.
