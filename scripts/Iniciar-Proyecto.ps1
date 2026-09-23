# Ejecutar desde PowerShell: .\scripts\Iniciar-Proyecto.ps1
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $projectRoot

$javaCommand = 'java'
if ($env:JAVA_HOME) {
    $javaCommand = Join-Path $env:JAVA_HOME 'bin/java.exe'
}
if (!(Get-Command $javaCommand -ErrorAction SilentlyContinue)) {
    throw 'Instala JDK 17 y configura JAVA_HOME con su carpeta de instalacion.'
}
$previousPreference = $ErrorActionPreference
$ErrorActionPreference = 'Continue'
$javaVersion = (& $javaCommand -version 2>&1 | Out-String)
$javaExit = $LASTEXITCODE
$ErrorActionPreference = $previousPreference
if ($javaExit -ne 0 -or $javaVersion -notmatch 'version "17[.]') {
    throw 'Este taller requiere JDK 17. Revisa JAVA_HOME y java -version.'
}
if (!(Get-Command docker -ErrorAction SilentlyContinue)) {
    throw 'Instala Docker Desktop e inicialo con contenedores Linux.'
}
docker compose version
if ($LASTEXITCODE -ne 0) { throw 'Se requiere Docker Compose incluido en Docker Desktop.' }

& (Join-Path $PSScriptRoot 'Preparar-MySql.ps1')
Write-Host 'MySQL disponible. Iniciando la API; la primera descarga de Maven puede tardar.'
Write-Host 'Cuando aparezca Started VetTurnoApplication, abre http://localhost:8080/swagger-ui/index.html'
& (Join-Path $projectRoot 'mvnw.cmd') spring-boot:run
if ($LASTEXITCODE -ne 0) { throw 'La API no pudo iniciar. Revisa el error mostrado por Maven.' }
