# Notas del back (memoria para próximos cambios)

Última actualización: 2026-11-25. Responder en español, simple y en pasos cortos.
**El repositorio es público: no escribir aquí contraseñas, claves, direcciones de la base ni usuarios.**

## Proyecto
- API Java, Jakarta EE 11 (Maven, `release` 17), WAR `papeleria-api`, contexto `/papeleria-api`, GlassFish 8.
- Conexión: `extra/config/ConexionBD.java` (HikariCP, PostgreSQL en Render).
- Variables: `PAPELERIA_DB_PASSWORD` (o `password=` en `%USERPROFILE%\.papeleria\bank-render.properties`),
  `PAPELERIA_AES_KEY_BASE64` (la misma con que se cifraron los datos), opcionales `PAPELERIA_DB_URL`, `PAPELERIA_DB_USER`.
- Front: repositorio Carrillo27/Frontend (Angular, se queda en localhost; `proxy.conf.json` apunta al 9080).

## Pasos de la profesora
| Paso | Estado |
| --- | --- |
| 1 y 1.5: base PostgreSQL en Render y conexión JDBC | Hecho |
| 2: clase de conexión en Java | Hecho (cumple el documento de ejemplo) |
| 3: actualizar el repositorio | Rama `migracion-cifrado` (sin pull request a `main`) |
| 3.5: probar en localhost | Back arrancado contra Render y `/api/salud` ok. Falta login y consultas con un usuario real |
| 4 y 4.5: desplegar en Render y obtener el enlace | Pendiente |
| 5: probar los endpoints en Render | Pendiente |

Arquitectura: PostgreSQL en la web, Java en la web, Angular en localhost.

## Cifrado
- La base de Render **ya está cifrada** (aplicado el 2026-11-25). No volver a migrar.
- Herramienta: `extra/herramientas/MigrarCifrado.java` y `migrar-render.bat` (ensayo y luego `APLICAR`).
- La clave queda fuera del repositorio. Sin ella no se pueden leer los datos; compartirla solo por medio privado.

## Despliegue en Render (Docker)
- `Dockerfile` en la raíz, **sin probar**. Web Service con Language `Docker`, región de la base.
- Variables en Environment: `PORT=8080`, `PAPELERIA_DB_URL` (host interno, formato `jdbc:`), `PAPELERIA_DB_USER`,
  `PAPELERIA_DB_PASSWORD`, `PAPELERIA_AES_KEY_BASE64`.
- Health check: `/papeleria-api/api/salud`.
- Plan gratuito: 512 MB (puede quedar justo), se suspende tras 15 min; la base gratuita caduca a los 30 días.
- Después: apuntar el `proxy.conf.json` del front al enlace de Render.

## En local
- Carpeta aparte `papeleria-local` (fuera del repositorio): base de prueba en el puerto 55432 con su propia clave.
- GlassFish `C:\glassfish8`, dominio `papeleria2`, HTTP 9080, admin 9048.
- Probado con la base local: login y 13 rutas GET con 200; otras 14 responden 405 (no implementan GET).
- No se probaron ventas, cierres de caja ni altas desde las pantallas.

## Cómo trabajar
- `mvn package` modifica `extra/target`, que está en Git y no debería: restaurar con `git checkout -- extra/target`.
- Mostrar al usuario antes de ejecutar algo que modifique la base.
- Trabajar en ramas, no en `main`, y confirmar antes de subir.
