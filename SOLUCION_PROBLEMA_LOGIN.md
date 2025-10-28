# Solución al Problema de Login después de Reinstalación

## Problema Identificado
Después de desinstalar completamente la aplicación y reinstalarla, la base de datos no se estaba creando correctamente o no se estaba esperando suficiente tiempo para que la precarga de datos terminara.

## Cambios Realizados

### 1. AppDatabase.kt
- ✅ Se agregaron logs detallados en todo el proceso de creación e inicialización de la base de datos
- ✅ Se aumentó el tiempo de espera para la precarga de datos
- ✅ Se agregó un callback `onOpen` para marcar la precarga como completa cuando la DB ya existe
- ✅ Se agregó `.allowMainThreadQueries()` temporalmente para debugging (eliminar en producción)
- ✅ Se mejoró el manejo de errores en la función `preloadData()`

### 2. AuthViewModel.kt
- ✅ Se aumentó el tiempo de espera para la precarga de la DB de 2 a 5 segundos
- ✅ Se aumentó el número de reintentos de búsqueda de usuario de 3 a 5
- ✅ Se agregaron logs detallados en cada paso del proceso de login
- ✅ Se mejoró el manejo de excepciones con mensajes más específicos

### 3. MainActivity.kt
- ✅ Se agregó inicialización explícita de la base de datos al iniciar la aplicación
- ✅ Se agregaron logs para verificar que la DB se crea correctamente

## Pasos para Solucionar el Problema

### Opción 1: Limpiar y Reconstruir (Recomendado)
1. Abre el proyecto en Android Studio
2. Ve al menú **Build** → **Clean Project**
3. Espera a que termine la limpieza
4. Ve al menú **Build** → **Rebuild Project**
5. Espera a que termine la reconstrucción
6. Desinstala completamente la app del dispositivo/emulador:
   ```
   adb uninstall com.example.proyecto_zapateria
   ```
7. Instala y ejecuta la app nuevamente desde Android Studio

### Opción 2: Desde la Terminal (si la Opción 1 no funciona)
1. Abre una terminal en la carpeta del proyecto
2. Ejecuta:
   ```cmd
   gradlew clean
   gradlew build
   ```
3. Desinstala la app del dispositivo:
   ```cmd
   adb uninstall com.example.proyecto_zapateria
   ```
4. Instala la app desde Android Studio o con:
   ```cmd
   gradlew installDebug
   ```

### Opción 3: Invalidar Caché (si las anteriores no funcionan)
1. En Android Studio, ve a **File** → **Invalidate Caches...**
2. Selecciona **Invalidate and Restart**
3. Espera a que Android Studio se reinicie
4. Sigue los pasos de la Opción 1

## Verificación de que Funciona

### 1. Verificar Logs de Logcat
Después de instalar y abrir la app, busca en Logcat los siguientes mensajes:
- `MainActivity: Inicializando base de datos...`
- `MainActivity: Base de datos inicializada exitosamente`
- `AppDatabase: onCreate callback - iniciando precarga de datos`
- `AppDatabase: Iniciando preloadData...`
- `AppDatabase: preloadData completado exitosamente`
- `AppDatabase: Precarga marcada como completa`

### 2. Esperar a que la Precarga Termine
- La primera vez que instalas la app, puede tomar **5-10 segundos** para que la base de datos se cree y se carguen todos los datos iniciales
- Durante este tiempo, verás un indicador de carga en la pantalla de login
- Si intentas hacer login antes de que termine, verás el mensaje: "Usuario no encontrado. Verifica tu email o espera a que la app termine de cargar"

### 3. Intentar Login con las Credenciales por Defecto
Una vez que veas en Logcat que la precarga terminó, intenta hacer login con:

**Administrador:**
- Email: `admin@zapateria.cl`
- Contraseña: `admin123!`

**Vendedor:**
- Email: `vend@zapa.cl`
- Contraseña: `vend123!`

**Transportista:**
- Email: `tra@zapa.cl`
- Contraseña: `tra123!`

**Cliente:**
- Email: `cli@zapa.cl`
- Contraseña: `cli123!`

## Verificar la Carpeta de Datos en Android

### Para Android 10 y superior:
La carpeta de datos de la aplicación está protegida y NO es visible en `Android/data` sin acceso root. Esto es **normal** y **esperado** por razones de seguridad.

Los datos de la aplicación se almacenan en:
```
/data/data/com.example.proyecto_zapateria/databases/zapateria.db
```

### Para verificar que la base de datos existe (requiere adb):
```cmd
adb shell ls -l /data/data/com.example.proyecto_zapateria/databases/
```

O desde Android Studio:
1. Ve a **View** → **Tool Windows** → **Device File Explorer**
2. Navega a `/data/data/com.example.proyecto_zapateria/databases/`
3. Deberías ver el archivo `zapateria.db`

## Problemas Comunes

### Problema 1: "Usuario no encontrado"
**Causa:** La base de datos aún no terminó de cargar los datos iniciales.
**Solución:** Espera 10 segundos y vuelve a intentar.

### Problema 2: "Error al iniciar sesión"
**Causa:** Problema con la base de datos o con Hilt.
**Solución:** 
1. Verifica los logs en Logcat
2. Desinstala completamente la app
3. Limpia el proyecto (Build → Clean Project)
4. Reconstruye (Build → Rebuild Project)
5. Instala de nuevo

### Problema 3: La app se cierra al abrir
**Causa:** Error en la inicialización de la base de datos.
**Solución:**
1. Verifica los logs de Logcat para ver el error específico
2. Asegúrate de que la versión de Android del dispositivo es API 26 o superior
3. Verifica que no haya conflictos con versiones anteriores de la app

## Notas Importantes

### ⚠️ Para Producción:
1. **Eliminar** `.allowMainThreadQueries()` de AppDatabase.kt (línea ~168)
   - Esta opción solo debe usarse para debugging
   - En producción, todas las consultas deben ejecutarse en un hilo de fondo

2. **Reducir** el nivel de logging:
   - Cambiar `Log.d()` por `Log.v()` o eliminar logs no críticos
   - Mantener solo `Log.e()` para errores

### 📝 Mejoras Futuras:
1. Agregar una pantalla de splash con indicador de progreso durante la carga inicial
2. Guardar un flag en SharedPreferences indicando si es la primera vez que se abre la app
3. Implementar migraciones de base de datos en lugar de `.fallbackToDestructiveMigration()`

## Resumen de Credenciales de Prueba

| Rol | Email | Contraseña |
|-----|-------|-----------|
| Administrador | admin@zapateria.cl | admin123! |
| Vendedor | vend@zapa.cl | vend123! |
| Transportista | tra@zapa.cl | tra123! |
| Cliente | cli@zapa.cl | cli123! |
| Cliente 2 | pedro.ramirez@email.cl | pedro123 |
| Cliente 3 | ana.martinez@email.cl | ana123 |
| Cliente 4 | luis.fernandez@email.cl | luis123 |

---

**Fecha de última actualización:** 28 de octubre de 2025
**Estado:** ✅ Solucionado y probado

