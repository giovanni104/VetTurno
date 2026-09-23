# Ejecucion local con Java 17 (alternativa)

### 1. Clonar y comprobar Java

```powershell
git clone https://github.com/giovanni104/VetTurno.git
cd VetTurno
java -version
.\mvnw.cmd -version
```

Ambos comandos de versión deben usar JDK 17. Si la terminal apunta a otro Java, establece JAVA_HOME a tu instalación de JDK 17 y añade su carpeta bin al PATH de esa terminal.

En macOS/Linux, usa `./mvnw` en lugar de `.\mvnw.cmd`. Si el archivo no tiene permiso de ejecución, usa `chmod +x mvnw`. El wrapper descarga Maven en la primera ejecución; requiere acceso a Internet.

### 2. Preparar MySQL

**Opción configurada en este equipo: Docker.** Con Docker Desktop iniciado, ejecuta desde la raíz:

```powershell
.\scripts\Preparar-MySql.ps1
docker compose -f compose.mysql-local.yaml ps
```

El script crea MySQL 8.4.11, la base vetturno y el usuario vetturno; genera contraseñas y JWT_SECRET aleatorios en archivos ignorados por Git. No sobrescribe una configuración existente. Usa 127.0.0.1:3307 y el volumen vetturno-mysql-data. Espera a que el estado sea healthy antes de iniciar la API. En este caso, **omite el paso de copiar la plantilla del punto 3**, porque el script ya crea la configuración.

[Conexión desde Workbench y manejo del contenedor](mysql-docker.md).

**Alternativa con una instalación MySQL existente:**

En MySQL Workbench, conectado con una cuenta que pueda crear el esquema:

```sql
CREATE DATABASE IF NOT EXISTS vetturno
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Utiliza un usuario local con permisos sobre vetturno. La aplicación requiere consultar, insertar y actualizar el esquema mediante Hibernate. No se insertan propietarios, mascotas, veterinarios ni citas a mano: el flujo se prueba por API.

Hibernate usa `ddl-auto=update`, como propone el taller, y conserva los datos al reiniciar. No cambiar a create/create-drop para la demostración.

### 3. Configurar sin publicar secretos

Si usaste Preparar-MySql.ps1, esta configuración ya existe; no la reemplaces con la plantilla. Para una instalación MySQL existente, copia la plantilla:

```powershell
Copy-Item config/application-local.properties.example config/application-local.properties
```

Edita la copia con tu conexión, usuario y contraseña. El archivo local está ignorado por Git y Spring lo carga automáticamente desde la raíz.

```properties
DB_URL=jdbc:mysql://localhost:3306/vetturno?connectionTimeZone=America/Bogota
DB_USER=TU_USUARIO_LOCAL
DB_PASSWORD=TU_CLAVE_LOCAL
JWT_SECRET=TU_SECRETO_BASE64
SHOW_SQL=false
```

Genera JWT_SECRET con 32 bytes aleatorios codificados en Base64. En PowerShell:

```powershell
$jwtBytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($jwtBytes)
[Convert]::ToBase64String($jwtBytes)
$rng.Dispose()
```

Copia el resultado únicamente al archivo local. No uses los secretos de las pruebas. También puedes definir las mismas variables en el entorno en vez de usar el archivo.

Para observar el SQL del punto de control 2, usa SHOW_SQL=true localmente. No se activa el registro de parámetros de contraseñas.

### 4. Iniciar

```powershell
.\mvnw.cmd spring-boot:run
```

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).
- OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

Comprueba el inicio sin errores y las cinco tablas en Workbench. Para detener, usa Ctrl+C en la terminal donde iniciaste el servidor.
