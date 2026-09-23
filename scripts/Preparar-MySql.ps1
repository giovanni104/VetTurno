# Genera configuracion local y levanta SOLO la base de datos.
# Ejecutar desde la raiz del repositorio: .\scripts\Preparar-MySql.ps1
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot
$secretPath = Join-Path $projectRoot '.local/mysql.env'
$configPath = Join-Path $projectRoot 'config/application-local.properties'
docker info --format '{{.OSType}}'
if ($LASTEXITCODE -ne 0) { throw 'Inicia Docker Desktop antes de continuar.' }

if (!(Test-Path -LiteralPath $secretPath)) {
    if (Test-Path -LiteralPath $configPath) { throw 'Existe configuracion local. Revisala antes de crear otra base; no se sobrescribira.' }
    $existingContainer = docker ps -a --filter 'name=^/vetturno-mysql$' --format '{{.Names}}'
    $existingVolume = docker volume ls --filter 'name=^vetturno-mysql-data$' --format '{{.Name}}'
    if ($existingContainer -or $existingVolume) { throw 'Ya existe el contenedor o volumen. Conserva sus credenciales originales.' }
    New-Item -ItemType Directory -Force -Path (Join-Path $projectRoot '.local'), (Join-Path $projectRoot 'config') | Out-Null
    function New-LocalSecret {
        $bytes = New-Object byte[] 32
        $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
        try { $generator.GetBytes($bytes) } finally { $generator.Dispose() }
        return [Convert]::ToBase64String($bytes)
    }
    $dbPassword = New-LocalSecret
    $rootPassword = New-LocalSecret
    $jwtSecret = New-LocalSecret
    $mysqlValues = @(
        'MYSQL_DATABASE=vetturno'
        'MYSQL_USER=vetturno'
        "MYSQL_PASSWORD=$dbPassword"
        "MYSQL_ROOT_PASSWORD=$rootPassword"
        'TZ=America/Bogota'
    )
    $appValues = @(
        '# Configuracion local privada. No publicar en Git.'
        'DB_URL=jdbc:mysql://127.0.0.1:3307/vetturno?connectionTimeZone=America/Bogota&allowPublicKeyRetrieval=true&useSSL=false'
        'DB_USER=vetturno'
        "DB_PASSWORD=$dbPassword"
        "JWT_SECRET=$jwtSecret"
        'SHOW_SQL=false'
    )
    [IO.File]::WriteAllLines($secretPath, $mysqlValues, [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllLines($configPath, $appValues, [Text.UTF8Encoding]::new($false))
}
docker compose up -d mysql
if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar MySQL. Revisa Docker; conserva los archivos de credenciales.' }
Write-Output 'MySQL: 127.0.0.1:3307, base vetturno, usuario vetturno.'
Write-Output 'La contrasena de la aplicacion esta en config/application-local.properties (DB_PASSWORD).'
Write-Output 'Estado: docker compose ps. La inicializacion puede tardar unos segundos.'
