# Seguimiento del taller

Esta lista distingue codigo preparado de evidencia ejecutada. MySQL, Java 17, Swagger y reinicio ya fueron verificados. Permanecen abiertas las capturas especificas de Workbench y la demostracion manual del estudiante.

## Criterios de aceptacion de Dona Marta

| Criterio | Implementacion | Comprobacion de entrega |
| --- | --- | --- |
| Mascota relacionada con responsable correcto | MascotaService resuelve propietarioId; FK obligatoria | Verificado en MySQL y Swagger, caso 9; repetir en defensa manual. |
| Cita referencia mascota y veterinario existentes | CitaService resuelve ids; FK obligatorias | Verificado en MySQL y Swagger, casos 10/11 y pruebas negativas. |
| Sin pasado ni mismo veterinario/hora | @Future, reloj Colombia, consulta derivada y UNIQUE | Verificado en MySQL y Swagger 12/13; concurrencia real aprobada. |
| USER no registra veterinarios | SecurityConfig exige ADMIN | 403 verificado en prueba automatizada y Swagger (caso 6). |
| Datos persisten al reiniciar | MySQL; ddl-auto=update | Reinicio de JVM verificado; 5 citas con mismos ids/datos. |
| Swagger y README permiten probar | OpenApiConfig, Bearer y guia | Recorrido Swagger y clon remoto aprobados; defensa manual por completar. |

## Parte 1: proyecto

- [x] Proyecto de Spring Initializr con Maven, Java 17 configurado y dependencias del modulo.
- [x] Clase principal bajo com.huellitas.vetturno y paquetes por responsabilidad.
- [x] Commit inicial limpio: deb7e4e.
- [x] README con historia y ejecucion.
- [x] Registro real del inicio con Java 17 en evidencias/inicio-java17.txt.
- [ ] Captura del arbol de paquetes.
- [x] Explicacion de pom.xml y clase principal en modelo.md.

## Parte 2: persistencia

- [x] Cinco entidades, ids generados y constructores JPA.
- [x] ManyToOne y llaves foraneas en el lado muchos.
- [x] Repositorios con ids Long.
- [x] Diagrama y explicacion de JPA, Hibernate y JpaRepository.
- [ ] Capturas del SQL, tablas y llaves en Workbench.
- [x] Esquema MySQL 8.4.11 confirmado; evidencias/mysql-esquema.txt.

## Parte 3: REST y DTO

- [x] Inyeccion por constructor; controllers delegan en services.
- [x] Services sin ResponseEntity ni decisiones HTTP.
- [x] DTO de entrada/salida sin entidades ni colecciones recursivas.
- [x] Crear/listar propietarios, mascotas y veterinarios.
- [x] Peticiones y respuestas reales en evidencias/swagger-casos.json y capturas.
- [x] Evidencia de reinicio en persistencia-reinicio.json y capturas antes/despues.

## Parte 4: agenda

- [x] CitaRequest recibe ids y CitaDTO muestra nombres.
- [x] Validacion de relaciones, fecha futura y mismo horario.
- [x] Consultas derivadas de disponibilidad y filtro.
- [x] Fecha ascendente y precision de minutos.
- [x] Capturas Swagger 11, 12, 13 y 14 con resultados reales.

## Parte 5: seguridad

- [x] Usuario/Rol, email unico y BCrypt.
- [x] Registro siempre USER; nunca responde la entidad Usuario.
- [x] AuthenticationManager, JWT una hora y Bearer filter.
- [x] Stateless; POST veterinarios solo ADMIN.
- [x] Procedimiento de primer ADMIN documentado.
- [x] Capturas Swagger con token oculto y comparacion 403/201; hash BCrypt en salida SQL real.
- [ ] Captura del hash especificamente en MySQL Workbench.

## Parte 6: calidad

- [x] Restricciones en DTO y @Valid en controllers.
- [x] ApiError: status, mensaje, errores y timestamp.
- [x] Validacion/negocio 400; imprevistos 500 sin detalles sensibles.
- [x] Explicacion del manejador global frente a la seguridad.
- [x] Respuestas reales Swagger 03 y 13, con capturas y JSON.

## Parte 7: entrega

- [x] OpenAPI con identidad VetTurno y esquema Bearer.
- [x] Rutas tecnicas de Swagger publicas; negocio protegido.
- [x] Matriz de 15 pruebas preparada sin resultados inventados.
- [x] README, decisiones, modelo y guia de evidencia.
- [x] Titulos, tablas con contexto y mensajes independientes del color.
- [x] 32 pruebas aprobadas con Java 17 y MySQL real. Ver evidencias/pruebas-automatizadas.md.
- [x] Empaquetado Maven clean verify exitoso con Temurin JDK 17.0.20.1.
- [x] Captura Swagger con Authorize en evidencias/swagger-autorizado.png.
- [x] Publicacion en GitHub y ejecucion desde clon remoto con Maven Wrapper y Java 17; evidencias/clon-remoto.txt.
- [x] Las 45 fuentes del clon coinciden con las probadas; huellas en evidencias/fuentes-sha256.json.
- [ ] Repetir la demostracion manual del estudiante con el commit final publicado.

## Rúbrica

Se siguen los siete criterios de la tabla: 10 + 18 + 20 + 12 + 18 + 12 + 10 = 100.
La formula de ocho sumandos de la pagina 15 no corresponde a esa tabla.
Docker no aporta puntaje. La actividad extra usa un contenedor para la API Java 17 y otro para MySQL. Ver [guia](ejecucion-profesora.md) y [evidencia Docker](evidencias/docker.md).

## Actividad extra sin puntaje: Docker

- [x] Dockerfile con Java 17, JAR, puerto 8080 y variables externas.
- [x] .dockerignore excluye configuracion privada y archivos innecesarios.
- [x] Imagen construida y API conectada a MySQL en Docker Compose.
- [x] Login y consulta en Swagger comprobados; captura en evidencias/docker-swagger.png.
- [x] Guia con los pasos para Windows y Linux y explicacion de imagen y contenedor.
- [x] Reinicio real y persistencia verificados; 40 solicitudes y 173 aserciones aprobadas.
