# Verificacion de la actividad extra Docker

Fecha: 23 de septiembre de 2026. Entorno: Windows con Docker Desktop y contenedores Linux. No se realizo una segunda ejecucion en un equipo Linux nativo; la guia utiliza Docker Compose y explica la copia del archivo para ambas terminales.

## Resultados

- [x] Imagen construida con Maven y Java 17; etapa final con Java 17 JRE.
- [x] Construccion aprobada: 31 pruebas ejecutadas, cero fallos; una prueba de integracion omitida durante el build.
- [x] API iniciada en 127.0.0.1:8080 y MySQL healthy en la red interna.
- [x] Credenciales inyectadas mediante variables, sin copiar .env ni configuracion privada a la imagen.
- [x] Inspeccion de /app: contiene unicamente app.jar; proceso con UID 10001.
- [x] Login en Swagger: 200. Consulta autenticada de citas: 200.
- [x] Verificacion automatizada de la API: 40 solicitudes, 173 aserciones y cero fallos; cobertura de los once endpoints. La coleccion de entrega contiene 18 solicitudes manuales, verificadas por separado en [postman-manual.json](postman-manual.json).
- [x] Reinicio real del contenedor api: las tres citas de prueba conservaron sus ids y datos.

La prueba promovio a ADMIN exclusivamente la cuenta creada para esa ronda. El JWT vencido se construyo como dato de prueba firmado con expiracion pasada. Tokens y contraseñas no se incluyen en el reporte.

## Evidencias

- [Resultados detallados de Postman y reinicio](docker-postman.json).
- [Consulta en Swagger con Authorization oculto](docker-swagger.png).
- [Datos de la imagen y huellas de configuracion](docker-verificacion.json).

La construccion toma solo Dockerfile, pom.xml y src mediante .dockerignore; no utiliza target, .local, tmp ni config/application-local.properties del equipo. La base de esta actividad usa un volumen propio de Compose y no reemplaza el volumen de la alternativa de desarrollo local.

La actividad es voluntaria y vale cero puntos. El estudiante debe poder explicar Dockerfile (receta), imagen (resultado de construir) y contenedor (ejecucion), y repetir la consulta si desea conservar su propia captura.
