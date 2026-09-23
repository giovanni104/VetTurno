# Postman: practicar VetTurno paso a paso

Esta coleccion tiene **18 solicitudes que cubren los 11 endpoints**. Se utilizan de una en una, pulsando **Send**. No contiene scripts, generacion automatica de datos ni pruebas programadas.

## 1. Iniciar e importar

Inicia la API siguiendo la [guia de Docker](ejecucion-profesora.md).

En Postman, pulsa **Import** e importa estos dos archivos de la carpeta postman:

- **VetTurno.postman_collection.json**
- **VetTurno-Local.postman_environment.json**

Selecciona el entorno **VetTurno - Local sencillo**. La coleccion se llama **VetTurno - Practica paso a paso**.

El entorno solo contiene tres valores:

| Variable | Valor |
| --- | --- |
| baseUrl | http://localhost:8080 |
| tokenUsuario | Aqui pegaras el token de Paula |
| tokenAdmin | Aqui pegaras el token de Marta |

Los textos entre llaves, como `{{baseUrl}}`, toman su valor del entorno seleccionado.

## 2. Registrar e iniciar sesion

Abre la carpeta **1 - Registro y login**.

1. En **01 - Registrar a Paula**, abre **Body** para ver el JSON y pulsa **Send**. Espera **200 OK**.
2. En **02 - Login de Paula**, pulsa **Send**. La respuesta tendra esta forma:

```json
{
  "token": "un-token-largo"
}
```

3. Copia solo el valor del token, sin comillas ni la palabra Bearer.
4. Abre el entorno y pega el valor en **tokenUsuario**. Guarda el cambio.
5. Ejecuta **03 - Registrar a Marta**. Espera **200 OK**. Marta todavia es USER.

Los correos y contraseñas de los cuerpos son ejemplos de practica. Si un correo ya esta registrado, utiliza el login con su contraseña original o cambia el correo tanto en registro como en login. Un segundo registro del mismo correo devuelve 400.

## 3. Dar permiso ADMIN a Marta

Desde la carpeta del proyecto, abre MySQL:

```text
docker compose exec mysql mysql -u vetturno -p vetturno
```

Introduce la contraseña **MYSQL_PASSWORD** de tu archivo .env. Esta es la clave de la base, no la contraseña de Marta.

Ejecuta este SQL. Si cambiaste el correo de Marta en Postman, cambialo aqui tambien:

```sql
START TRANSACTION;
UPDATE usuarios SET rol = 'ADMIN'
WHERE email = 'marta.postman@example.com' AND rol = 'USER';
SELECT ROW_COUNT() AS filas_actualizadas;
SELECT id, email, rol FROM usuarios
WHERE email = 'marta.postman@example.com';
```

Comprueba la cuenta y la fila actualizada. Si es correcto, ejecuta `COMMIT;`; si no, `ROLLBACK;`. Sal con `exit;`. Si ya era ADMIN, no es necesario promoverla otra vez.

Ahora ejecuta **04 - Login de Marta**. Copia su token en **tokenAdmin** del entorno.

## 4. Crear responsable, mascota y veterinario

Continua en orden:

| Solicitud | Que debes hacer | Respuesta |
| --- | --- | --- |
| 05 - Registrar responsable | Enviar y anotar el id de Laura | 201 |
| 06 - Consultar responsables | Buscar a Laura en la lista | 200 |
| 07 - Registrar mascota | Cambiar propietarioId por el id de Laura; enviar y anotar el id de Luna | 201 |
| 08 - Consultar mascotas | Comprobar que Luna pertenece a Laura | 200 |
| 09 - Registrar veterinario como ADMIN | Enviar y anotar el id de Andres | 201 |
| 10 - Consultar veterinarios | Buscar a Andres | 200 |

**Los ids 1 del ejemplo no son valores garantizados.** Si Laura tiene id 7, el cuerpo de la mascota debe contener `"propietarioId": 7`. Edita el numero en **Body**, sin comillas.

Cada solicitud ya indica el token que utiliza en **Authorization**: normalmente tokenUsuario; el registro de veterinario usa tokenAdmin. No debes cambiar manualmente el token entre estas solicitudes.

## 5. Crear y consultar citas

En **11 - Registrar cita a las 10**:

- Cambia mascotaId por el id de Luna.
- Cambia veterinarioId por el id de Andres.
- Revisa fechaHora: debe estar en el futuro, en hora de Colombia y con segundos en cero.
- Pulsa Send y espera **201**.

La fecha 2030-10-20 es un ejemplo. Puedes usar otro dia futuro, como mañana, manteniendo el formato `AAAA-MM-DDTHH:mm:00`.

En **12 - Registrar cita a las 09**, usa los mismos ids y el mismo dia, pero a las 09:00. Ambas horas deben ser futuras. Espera **201**.

Ejecuta **13 - Consultar agenda**. La cita de las 09:00 debe aparecer antes de la de las 10:00, aunque la creaste despues.

En **14 - Consultar agenda por veterinario**, cambia el **1 al final de la URL** por el id de Andres. Debe responder **200** con sus citas.

## 6. Comprobar errores y permisos

| Solicitud | Preparacion | Resultado esperado |
| --- | --- | --- |
| 15 - Responsable con datos invalidos | Enviar el ejemplo tal como esta | 400 con errores de nombre, telefono y email |
| 16 - Cita con horario cruzado | Copiar exactamente el Body enviado en la solicitud 11, incluidos ids y fecha | 400 porque el horario del veterinario esta ocupado |
| 17 - Registrar veterinario como USER | Tener el token de Paula en tokenUsuario | 403 por falta de permiso |
| 18 - Consultar agenda sin token | Enviar; ya tiene No Auth | 401 porque falta autenticacion |

En estas solicitudes, recibir 400, 403 o 401 es el resultado correcto. Mira el codigo de estado y el JSON de respuesta; no hay scripts que marquen pruebas en verde.

El manejador global convierte validaciones y reglas de negocio en 400. La seguridad comprueba identidad y permisos mediante filtros antes del controlador; por eso necesita sus propias respuestas 401/403. Un fallo imprevisto debe devolver 500 con mensaje generico; no se agrega un endpoint para provocarlo.

## 7. Verificar que las citas permanecen

Anota los ids de las citas de la solicitud 13 y ejecuta en la terminal:

```text
docker compose restart api
docker compose logs --tail 50 api
```

Espera a que aparezca **Started VetTurnoApplication** y vuelve a enviar **13 - Consultar agenda**. Los ids y datos deben ser los mismos.

## Si algo no funciona

- **No conecta:** confirma que la API esta iniciada y baseUrl es correcto.
- **401 inesperado:** revisa el entorno seleccionado y el token. Dura una hora; repite el login y pega el nuevo valor.
- **403 con Marta:** comprueba que la cuenta tenga ADMIN y vuelve a iniciar sesion.
- **400 al crear una mascota o cita:** revisa los ids, la fecha futura y que no hayas enviado esa misma cita antes.
- **Registro duplicado:** usa el login; no hace falta registrar cada vez.
- **No aparecen las variables:** selecciona VetTurno - Local sencillo.

Las creaciones agregan datos. No uses Run collection para esta guia: hay pasos manuales entre solicitudes. No publiques el entorno con tus tokens rellenados.

Para la alternativa con Java fuera de Docker, el acceso a MySQL es `docker exec -it vetturno-mysql mysql -u vetturno -p vetturno` y la clave esta en DB_PASSWORD de config/application-local.properties.

Los 18 ejemplos de esta coleccion fueron verificados contra la API. El resultado esta en [verificacion de ejemplos](evidencias/postman-manual.json).
