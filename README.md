# 1. Nombre del Proyecto: PagoFlex

**PagoFlex** es una aplicación móvil financiera desarrollada en **Kotlin con Jetpack Compose**, orientada a la gestión, seguimiento y cumplimiento de compromisos de pago y cuotas, respaldada por una arquitectura distribuida de **microservicios en Spring Boot**, base de datos **MySQL** e integración con servicios externos en tiempo real.

---

## 2. Integrantes
- **Integrante 1:** Ignacia León — (`nachaavsc`)
- **Integrante 2:** Krishna Leyton — (`[Usuario GitHub Krishna]`)
- **Asignatura:** DSY1105 — Desarrollo de Aplicaciones Móviles (Duoc UC)
- **Repositorio App Móvil:** https://github.com/nachaavsc/PagoFlex
- **Repositorio Microservicios:** https://github.com/nachaavsc/PagoFlexMicroservicios

---

## 3. Funcionalidades

### Aplicación Móvil (Kotlin + Jetpack Compose + MVVM)
- **Acceso y Roles:** Inicio de sesión, registro con validación de contraseña segura, recuperación de clave, edición de perfil, cambio de contraseña encriptada (`Hash.kt`) y navegación diferenciada para perfiles **Cliente** y **Gestor**.
- **Gestión de Compromisos y Cuotas (CRUD):** Creación de compromisos financieros (monto, organización, fecha real del sistema y estado), visualización de cuotas pendientes/pagadas, confirmación de pagos paso a paso e historial detallado.
- **Indicadores Económicos en Tiempo Real:** Visualización en la pantalla de inicio del valor diario de la **UF** y el **Dólar** mediante consumo de API externa independiente.
- **Hábito y Gamificación:** Indicador dinámico de cumplimiento (`Cumplimiento.kt`) calculado a partir del historial de pagos puntuales y sistema de recordatorios.
- **Recursos Nativos Integrados:**
    - **Cámara:** Captura de foto de perfil con solicitud de permiso en tiempo real (`RequestPermission`) y guardado en almacenamiento interno (`AlmacenImagenes.kt`).
    - **Vibración:** Retroalimentación háptica (`Vibracion.kt`) conectada a confirmaciones de pago y alertas de error.
    - **Notificaciones:** Canal de notificaciones con permiso `POST_NOTIFICATIONS` y disparo real desde recordatorios (`GestorNotificaciones.kt`).
- **Gestión de Incidencias:** Creación y seguimiento de reportes asociados a compromisos o pagos.
- **Persistencia Local y Tolerancia a Fallos:** Almacenamiento local con **Room** para respaldo ante pérdida temporal de conectividad.
- **Identidad Visual:** Ícono personalizado de **PagoFlex**, paleta Material Design 3 y pantalla de bienvenida (*Splash Screen*).

### Arquitectura Backend (Spring Boot + MySQL)
- **Eureka Server:** Registro y descubrimiento dinámico de microservicios (`puerto 8761`).
- **API Gateway:** Punto único de entrada (`puerto 8080`) expuesto mediante túnel seguro para el consumo desde la aplicación móvil.
- **Microservicios Desacoplados:** Servicios independientes para Autenticación (`8081`), Usuarios (`8082`), Compromisos (`8083`) e Incidencias (`8084`), cada uno con persistencia en **MySQL** y documentación **Swagger / OpenAPI**.

---

## 4. Endpoints Utilizados (API Externa y Microservicios)

### A. API Externa Pública (Consumo vía Retrofit)
- **Servicio:** Indicadores Económicos Chile (`https://mindicador.cl/`)
    - `GET https://mindicador.cl/api` — Obtiene el valor actualizado en pesos chilenos (CLP) de la **UF** (`uf.valor`) y del **Dólar observado** (`dolar.valor`).

### B. Microservicios Propios (Spring Boot vía API Gateway)
- **URL Base Gateway (Túnel):** `https://zf8jdvcf-8080.brs.devtunnels.ms/` (Local: `http://localhost:8080/`)
- **Documentación Swagger UI:** `http://localhost:<puerto-microservicio>/swagger-ui.html`

| Microservicio | Método | Endpoint | Descripción |
| :--- | :---: | :--- | :--- |
| **Autenticación** | `POST` | `/api/auth/login` | Inicia sesión y retorna datos del usuario autenticado. |
| **Autenticación** | `POST` | `/api/auth/registro` | Registra un nuevo cliente en el sistema. |
| **Usuarios** | `GET` | `/api/usuarios` | Lista los usuarios registrados (vista Gestor). |
| **Usuarios** | `PUT` | `/api/usuarios/{id}` | Actualiza los datos del perfil de usuario. |
| **Compromisos y Pagos** | `GET` | `/api/compromisos` | Lista los compromisos y cuotas asociadas al usuario. |
| **Compromisos y Pagos** | `POST` | `/api/compromisos` | Crea un nuevo compromiso de pago. |
| **Compromisos y Pagos** | `PUT` | `/api/compromisos/{id}/pagar` | Registra el pago de una cuota pendiente. |
| **Incidencias** | `GET` | `/api/incidencias` | Obtiene el listado de incidencias o reportes. |
| **Incidencias** | `POST` | `/api/incidencias` | Registra una nueva incidencia desde la app. |

---

## 5. Guía Rápida de Uso (Flujo Cliente)
1. **Registro e Inicio de Sesión:** Crea una cuenta con contraseña segura (mínimo 8 caracteres, mayúscula, número y carácter especial) o ingresa con un usuario registrado.
2. **Crear Compromiso:** Presiona **Nuevo compromiso (+)**, ingresa organización, monto y fecha de vencimiento.
3. **Pagar Cuota:** Selecciona un compromiso pendiente, verifica el resumen en **Confirmar pago** y confirma (la app vibrará, actualizará el estado a *Pagado* y sumará al historial).
4. **Cumplimiento y Recordatorios:** Revisa tu porcentaje de cumplimiento en el **Historial** y activa los recordatorios en **Inicio**.
5. **Perfil e Incidencias:** Actualiza tu foto con la cámara en **Perfil**, edita tus datos o reporta problemas en **Incidencias**.

---

## 6. Pasos para Ejecutar

### Requisitos Previos
- **Android Studio** (Ladybug o superior, JDK 17+ y SDK Android configurado).
- **Visual Studio Code** o **IntelliJ IDEA** con soporte para Java / Spring Boot.
- **Laragon**, **XAMPP** o **Docker** con servicio **MySQL** activo.

### Ejecución del Backend (Microservicios)
1. Clonar el repositorio de microservicios:
   ```bash
   git clone [https://github.com/nachaavsc/PagoFlexMicroservicios.git](https://github.com/nachaavsc/PagoFlexMicroservicios.git)
   ```
2. Iniciar **Laragon** (clic en **Iniciar todo**) para levantar el servidor **MySQL** en el puerto `3306`.
3. Abrir la carpeta `MicroServicios PagoFlex` en **VS Code**.
4. Ejecutar primero **Eureka Server** (`8761`), luego **Usuarios** (`8082`), **Autenticacion** (`8081`), **Compromisos** (`8083`), **Incidencias** (`8084`) y finalmente **API Gateway** (`8080`).
5. En la pestaña **Ports** de VS Code, verificar que el puerto **`8080`** esté activo con visibilidad **Public** y confirmar que la URL coincida con `BASE_URL` en `RetrofitClient.kt`.

### Ejecución de la Aplicación Móvil (Android)
1. Clonar el repositorio de la app móvil:
   ```bash
   git clone [https://github.com/nachaavsc/PagoFlex.git](https://github.com/nachaavsc/PagoFlex.git)
   ```
2. Abrir el proyecto en **Android Studio** y esperar la sincronización de Gradle.
3. Ejecutar la aplicación en un emulador o dispositivo físico (`Run app`).
4. **Credenciales de prueba (Cliente):**
    - **Correo:** `cliente@pagoflex.cl`
    - **Contraseña:** `123456`

### Ejecución de Pruebas Unitarias y Cobertura JaCoCo
- **En Android Studio (App Móvil):**
  ```bash
  ./gradlew testDebugUnitTest jacocoTestReport
  ```
- **En Microservicios (Spring Boot):**
  ```bash
  mvn test jacoco:report
  ```

---

## 7. Captura del APK Firmado, Llave .jks y Evidencias

### Distribución y Firma (`release`)
- **Archivo APK firmado:** `entrega_release/app-release.apk`
- **Archivo App Bundle firmado:** `entrega_release/app-release.aab`
- **Llave de firma (Keystore):** `entrega_release/pagoflex.jks`
- **Cobertura de Pruebas Unitarias (JaCoCo > 80%):** Reporte ejecutado sobre lógica de negocio, repositorio y ViewModels (`ValidadorTest`, `CumplimientoTest`, `PagoFlexRepositoryTest` y suite de ViewModels).

### Capturas de Evidencia
![Evidencia APK Firmado y Keystore](docs/captura_apk_jks.png)
![Reporte Cobertura JaCoCo](docs/captura_jacoco.png)