# Guia facil: VetTurno con Docker en Windows o Linux

## 1. Tener Docker listo

En Windows, instala y abre Docker Desktop con contenedores Linux. En Linux, instala Docker Engine con el complemento Docker Compose, o Docker Desktop.

Abre una terminal y comprueba:

```text
docker --version
docker compose version
docker info
```

Docker debe estar iniciado. En Linux, tu usuario debe tener permiso para ejecutar Docker. No necesitas instalar Java, Maven ni MySQL: se ejecutan dentro de los contenedores.

## 2. Descargar el proyecto

En [GitHub](https://github.com/giovanni104/VetTurno), pulsa **Code > Download ZIP** y descomprime. Tambien puedes usar Git:

```text
git clone https://github.com/giovanni104/VetTurno.git
cd VetTurno
```

Abre la terminal en la carpeta que contiene **compose.yaml**, **Dockerfile** y **pom.xml**. Si descargaste ZIP, puede llamarse VetTurno-main.

## 3. Preparar un solo archivo

Con el explorador de archivos, duplica **.env.example** y llama a la copia **.env**. En Linux, activa la visualizacion de archivos ocultos si no lo ves. En Windows, muestra las extensiones y comprueba que no se llame .env.txt.

Tambien puedes copiarlo desde la terminal:

| Terminal | Comando |
| --- | --- |
| Windows (PowerShell o CMD) | `copy .env.example .env` |
| Linux (Bash) | `cp .env.example .env` |

Para probar el taller localmente, puedes dejar los valores de ejemplo. Son claves publicas de demostracion, no las credenciales privadas del estudiante. No deben utilizarse en un servidor publico.

No necesitas las carpetas .local, tmp ni target. Tampoco necesitas crear config/application-local.properties.

## 4. Iniciar la aplicacion y la base de datos

Este comando es el mismo en Windows y Linux:

```text
docker compose up --build -d
```

La primera ejecucion descarga las imagenes y dependencias, compila el proyecto, ejecuta las pruebas que no necesitan MySQL, crea la base y arranca la API. Puede tardar varios minutos; requiere Internet. La prueba de integracion se omite durante la construccion porque la base aun no esta iniciada.

Comprueba el estado y el inicio:

```text
docker compose ps
docker compose logs --tail 50 api
```

MySQL debe aparecer **healthy** y la API **Up**. En los logs debe aparecer **Started VetTurnoApplication**. Que el contenedor este Up no significa por si solo que Spring ya termino de iniciar.

## 5. Abrir Swagger y probar

Abre [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).

1. En **POST /api/auth/register**, pulsa **Try it out** y registra una cuenta:

```json
{
  "email": "paula@example.com",
  "password": "PaulaTaller2026"
}
```

2. Ejecuta **POST /api/auth/login** con esos mismos datos.
3. Copia el valor de **token** de la respuesta.
4. Pulsa **Authorize**, pega solo el token y confirma.
5. Ejecuta **GET /api/propietarios**. Debe responder **200**; al principio devolvera `[]`.

El token dura una hora. Si vence, vuelve a iniciar sesion. La base empieza vacia; el registro siempre asigna USER.

Para probar todos los endpoints, importa la coleccion y el entorno de la carpeta **postman** y sigue la [guia de Postman](guia-postman.md).

## 6. Habilitar la cuenta ADMIN

Registra otra cuenta, por ejemplo marta@example.com, mediante Swagger. Luego ejecuta:

```text
docker compose exec mysql mysql -u vetturno -p vetturno
```

Cuando pida la contraseña, escribe el valor de **MYSQL_PASSWORD** de tu archivo .env. Al escribirla no se muestran caracteres.

Dentro de MySQL, ejecuta:

```sql
START TRANSACTION;
UPDATE usuarios SET rol = 'ADMIN'
WHERE email = 'marta@example.com' AND rol = 'USER';
SELECT ROW_COUNT() AS filas_actualizadas;
SELECT id, email, rol FROM usuarios WHERE email = 'marta@example.com';
```

Comprueba que se actualizo exactamente la cuenta deseada. Si todo es correcto, ejecuta `COMMIT;`; si no, `ROLLBACK;`. Para salir, escribe `exit;`.

Vuelve a iniciar sesion como Marta y reemplaza el token en Swagger. Ahora puede registrar veterinarios. Si usaste Postman, usa el correo de la ronda y su variable **sqlPromocionAdmin**, en lugar del ejemplo anterior.

## 7. Detener, continuar y comprobar persistencia

Para detener sin borrar datos:

```text
docker compose down
```

Para volver a iniciar:

```text
docker compose up -d
```

Para reiniciar solo la API y verificar que las citas permanecen:

```text
docker compose restart api
```

Espera al mensaje de inicio y consulta de nuevo la agenda. El volumen **mysql-data** conserva los datos. No agregues `-v` al comando down: esa opcion elimina el volumen y sus datos.

Si cambias codigo, vuelve a usar `docker compose up --build -d`.

## Problemas habituales

| Problema | Que revisar |
| --- | --- |
| docker no responde | Instala Docker y comprueba que este iniciado |
| Permiso denegado en Linux | Revisa los permisos de tu usuario para Docker |
| Mensaje que pide .env | Copia .env.example como .env junto a compose.yaml |
| Puerto 8080 ocupado | Deten otra API que este usando ese puerto y repite el arranque |
| Swagger no abre todavia | Revisa `docker compose logs --tail 50 api` y espera al inicio |
| MySQL no esta healthy | Revisa `docker compose logs --tail 50 mysql` |
| Access denied tras cambiar .env | El volumen conserva las contraseñas iniciales; recupera los valores con los que lo creaste |
| 401 | Inicia sesion y autoriza con un token vigente |
| 403 al crear veterinario | Usa una cuenta ADMIN tras la promocion controlada |

Los comandos usan los valores de base y usuario de la plantilla. Si los cambias en .env, ajusta tambien el comando del cliente MySQL.

## Que contiene la solucion Docker

- **Dockerfile:** receta de dos etapas. Primero genera el JAR con Maven y Java 17; despues copia solo ese JAR a una imagen de ejecucion Java 17.
- **Imagen:** resultado construido a partir del Dockerfile.
- **Contenedor:** una ejecucion de la imagen.
- **compose.yaml:** inicia api y mysql juntos. La API espera a que MySQL este healthy.
- **.dockerignore:** limita la construccion al codigo y al POM; excluye credenciales y archivos locales.
- **.env:** valores que Compose inyecta al crear los contenedores; no se copian dentro de la imagen.

Dentro de Docker, la API conecta a **mysql:3306**: mysql es el nombre del servicio. El puerto de MySQL no se publica en el equipo; puedes inspeccionarlo con el cliente del paso 6. Swagger se publica en 127.0.0.1:8080.

Esta actividad es extra y vale cero puntos. La generacion del JAR se realiza dentro de Docker para que los pasos sean iguales en Windows y Linux. La evidencia de ejecucion se registra en [verificacion Docker](evidencias/docker.md).

Referencias: [construccion por etapas](https://docs.docker.com/build/building/multi-stage/) y [orden de inicio en Compose](https://docs.docker.com/compose/how-tos/startup-order/).
