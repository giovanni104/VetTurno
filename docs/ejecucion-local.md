# Ejecutar VetTurno localmente: Windows o Linux

En esta opcion instalas **Java 17 y MySQL en tu equipo**. Docker no es necesario. Si prefieres usar Docker, sigue la [otra guia de ejecucion](ejecucion-profesora.md).

## 1. Preparar el equipo

Necesitas:

- **JDK 17:** permite compilar y ejecutar Java.
- **MySQL Server 8.4:** guarda la informacion del proyecto. Debe estar instalado e iniciado.
- Internet para descargar las dependencias la primera vez.

No necesitas instalar Maven: el proyecto incluye Maven Wrapper, que lo descarga al ejecutarlo. MySQL Workbench es opcional; sirve para abrir la base de datos con una interfaz grafica.

Si aun no tienes MySQL Server, consulta la [instalacion oficial para tu sistema](https://dev.mysql.com/doc/refman/8.4/en/installing.html). Instalar solamente Workbench no instala el servidor.

## 2. Descargar y abrir el proyecto

Descarga el ZIP desde [GitHub](https://github.com/giovanni104/VetTurno) y descomprimelo.

Abre una terminal en la carpeta que contiene **pom.xml**, **mvnw** y **mvnw.cmd**. Todos los comandos de esta guia se ejecutan desde esa carpeta.

Comprueba Java:

```text
java -version
```

Debe mostrar la version **17**. Comprueba tambien la version que utilizara Maven:

| Sistema | Comando |
| --- | --- |
| Windows (PowerShell o CMD) | `.\mvnw.cmd -version` |
| Linux | `sh ./mvnw -version` |

Maven debe indicar **Java version: 17**. Si no es asi, consulta la ayuda al final antes de continuar.

## 3. Crear la base de datos

Abre MySQL Workbench y conecta con una cuenta administradora, como root. Tambien puedes abrir el cliente desde una terminal, si el comando mysql esta disponible:

```text
mysql -u root -p
```

Introduce la contraseña que elegiste al instalar MySQL. No es una contraseña incluida en este proyecto. En algunas instalaciones Linux, el administrador entra con `sudo mysql` en lugar de usar contraseña.

Ejecuta estas instrucciones **dentro de MySQL o en una pestaña SQL de Workbench**, no directamente en PowerShell o Bash:

```sql
CREATE DATABASE vetturno
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER 'vetturno'@'localhost'
  IDENTIFIED BY 'VetTurnoLocal2026';

GRANT ALL PRIVILEGES ON vetturno.*
  TO 'vetturno'@'localhost';
```

Esto crea una base vacia y un usuario que puede trabajar en ella. Las tablas las crea la aplicacion al iniciar.

Si la base o el usuario ya existen, no los borres: usa los datos de esa instalacion en el paso 4. Si usaste el cliente de terminal, escribe `exit;` para salir.

## 4. Preparar la configuracion

En la carpeta **config**, duplica **application-local.properties.example** y cambia el nombre de la copia a **application-local.properties**.

Puedes hacerlo con el explorador de archivos o con uno de estos comandos:

| Sistema | Comando |
| --- | --- |
| Windows | `copy config\application-local.properties.example config\application-local.properties` |
| Linux | `cp config/application-local.properties.example config/application-local.properties` |

Abre la copia con un editor de texto. Si seguiste el SQL del paso 3 y MySQL utiliza el puerto habitual 3306, **puedes dejar los valores incluidos**:

```properties
DB_URL=jdbc:mysql://localhost:3306/vetturno?connectionTimeZone=America/Bogota&allowPublicKeyRetrieval=true&useSSL=false
DB_USER=vetturno
DB_PASSWORD=VetTurnoLocal2026
JWT_SECRET=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=
SHOW_SQL=false
```

- **DB_URL:** direccion, puerto y nombre de la base.
- **DB_USER / DB_PASSWORD:** usuario y contraseña de MySQL.
- **JWT_SECRET:** clave con la que la aplicacion firma los tokens del login.
- **SHOW_SQL:** si vale true, muestra en la consola las consultas a la base.

Son valores de demostracion para probar el taller en tu equipo. Si tienes otra contraseña, usuario o puerto, cambia esos datos en la copia. No uses estas claves publicas para un servidor en Internet.

No necesitas crear .env para esta opcion. No sobrescribas un archivo de configuracion que ya funcione.

## 5. Iniciar VetTurno

Ejecuta el comando de tu sistema:

| Sistema | Comando |
| --- | --- |
| Windows | `.\mvnw.cmd spring-boot:run` |
| Linux | `sh ./mvnw spring-boot:run` |

La primera vez puede tardar unos minutos. Espera el mensaje **Started VetTurnoApplication** y deja la terminal abierta.

Abre [Swagger UI](http://localhost:8080/swagger-ui/index.html). Es la pagina para probar la API.

## 6. Probar una consulta

1. En **POST /api/auth/register**, pulsa **Try it out**, envia un email y una contraseña de al menos ocho caracteres.
2. En **POST /api/auth/login**, envia esos mismos datos.
3. Copia el valor de **token**, sin comillas.
4. Pulsa **Authorize**, pega el token y confirma.
5. Ejecuta **GET /api/propietarios**. Debe responder **200**; si no has creado responsables, veras `[]`.

Para recorrer todos los endpoints, sigue la [guia de Postman](guia-postman.md). En su paso de ADMIN, abre MySQL con Workbench o `mysql -u vetturno -p vetturno`, en lugar del comando Docker. Usa DB_PASSWORD de tu configuracion local y ejecuta el mismo SQL indicado en esa guia.

## 7. Detener y volver a iniciar

Pulsa **Ctrl+C** en la terminal donde corre VetTurno. Para iniciarlo otra vez, repite el comando del paso 5.

Los datos permanecen en MySQL. Para comprobarlo, registra una cita, anota su id, reinicia la aplicacion y consulta la agenda de nuevo.

## Ayuda si algo falla

| Mensaje o problema | Que hacer |
| --- | --- |
| Java no es 17 | Ajusta JAVA_HOME como se explica abajo y vuelve a comprobar Maven |
| mysql no se reconoce | Abre la conexion desde Workbench; el comando puede no estar en PATH |
| Access denied | Revisa DB_USER y DB_PASSWORD; deben corresponder al usuario creado |
| Communications link failure | Comprueba que MySQL Server este iniciado y que el puerto sea correcto |
| Unknown database vetturno | Crea la base del paso 3 o revisa su nombre en DB_URL |
| JWT_SECRET no encontrado | Comprueba el nombre del archivo y ejecuta Maven desde la carpeta de pom.xml |
| Puerto 8080 ocupado | Deten otra instancia de VetTurno, incluida la API de Docker si esta activa |

Si tienes varios Java instalados, indica la carpeta real de tu JDK 17. Las rutas siguientes son ejemplos que debes reemplazar:

**Windows, PowerShell:**

```powershell
$env:JAVA_HOME = 'C:\ruta\de\tu\jdk-17'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
.\mvnw.cmd -version
```

**Linux, Bash:**

```bash
export JAVA_HOME=/ruta/de/tu/jdk-17
export PATH="$JAVA_HOME/bin:$PATH"
sh ./mvnw -version
```

Estos cambios solo afectan a esa terminal. Cuando Maven muestre Java 17, continua con el arranque.
