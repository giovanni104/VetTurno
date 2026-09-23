# Ejecutar VetTurno desde una descarga limpia

Esta guia sirve para clonar el repositorio o descargarlo como ZIP. No necesitas archivos del equipo del estudiante, Maven instalado ni una base de datos con registros previos.

## Requisitos en Windows

- JDK 17 instalado y JAVA_HOME apuntando a su carpeta.
- Docker Desktop instalado, iniciado y configurado para contenedores Linux.
- PowerShell y acceso a Internet para descargar Maven, dependencias y la imagen MySQL.
- Puertos locales 8080 y 3307 disponibles.

Git solo es necesario si eliges clonar. Postman y MySQL Workbench son opcionales; Swagger permite probar la API desde el navegador.

## Arranque

1. Descarga y descomprime el proyecto, o clona https://github.com/giovanni104/VetTurno.git.
2. Abre PowerShell en la carpeta que contiene pom.xml.
3. Comprueba Java e inicia el proyecto:

```powershell
java -version
powershell -ExecutionPolicy Bypass -File .\scripts\Iniciar-Proyecto.ps1
```

La opcion ExecutionPolicy se aplica a ese proceso, sin cambiar permanentemente la politica del equipo. Si tu institucion impide ejecutar scripts, sigue la alternativa con MySQL existente del README.

Si Java no es 17, establece JAVA_HOME con la ruta real de tu instalacion antes de ejecutar el script:

```powershell
$env:JAVA_HOME = 'C:\ruta\real\del\jdk-17'
```

El script verifica Java, prepara MySQL, espera a que este disponible y ejecuta el Maven Wrapper incluido. Espera el mensaje **Started VetTurnoApplication**. La primera ejecucion puede tardar por las descargas.

Abre [Swagger UI](http://localhost:8080/swagger-ui/index.html). La API utiliza 8080; MySQL utiliza 3307.

## Archivos que se generan automaticamente

| Archivo | Contenido |
| --- | --- |
| .local/mysql.env | Credenciales nuevas del contenedor MySQL |
| config/application-local.properties | Conexion de la API y secreto JWT nuevo |

Estos archivos **no deben descargarse ni copiarse del estudiante**: el script los crea en tu equipo. No se publican porque contienen secretos. Conserva tus copias al reiniciar y al reutilizar el volumen.

Para consultar la contraseña de MySQL, abre config/application-local.properties y busca DB_PASSWORD. Host: 127.0.0.1; puerto: 3307; base y usuario: vetturno. No hace falta .local/conexion-mysql.txt.

El repositorio incluye codigo fuente, configuracion de ejemplo, Maven Wrapper, Compose, scripts, coleccion Postman y guias. target se genera al compilar y tmp no participa en el arranque.

## Primer uso y cuenta ADMIN

La base comienza vacia. Hibernate crea las tablas al iniciar la API. Registra las cuentas mediante Swagger o sigue la [guia de Postman](guia-postman.md).

El registro siempre asigna USER. Para habilitar ADMIN, registra primero una cuenta y abre MySQL:

```powershell
docker exec -it vetturno-mysql mysql -u vetturno -p vetturno
```

Introduce DB_PASSWORD de tu configuracion local. Sustituye el correo del ejemplo por el que acabas de registrar:

```sql
START TRANSACTION;
UPDATE usuarios SET rol = 'ADMIN'
WHERE email = 'correo-registrado@example.com' AND rol = 'USER';
SELECT ROW_COUNT() AS filas_actualizadas;
SELECT id, email, rol FROM usuarios WHERE email = 'correo-registrado@example.com';
```

Si la cuenta es correcta y se actualizo una fila, ejecuta COMMIT; de lo contrario, ROLLBACK. Inicia sesion nuevamente con esa cuenta y usa su token. No hay usuarios ni contraseñas de aplicacion predefinidos.

## Detener y volver a iniciar

Deten la API con Ctrl+C. Opcionalmente deten MySQL:

```powershell
docker compose stop mysql
```

Para continuar, ejecuta nuevamente Iniciar-Proyecto.ps1. Se conservan tus credenciales y los datos del volumen. No elimines el volumen para un reinicio normal.

Si ya existe un contenedor o volumen VetTurno y faltan sus credenciales originales, el script se detiene para evitar asociarlo con contraseñas incorrectas. Recupera la configuracion de esa instalacion; descargar otra copia no cambia las contraseñas de un volumen existente.

## Otros sistemas y alternativa sin Docker

El script de arranque es para Windows. En macOS/Linux o con MySQL instalado directamente, sigue **Como ejecutar desde cero** en el README: crea una base vacia, copia config/application-local.properties.example, configura conexion y JWT_SECRET y ejecuta `sh ./mvnw spring-boot:run`. Necesitas Java 17 e Internet. No uses las rutas de Windows en esos sistemas.
