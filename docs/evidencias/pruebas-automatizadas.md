# Resultado real de pruebas automatizadas

Ejecucion: 22 de septiembre de 2026, 23:55 (hora Colombia).
Entorno: Temurin JDK 17.0.20.1+1, Maven 3.9.16, Spring Boot 4.1.0.
Comando: Maven verify con VETTURNO_MYSQL_TEST=true, sin omitir pruebas.

| Suite | Aprobadas | Fallidas | Omitidas |
| --- | --- | --- | --- |
| ApiContractTest | 13 | 0 | 0 |
| JwtServiceTest | 3 | 0 | 0 |
| AuthServiceTest | 5 | 0 | 0 |
| CitaServiceTest | 10 | 0 | 0 |
| VetTurnoApplicationTests (MySQL 8.4.11 real) | 1 | 0 | 0 |
| Total | 32 | 0 | 0 |

Resultado Maven: BUILD SUCCESS. JAR ejecutable generado con Java 17.

## Limites de esta evidencia

- Mockito y MockMvc verifican reglas, HTTP y seguridad sin usar MySQL en estas 31 pruebas.
- La integracion MySQL se ejecuto con VETTURNO_MYSQL_TEST=true y todas sus verificaciones pasaron, incluida la concurrencia real: un 201, un 400 y una sola fila.
- El reporte depurado de esa ejecucion esta en mysql-integracion.json.
- El recorrido Swagger asistido, el reinicio real y las capturas se registran por separado. La demostracion manual del estudiante se mantiene distinguida de las pruebas automatizadas.
- El test de error 500 provoca deliberadamente una excepcion y verifica que su detalle no aparezca en la respuesta; el log del servidor puede contener esa excepcion simulada.

Los XML originales de Surefire quedan en target/surefire-reports y no se publican porque incluyen informacion del entorno. Esta tabla resume resultados reales sin secretos.

## Casos cubiertos

Campos invalidos multiples, JSON mal formado, USER sin permiso, ADMIN autorizado, peticiones sin token y token invalido, login incorrecto, registro sin aceptar rol, BCrypt y email repetido, JWT vigente y vencido, firma manipulada, pasado y fecha actual de Colombia, segundos/fracciones, referencias inexistentes, horario ocupado, conflicto de integridad concurrente simulado, lista vacia y ausencia de detalles internos en 500.
