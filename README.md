# VetTurno — Veterinaria Huellitas

Taller universitario de Java AI Engineer, módulo 3: backend profesional con Spring Boot.

Doña Marta, el doctor Andrés y Paula organizan su veterinaria con un cuaderno y mensajes de WhatsApp. VetTurno centraliza responsables, mascotas, veterinarios y citas, evita duplicar el inicio de una cita para el mismo veterinario y conserva la información en MySQL.

**Repositorio:** [giovanni104/VetTurno](https://github.com/giovanni104/VetTurno).

## Alcance

Paula (USER) registra responsables y mascotas, agenda citas y consulta información. Doña Marta (ADMIN) hace lo mismo y además registra veterinarios. El doctor Andrés no inicia sesión en este MVP.

Incluye registro/login, cinco entidades, once endpoints, DTO, validación, errores, JWT, MySQL y Swagger. Quedan fuera historia clínica, facturación, inventario, recordatorios, frontend y despliegue cloud. Docker no aporta puntaje al taller. La actividad extra ejecuta la API con Java 17 y MySQL en dos contenedores.

## Tecnologías

| Componente | Versión o decisión |
| --- | --- |
| Java | 17 |
| Spring Boot | 4.1.0 |
| Maven Wrapper | Maven 3.9.16 |
| Persistencia | Spring Data JPA / Hibernate y MySQL |
| Seguridad | Spring Security, BCrypt y JJWT 0.13.0 |
| Documentación | springdoc-openapi-starter-webmvc-ui 3.1.0 |
| Validación | Jakarta Bean Validation |
| Pruebas | JUnit, Mockito y MockMvc proporcionados por los starters de prueba |

Las versiones de Spring Data, Hibernate, Spring Security y Connector/J las administra Spring Boot en el POM. No instalar dependencias manualmente ni cambiar las versiones acordadas.

## Arquitectura y modelo

```text
src/main/java/com/huellitas/vetturno/
  VetTurnoApplication.java
  model/        Propietario, Mascota, Veterinario, Cita, Usuario y Rol
  repository/   acceso a MySQL con JpaRepository
  service/      reglas de negocio y transacciones
  controller/   rutas HTTP y @Valid
  dto/          contratos de entrada y salida
  security/     JWT, usuarios, permisos y errores de seguridad
  config/       reloj Colombia y OpenAPI
  exception/    ApiError y manejador global
```

[Diagrama, relaciones y conceptos para la defensa](docs/modelo.md).

Las llaves foráneas están en Mascota y Cita. No hay colecciones inversas innecesarias. Las respuestas usan DTO, nunca entidades JPA ni contraseñas. CitaDTO muestra los nombres de mascota, propietario y veterinario.

## Ejecutar en Windows o Linux con Docker

Necesitas Docker iniciado y acceso a Internet. No necesitas instalar Java, Maven ni MySQL.

1. Descarga y descomprime el repositorio.
2. Duplica `.env.example` como `.env` en la raiz del proyecto.
3. Abre una terminal en esa carpeta y ejecuta:

```text
docker compose up --build -d
docker compose logs --tail 50 api
```

Espera a ver **Started VetTurnoApplication** y abre [Swagger UI](http://localhost:8080/swagger-ui/index.html).

La [guia facil para la profesora](docs/ejecucion-profesora.md) explica cada paso, el primer ADMIN, las pruebas y como detener o reiniciar sin perder datos. La base empieza vacia y las claves incluidas son ejemplos para uso local.

Si quieres ejecutar Java y MySQL instalados en tu equipo, sigue la [guia local para Windows y Linux](docs/ejecucion-local.md). Elige una de las dos formas de arranque; ambas usan la misma API.

## Contrato HTTP

| Método | Ruta | Acceso | Éxito |
| --- | --- | --- | --- |
| POST | /api/auth/register | Público; siempre USER | 200 y token |
| POST | /api/auth/login | Público | 200 y token |
| POST | /api/propietarios | USER / ADMIN | 201 |
| GET | /api/propietarios | USER / ADMIN | 200 |
| POST | /api/mascotas | USER / ADMIN | 201 |
| GET | /api/mascotas | USER / ADMIN | 200 |
| POST | /api/veterinarios | ADMIN | 201 |
| GET | /api/veterinarios | USER / ADMIN | 200 |
| POST | /api/citas | USER / ADMIN | 201 |
| GET | /api/citas | USER / ADMIN | 200 |
| GET | /api/citas/veterinario/{id} | USER / ADMIN | 200 |

Registrar una cuenta responde 200 por indicación de la matriz del taller. Las otras creaciones responden 201.

| Entrada | Campos |
| --- | --- |
| RegistroRequest | email obligatorio y válido; password mínimo 8 caracteres |
| LoginRequest | email y password obligatorios |
| PropietarioRequest | nombre y telefono obligatorios; email opcional válido |
| MascotaRequest | nombre, especie y propietarioId obligatorios; raza opcional |
| VeterinarioRequest | nombre y especialidad obligatorios |
| CitaRequest | fechaHora futura, motivo, mascotaId y veterinarioId obligatorios |

El teléfono admite entre 7 y 15 dígitos y un + inicial opcional. Los ids deben ser positivos y referenciar registros existentes. BCrypt tiene un límite técnico de 72 bytes de contraseña; la API lo controla en el registro.

### Fechas y agenda

Todas las fechas de citas representan hora local de Colombia, America/Bogota. Envía un valor sin offset, por ejemplo `2030-10-20T09:00:00`, con segundos y fracciones en cero. El servidor rechaza segundos distintos de cero; no redondea silenciosamente.

La fecha debe ser futura al procesar la solicitud. Sustituye las fechas de ejemplo si ya pasaron.

Las agendas se ordenan por fecha ascendente e incluyen citas pasadas. La regla de cruce compara exactamente veterinario y fechaHora; el MVP no modela duración ni solapamientos por intervalos. El mismo horario para veterinarios distintos es válido.

Un veterinario existente sin citas devuelve 200 y []; un veterinario inexistente produce 400.

## Pruebas con Postman

Importa la [colección](postman/VetTurno.postman_collection.json) y el [entorno local](postman/VetTurno-Local.postman_environment.json). Sigue la [guía paso a paso](docs/guia-postman.md). Incluye 18 solicitudes manuales para los once endpoints: pulsa Send, copia los tokens al entorno y reemplaza los ids en los ejemplos. No requiere scripts ni Collection Runner.

## Flujo en Swagger y primer ADMIN

1. Registra a Paula mediante POST /api/auth/register, con un correo válido y una contraseña elegida localmente. La respuesta contiene token. Si envías rol=ADMIN, se ignora y se guarda USER.
2. Usa POST /api/auth/login para comprobar las credenciales. Pulsa **Authorize** y pega solo el token, sin escribir Bearer. Swagger agrega ese prefijo.
3. Con Paula, intenta POST /api/veterinarios. Debes recibir 403.
4. Registra la cuenta de Marta mediante la misma API. Todavía tendrá USER.
5. Abre MySQL como indica la guia de ejecucion que elegiste (cliente de Docker, terminal local o Workbench). Verifica que el email corresponda a Marta y promueve exclusivamente esa fila. Ejemplo con un correo ficticio que debes sustituir por el registrado:

```sql
USE vetturno;
SELECT id, email, rol FROM usuarios WHERE email = 'marta@example.com';

START TRANSACTION;
UPDATE usuarios
SET rol = 'ADMIN'
WHERE email = 'marta@example.com' AND rol = 'USER';
SELECT ROW_COUNT() AS filas_actualizadas;
SELECT id, email, rol FROM usuarios WHERE email = 'marta@example.com';
-- Confirmar que se actualizo exactamente la cuenta prevista.
COMMIT;
```

Si la cuenta no es la prevista, usa ROLLBACK antes de COMMIT. No existe endpoint público para asignar ADMIN.

6. Inicia sesión otra vez con Marta y reemplaza el token en Authorize.
7. Crea el veterinario y comprueba el 201:

```json
{"nombre": "Andres", "especialidad": "Medicina general"}
```

8. Crea un responsable:

```json
{"nombre": "Laura", "telefono": "3001234567", "email": "laura@example.com"}
```

9. Crea la mascota usando el id real recibido para el responsable:

```json
{"nombre": "Luna", "especie": "Canino", "raza": "Mestiza", "propietarioId": 1}
```

10. Crea la cita con una fecha futura y los ids reales de mascota y veterinario:

```json
{"fechaHora": "2030-10-20T09:00:00", "motivo": "Control general", "mascotaId": 1, "veterinarioId": 1}
```

Los ids 1 son ilustrativos, no se asume que existan. La respuesta de cita es plana:

```json
{"id": 1, "fechaHora": "2030-10-20T09:00:00", "motivo": "Control general", "mascota": "Luna", "propietario": "Laura", "veterinario": "Andres"}
```

11. Consulta la agenda completa y por veterinario; repite la creación para obtener 400.
12. Guarda los ids, detén y reinicia el servidor, vuelve a consultar y verifica que permanecen. Si el JWT venció, inicia sesión de nuevo.

## Seguridad y errores

El JWT tiene vigencia de **una hora**. Es una credencial firmada, no cifrada. Cada solicitud protegida presenta su token; no se crea sesión HTTP.

| Estado | Significado |
| --- | --- |
| 400 | Campos inválidos, referencias inexistentes, email repetido o cita cruzada |
| 401 | Sin token, token inválido/vencido o credenciales incorrectas |
| 403 | Usuario autenticado sin el rol necesario |
| 500 | Fallo imprevisto; mensaje genérico sin detalles internos |

Ejemplo ilustrativo de validación, no evidencia de una ejecución:

```json
{
  "status": 400,
  "mensaje": "Hay campos invalidos",
  "errores": {
    "email": ["El email debe ser valido"],
    "password": ["La contrasena debe tener al menos 8 caracteres"]
  },
  "timestamp": "2030-10-20T08:00:00-05:00"
}
```

Ejemplo ilustrativo de cruce:

```json
{
  "status": 400,
  "mensaje": "El veterinario ya tiene una cita en ese horario",
  "errores": {},
  "timestamp": "2030-10-20T08:00:00-05:00"
}
```

El manejador global traduce errores de validación/negocio y fallos imprevistos. Spring Security comprueba identidad y permisos antes del controller; sus filtros requieren manejadores específicos de 401/403. [Explicación para la defensa](docs/modelo.md#por-que-el-manejador-global-no-reemplaza-la-seguridad).

## Pruebas y empaquetado

Docker compila el proyecto y ejecuta las pruebas sin MySQL al construir la imagen. Si estas siguiendo la guia local con Java 17, usa estos comandos desde la carpeta de pom.xml:

| Que quieres hacer | Windows | Linux |
| --- | --- | --- |
| Ejecutar pruebas sin MySQL | `.\mvnw.cmd test` | `sh ./mvnw test` |
| Crear el JAR y ejecutar pruebas | `.\mvnw.cmd clean verify` | `sh ./mvnw clean verify` |

Un JAR es el archivo que contiene la aplicacion compilada. Se genera en `target/vetturno-0.0.1-SNAPSHOT.jar`. Con MySQL iniciado y la configuracion local preparada, puedes ejecutarlo en ambos sistemas:

```text
java -jar target/vetturno-0.0.1-SNAPSHOT.jar
```

Para comprobar tambien el flujo con MySQL, primero completa la guia local. Esta prueba agrega sus propios datos de ejemplo y necesita la conexion configurada.

**Windows, PowerShell:**

```powershell
$env:VETTURNO_MYSQL_TEST = 'true'
.\mvnw.cmd test
Remove-Item Env:VETTURNO_MYSQL_TEST
```

**Linux, Bash:**

```bash
VETTURNO_MYSQL_TEST=true sh ./mvnw test
```

Sin esa variable, la prueba de integracion se omite. Una prueba omitida no demuestra que MySQL funcione. Los resultados se guardan en `target/surefire-reports`. Para comprobar que las citas permanecen al reiniciar, sigue el paso de persistencia de la guia de Postman.

## Evidencias, listas y entrega

- [Decisiones acordadas](docs/decisiones.md).
- [Matriz de las 15 pruebas manuales](docs/pruebas-manuales.md).
- [Criterios de aceptación y listas de verificación](docs/cumplimiento.md).
- [Organización de evidencias](docs/evidencias/README.md).
- [Resultado real de pruebas automatizadas](docs/evidencias/pruebas-automatizadas.md).

Cada captura debe acompañarse de una descripción, fecha, caso y commit. Oculta contraseñas y tokens. No marcar pruebas pendientes como aprobadas ni confundir ejemplos de este README con resultados reales.

El commit publicado, las capturas y la demostración deben corresponder a la misma versión. La rúbrica válida tiene siete criterios que suman 100 puntos; Docker no aporta puntaje.

## Errores frecuentes

| Síntoma | Revisión |
| --- | --- |
| UnsupportedClassVersionError o release 17 no soportado | JAVA_HOME y java/mvn deben apuntar a JDK 17 |
| Access denied / Communications link failure | MySQL activo, host/puerto, usuario, contraseña y permisos |
| Unknown database vetturno | Crear la base del paso 3 de la guia local; en Docker, revisar el inicio de MySQL |
| JWT_SECRET sin resolver o clave inválida | Revisar JWT_SECRET en la configuracion local o en .env, segun la guia elegida |
| 401 al consultar | Token vigente, Authorize y prefijo Bearer correcto |
| 403 al crear veterinario | Login como ADMIN tras promoción controlada |
| 400 por fecha | Hora futura en Colombia, sin segundos ni offset |
| 400 por referencia | Usar los ids realmente devueltos por la API |
| Puerto 8080 ocupado | Detener la otra instancia o configurar SERVER_PORT localmente |

## Aprendizaje y defensa del proyecto

El proyecto permite practicar Spring Boot mediante una API organizada en controladores, servicios y repositorios. Para la defensa, explica el recorrido de una solicitud, las relaciones JPA, las validaciones, el manejo de errores y los permisos USER/ADMIN. Los conceptos de apoyo estan en [modelo y responsabilidades](docs/modelo.md).
