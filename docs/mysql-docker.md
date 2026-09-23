# MySQL local en Docker

Configuracion solicitada por el estudiante para ejecutar el taller. La aplicacion Java se ejecuta fuera de Docker.

| Campo de MySQL Workbench | Valor |
| --- | --- |
| Connection Name | VetTurno local |
| Connection Method | Standard (TCP/IP) |
| Hostname | 127.0.0.1 |
| Port | 3307 |
| Username | vetturno |
| Default Schema | vetturno |
| Password | Valor DB_PASSWORD de config/application-local.properties |

La contrasena se guarda solo localmente. No se publica en este documento ni en Git.
El usuario vetturno tiene permisos sobre su base; no es el usuario root del servidor.

## Conexion JDBC local

```text
jdbc:mysql://127.0.0.1:3307/vetturno?connectionTimeZone=America/Bogota&allowPublicKeyRetrieval=true&useSSL=false
```

La configuracion sin TLS es para este contenedor ligado a 127.0.0.1. No es una configuracion de despliegue remoto.

## Preparacion reproducible

1. Instalar e iniciar Docker Desktop.
2. Ejecutar scripts/Preparar-MySql.ps1 desde PowerShell.
3. Esperar a que docker compose ps muestre healthy.
4. Iniciar la API con JDK 17 siguiendo el README.
5. Abrir Workbench con los datos de la tabla y hacer Test Connection.
6. Actualizar Schemas para inspeccionar tablas y llaves creadas por Hibernate.

El script usa la [imagen oficial de MySQL](https://hub.docker.com/_/mysql), version 8.4.11, con credenciales aleatorias. Archivos privados:
- .local/mysql.env: configuracion inicial del contenedor, incluida la clave root.
- config/application-local.properties: conexion de la aplicacion y secreto JWT.

Conservar estos archivos para reutilizar el volumen. Cambiar las variables del contenedor despues de inicializarlo no cambia automaticamente las cuentas persistidas.

## Operacion habitual

```powershell
docker compose ps
docker compose stop mysql
docker compose start mysql
docker compose logs --tail 30 mysql
```

Contenedor: vetturno-mysql. Volumen: vetturno-mysql-data.
El puerto 3307 permite conservar los otros contenedores del equipo que usan 3306.
Detener y volver a iniciar el contenedor conserva los datos del volumen.

## Evidencia del esquema

En Workbench, ejecutar y guardar capturas con una descripcion:

```sql
USE vetturno;
SHOW TABLES;
SHOW CREATE TABLE mascotas;
SHOW CREATE TABLE citas;
SELECT id, email, rol FROM usuarios;
```

Para la evidencia BCrypt, consultar solo la cuenta de prueba prevista y ocultar su contrasena original. Nunca capturar JWT_SECRET ni el archivo de credenciales.

La creacion automatica de la base por Docker sustituye el paso manual de crear la base vacia, por solicitud expresa del estudiante. El flujo de datos de negocio sigue realizandose por API.
