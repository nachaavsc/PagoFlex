# 1. Nombre del Proyecto: PagoFlex

**PagoFlex** es una aplicación móvil financiera desarrollada en **Kotlin con Jetpack Compose** bajo la arquitectura **MVVM + Clean Architecture**, orientada a la gestión, seguimiento y cumplimiento de compromisos de pago y cuotas, respaldada por una arquitectura distribuida de **microservicios en Spring Boot**, base de datos **MySQL** y persistencia local con **Room**.

---

## 2. Integrantes
* **Integrante 1:** Ignacia León — (`nachaavsc`)
* **Integrante 2:** Krishna Leyton
* **Asignatura:** DSY1105 — Desarrollo de Aplicaciones Móviles (Duoc UC)
* **Repositorio App Móvil:** https://github.com/nachaavsc/PagoFlex
* **Repositorio Microservicios:** https://github.com/nachaavsc/PagoFlexMicroservicios

---

## 3. Funcionalidades

### Aplicación Móvil (Kotlin + Jetpack Compose + MVVM)
* **Acceso y Roles:** Inicio de sesión, registro con validación de contraseña segura, recuperación de clave, edición de perfil, cambio de contraseña encriptada (`Hash.kt`) y navegación diferenciada para perfiles **Cliente** y **Gestor**.
* **Gestión de Compromisos y Cuotas (CRUD):** Creación de compromisos financieros (monto, organización, fecha real del sistema y estado), visualización de cuotas pendientes/pagadas, confirmación de pagos paso a paso e historial detallado.
* **Indicadores Económicos en Tiempo Real:** Visualización en la pantalla de inicio del valor diario de la **UF** y el **Dólar** mediante consumo de API externa independiente.
* **Hábito y Gamificación:** Indicador dinámico de cumplimiento (`Cumplimiento.kt`) calculado a partir del historial de pagos puntuales y sistema de recordatorios configurables.
* **Validación Reactiva en Formularios:** Control de errores individuales por campo (`*Error`) en todos los ViewModels (`Validador.kt`), con expresiones regulares para correos y validación de rangos numéricos en montos.
* **Recursos Nativos Integrados:**
    * **Cámara:** Captura de foto de perfil con solicitud de permiso en tiempo real (`RequestPermission`) y guardado en almacenamiento interno (`AlmacenImagenes.kt`).
    * **Vibración:** Retroalimentación háptica (`Vibracion.kt`) conectada a confirmaciones de pago y alertas de error.
    * **Notificaciones:** Canal de notificaciones con permiso `POST_NOTIFICATIONS` y disparo real desde recordatorios (`GestorNotificaciones.kt`).
* **Gestión de Incidencias:** Creación y seguimiento de reportes asociados a compromisos o pagos.
* **Persistencia Local y Tolerancia a Fallos:** Almacenamiento local con **Room** para respaldo ante pérdida temporal de conectividad.
* **Identidad Visual:** Ícono personalizado de **PagoFlex**, paleta Material Design 3 y pantalla de bienvenida (*Splash Screen*).

### Arquitectura Backend (Spring Boot + MySQL)
* **Eureka Server:** Registro y descubrimiento dinámico de microservicios (puerto `8761`).
* **API Gateway:** Punto único de entrada (puerto `8080`) expuesto mediante túnel seguro para el consumo desde la aplicación móvil.
* **Microservicios Desacoplados:** Servicios independientes para Autenticación (`8081`), Usuarios (`8082`), Compromisos (`8083`) e Incidencias (`8084`), cada uno con persistencia en **MySQL** y documentación **Swagger / OpenAPI**.

---

## 4. Guía de Uso de la Aplicación (Paso a Paso)

### Acceso y Gestión de Cuenta
1. **Registro de usuario:** Al abrir la aplicación por primera vez, selecciona **Crear cuenta**, completa tu nombre, correo electrónico, teléfono y una contraseña segura (mínimo 8 caracteres, una mayúscula, un número y un carácter especial) y presiona **Crear cuenta**.
2. **Inicio de sesión:** Ingresa tu correo y contraseña registrados en la pantalla de **Login** para acceder al panel principal.
3. **Recuperación y cambio de clave:** Si olvidaste tu contraseña, utiliza **Recuperar clave** desde el inicio de sesión; si deseas actualizarla estando dentro de tu sesión, ve a **Perfil > Cambiar clave**.

### Registro y Pago de Compromisos
1. Desde la pantalla principal (**Inicio**) o desde la sección **Compromisos**, presiona el botón **Nuevo compromiso (+)**, completa organización, monto y fecha de vencimiento, y presiona **Guardar**.
2. En el listado de **Compromisos pendientes**, selecciona el compromiso que deseas pagar, verifica el detalle en **Confirmar pago** y presiona **Confirmar pago** (emitirá vibración, mostrará un `Snackbar`, cambiará el estado a **Pagado** y registrará la operación en el historial).
3. En **Historial de pagos** visualizarás tu **Indicador de cumplimiento (%)** y podrás activar los **Recordatorios** con notificaciones locales reales.

### Perfil, Cámara e Incidencias
1. Accede a **Perfil** desde la barra de navegación y toca sobre el ícono de cámara en tu foto para actualizar tu avatar con almacenamiento interno persistente.
2. Desde esta vista también puedes actualizar tus datos en **Editar perfil** o reportar inconvenientes en **Reporte de incidencias**.

---

## 5. Arquitectura y Estructura del Proyecto

```text
com.example.pagoflex/
├── data/
│   ├── archivos/       # AlmacenImagenes.kt (persistencia de fotos en almacenamiento interno)
│   ├── local/          # Room Database (Entities, DAOs, AppDatabase, DatosIniciales)
│   ├── remote/         # RetrofitClient.kt, PagoFlexApi.kt y ModelosRemotos.kt (Spring Boot)
│   ├── repository/     # PagoFlexRepository.kt (fuente única de verdad local/remota)
│   └── seguridad/      # Hash.kt (encriptación de contraseñas)
├── model/              # FormularioUIState.kt, Validador.kt, Cumplimiento.kt, LoginResponse.kt
├── navegation/         # AppNavigation.kt (grafo de navegación Jetpack Compose)
├── notificaciones/     # GestorNotificaciones.kt (canales y notificaciones POST_NOTIFICATIONS)
├── ui/
│   ├── components/     # Componentes reutilizables Material 3 (CampoTexto, BotonPrimario, EstadoBadge, FotoPerfil, SnackbarPagoFlex)
│   ├── screens/        # Pantallas (Login, Registro, RecuperarClave, Home, Compromisos, NuevoCompromiso, ConfirmarPago, HistorialPagos, ReporteIncidencias, Perfil, EditarPerfil, CambiarClave)
│   ├── theme/          # Color.kt, Theme.kt, Type.kt (Material Design 3)
│   └── util/           # Formato.kt y Vibracion.kt
└── viewmodel/          # ViewModels con StateFlow, EventoUI.kt y PagoFlexViewModelFactory.kt
```

---

## 6. Tecnologías y Pruebas Unitarias
* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose + Material Design 3
* **Arquitectura:** MVVM (Model-View-ViewModel) con `StateFlow`, `SharedFlow` (`EventoUI`) y `PagoFlexViewModelFactory`
* **Persistencia Local:** Room Database (SQLite) + Coroutines / Flow
* **Consumo de API & Backend:** Retrofit 2 + OkHttp + Gson conectados a microservicios **Spring Boot** (Eureka Server, API Gateway, Autenticación, Usuarios, Compromisos e Incidencias)
* **Pruebas Unitarias (JUnit 4 + Coroutines Test):** Cobertura sobre lógica de negocio, repositorio y ViewModels (`ValidadorTest`, `CumplimientoTest`, `FormatoYPeriodoTest`, `PagoFlexRepositoryTest` y tests unitarios de los ViewModels).

---

## 7. Ejecución y Distribución

### Ejecución de la Aplicación Móvil (Android)
1. Clonar el repositorio:
   ```bash
   git clone [https://github.com/nachaavsc/PagoFlex.git](https://github.com/nachaavsc/PagoFlex.git)
   ```
2. Abrir la carpeta `PagoFlex-main` en **Android Studio** (Ladybug o superior) y esperar a que **Gradle Sync** descargue todas las dependencias.
3. Iniciar los microservicios **Spring Boot** (`Eureka Server` en puerto `8761`, `API Gateway` en puerto `8080` y los servicios de negocio) y verificar la `BASE_URL` en `data/remote/RetrofitClient.kt`.
4. Ejecutar un emulador con **Android 13 (API 33) o superior** o conectar un dispositivo físico con depuración USB activada y presionar **Run (`Shift + F10`)**.
5. **Credenciales de prueba (Cliente):**
    * **Correo:** `cliente@pagoflex.cl`
    * **Contraseña:** `123456`

### Ejecución de Pruebas Unitarias y Cobertura JaCoCo
* **En Android Studio (App Móvil):**
  ```bash
  ./gradlew testDebugUnitTest jacocoTestReport
  ```
* **En Microservicios (Spring Boot — Cobertura JaCoCo > 80%):**
  ```bash
  mvn test jacoco:report
  ```

### Distribución y Firma (`release`)
* **Archivo APK firmado:** `entrega_release/app-release.apk`
* **Archivo App Bundle firmado:** `entrega_release/app-release.aab`
* **Llave de firma (Keystore):** `entrega_release/pagoflex.jks`