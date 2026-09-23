# Consultar MySQL cuando ejecutas VetTurno con Docker

Primero inicia el proyecto siguiendo la [guia de Docker](ejecucion-profesora.md). Los comandos siguientes sirven en Windows y Linux.

## Abrir la base

Desde la carpeta de compose.yaml, ejecuta:

```text
docker compose exec mysql mysql -u vetturno -p vetturno
```

Escribe la contraseña **MYSQL_PASSWORD** de tu archivo .env. No se muestran caracteres mientras escribes. Si cambiaste el nombre del usuario o de la base en .env, cambia esos valores en el comando.

Cuando aparezca `mysql>`, puedes escribir instrucciones SQL.

## Ver tablas y relaciones

Ejecuta una instruccion cada vez, terminando con punto y coma:

```sql
SHOW TABLES;
SHOW CREATE TABLE mascotas;
SHOW CREATE TABLE citas;
SELECT id, email, rol FROM usuarios;
```

- **SHOW TABLES:** muestra las tablas creadas por la aplicacion.
- **SHOW CREATE TABLE:** muestra las columnas, relaciones y restricciones de una tabla.
- **SELECT:** consulta datos sin modificarlos.

Si todavia no hay tablas, comprueba que la API haya terminado de iniciar. Para salir del cliente, escribe `exit;`.

## Comprobar el estado

Estos comandos van en la terminal del sistema, fuera del cliente MySQL:

```text
docker compose ps
docker compose logs --tail 30 mysql
```

El primero muestra los contenedores. El segundo muestra los ultimos mensajes de MySQL.

Los datos se guardan en un volumen: una carpeta administrada por Docker que permanece al detener los contenedores. Usa `docker compose down` para detener el proyecto sin borrar ese volumen.

## Si quieres usar Workbench

La opcion Docker completa no publica un puerto de MySQL en el equipo. Para esta opcion, utiliza el cliente del primer paso.

Si ejecutas MySQL instalado en tu equipo, como explica la [guia local](ejecucion-local.md), puedes crear una conexion en Workbench con:

| Campo | Valor de ejemplo |
| --- | --- |
| Hostname | localhost |
| Port | 3306 |
| Username | vetturno |
| Password | DB_PASSWORD de config/application-local.properties |
| Default Schema | vetturno |

Usa **Test Connection** para comprobarla. Si cambiaste algun dato en tu instalacion, utiliza ese valor. Las capturas para el taller deben ocultar contraseñas y tokens.
