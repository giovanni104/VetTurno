# Matriz de las 15 pruebas del taller

Recorrido realizado en Swagger UI con navegador Edge controlado por Playwright, Java 17 y MySQL 8.4.11 real. Los estados y las capturas son reales. Es una verificacion asistida; el estudiante debe repetir el recorrido para su demostracion manual. Las capturas especificas de Workbench siguen pendientes.

[Resultados HTTP completos con credenciales ocultas](evidencias/swagger-casos.json).
[32 pruebas automatizadas aprobadas](evidencias/pruebas-automatizadas.md).

| # | Escenario | Esperado | Resultado real | Evidencia |
| --- | --- | --- | --- | --- |
| 1 | Iniciar con MySQL disponible | Servidor activo y esquema accesible | APROBADO: Java 17, MySQL 8.4.11, puerto 8080 | [Inicio](evidencias/inicio-java17.txt), [esquema](evidencias/mysql-esquema.txt) |
| 2 | Registro valido de Paula | 200 y token; BCrypt en MySQL | APROBADO: 200, USER y hash BCrypt | [Swagger](evidencias/swagger-caso-02.png), [hash oculto parcialmente](evidencias/mysql-roles-hash.txt) |
| 3 | Registro con email invalido y clave corta | 400 con ambos errores | APROBADO: 400, email y password | [Swagger](evidencias/swagger-caso-03.png) |
| 4 | Login valido | 200 y JWT vigente | APROBADO: 200 | [Swagger](evidencias/swagger-caso-04.png) |
| 5 | GET /api/citas sin token | 401 | APROBADO: 401 | [Swagger](evidencias/swagger-caso-05.png) |
| 6 | POST /api/veterinarios con USER | 403 | APROBADO: 403 | [Swagger](evidencias/swagger-caso-06.png) |
| 7 | POST /api/veterinarios con ADMIN | 201 y persistencia | APROBADO: 201 tras promocion controlada y nuevo login | [Swagger](evidencias/swagger-caso-07.png) |
| 8 | Crear propietario valido | 201 y DTO plano | APROBADO: 201 | [Swagger](evidencias/swagger-caso-08.png) |
| 9 | Crear mascota con propietario existente | 201 y relacion correcta | APROBADO: 201 y propietarioId correcto | [Swagger](evidencias/swagger-caso-09.png) |
| 10 | Crear mascota con propietario inexistente | 400, sin insertar fila | APROBADO: 400 | [Swagger](evidencias/swagger-caso-10.png), [conteo MySQL: cero filas](evidencias/mysql-mascota-invalida.txt) |
| 11 | Crear cita futura con referencias validas | 201 y cita persistida | APROBADO: 201 | [Swagger](evidencias/swagger-caso-11.png) |
| 12 | Crear cita pasada | 400 | APROBADO: 400 | [Swagger](evidencias/swagger-caso-12.png) |
| 13 | Repetir veterinario y horario | 400, una sola cita | APROBADO: 400 y cantidad 1 en MySQL | [Swagger](evidencias/swagger-caso-13.png), [conteo](evidencias/mysql-duplicados.txt) |
| 14 | Filtrar citas por veterinario | 200, solo coincidencias, fecha ascendente | APROBADO: 200, dos citas propias, 09:00 antes de 10:00 | [Swagger](evidencias/swagger-caso-14.png) |
| 15 | Reiniciar y usar Swagger con Authorize | Datos persisten y flujo protegido funciona | APROBADO: 5 citas con mismos ids y datos tras reinicio de JVM | [Antes](evidencias/swagger-caso-15-antes.png), [despues](evidencias/swagger-caso-15-despues.png), [comparacion](evidencias/persistencia-reinicio.json) |

## Repeticion manual para la defensa

1. Seguir el README y confirmar docker compose ps saludable.
2. Abrir Swagger, registrar a Paula y Marta con datos ficticios y ocultar sus contrasenas.
3. Probar los casos publicos; pegar el token de Paula en Authorize.
4. Probar el 403. Promover solo a Marta desde Workbench; iniciar sesion de nuevo y usar su token.
5. Crear propietario, mascota y citas con los ids recibidos, sin suponer que son 1.
6. Repetir el horario y verificar en Workbench que hay una sola fila.
7. Consultar por veterinario y comprobar el orden.
8. Guardar ids, reiniciar la API y comprobar los mismos datos con Authorize.
9. Registrar fecha y commit de la demostracion, y agregar las capturas Workbench pendientes.

## Alcance de las pruebas adicionales

JWT manipulado/vencido, registro con rol ADMIN que conserva USER, JSON mal formado, segundos/fracciones, referencias inexistentes y error 500 generico se verifican mediante pruebas automatizadas. Dos solicitudes concurrentes contra MySQL produjeron un 201 y un 400, con una sola fila. La prueba de concurrencia no es una simulacion en memoria.
