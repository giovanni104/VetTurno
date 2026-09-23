# Evidencias reales de VetTurno

Java 17.0.20.1, Spring Boot 4.1.0 y MySQL 8.4.11 en Docker.
Datos ficticios. Capturas tomadas de Swagger UI mediante Edge/Playwright. Las areas coloreadas ocultan credenciales, encabezados Authorization y cuerpos de autenticacion.

## Indice y descripciones

| Archivo | Descripcion |
| --- | --- |
| [pruebas-automatizadas.md](pruebas-automatizadas.md) | Resultado real de 32 pruebas, sin fallos ni omisiones. |
| [mysql-integracion.json](mysql-integracion.json) | Flujo real contra MySQL y reserva concurrente, sin JWT. |
| [inicio-java17.txt](inicio-java17.txt) | Inicio real del servidor Java 17 y conexion MySQL. |
| [arbol-paquetes.txt](arbol-paquetes.txt) | Arbol real de archivos por responsabilidad. Falta captura visual del IDE. |
| [mysql-esquema.txt](mysql-esquema.txt) | SHOW TABLES y SHOW CREATE TABLE: llaves y UNIQUE. Falta captura especifica de Workbench. |
| [mysql-roles-hash.txt](mysql-roles-hash.txt) | Promocion de una cuenta, USER/ADMIN y prefijo del hash BCrypt. |
| [mysql-duplicados.txt](mysql-duplicados.txt) | Conteo real: una sola cita por veterinario/hora. |
| [swagger-inicial.png](swagger-inicial.png) | Nombre VetTurno, endpoints y boton Authorize. |
| [swagger-autorizado.png](swagger-autorizado.png) | Swagger con autorizacion aplicada, sin revelar el JWT. |
| [swagger-casos.json](swagger-casos.json) | Entradas y respuestas reales del recorrido, credenciales ocultas. |
| [persistencia-reinicio.json](persistencia-reinicio.json) | Comparacion antes/despues: mismos 5 registros. |
| [reinicio-java17.txt](reinicio-java17.txt) | Inicio de una nueva JVM tras detener la anterior. |

Las capturas swagger-caso-02 a swagger-caso-14 corresponden a la [matriz](../pruebas-manuales.md).
Las capturas admin-registro y admin-login muestran el alta y nuevo login de la administradora.
La captura orden-previa muestra una cita a las 09:00 creada despues de la cita de las 10:00; el filtro la devuelve primero.
Las capturas 15-antes y 15-despues muestran persistencia con Authorize tras el reinicio.

## Pendientes de la entrega academica

- Capturas especificas de tablas, llaves y hash en MySQL Workbench.
- Captura del arbol de paquetes en el IDE.
- Defensa y repeticion manual del estudiante con el commit publicado.

No se presenta el recorrido asistido como una demostracion manual realizada por el estudiante. Los XML originales de Surefire quedan en target y no se publican porque incluyen informacion del entorno.
