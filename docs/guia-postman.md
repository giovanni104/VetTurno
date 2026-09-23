# Guía de Postman — VetTurno

## 1. Importar y preparar

Importa en Postman los dos archivos de la carpeta `postman`:

- `VetTurno.postman_collection.json`: solicitudes y pruebas automáticas.
- `VetTurno-Local.postman_environment.json`: variables locales.

Selecciona el entorno **VetTurno - Local**. La API debe estar iniciada y conectada a MySQL. `baseUrl` es `http://localhost:8080`; el puerto 3307 corresponde a MySQL, no a la API.

La colección contiene **40 solicitudes para los 11 endpoints**. Las respuestas 400, 401 y 403 son resultados esperados en las pruebas negativas: sus aserciones deben aparecer aprobadas.

Usa **Run collection / Collection Runner**. Activa **Keep variable values** para conservar tokens e identificadores entre fases. Ejecuta una sola iteración, en el orden definido. Los nombres de las opciones pueden variar según la versión de Postman.

## 2. Registrar cuentas: carpeta 01

Ejecuta **01 - Preparar cuentas y PAUSAR**. La primera solicitud genera una ronda con:

- Correos únicos de Paula y Marta.
- Contraseñas locales si las variables están vacías.
- Nombres únicos de responsables, mascotas y veterinarios.
- Fechas de mañana a las 09:00 y 10:00, calculadas con hora de Colombia.
- La variable `sqlPromocionAdmin`, con el correo exacto de Marta.

Ambas cuentas se registran como **USER**, aunque el cuerpo intente enviar ADMIN. La carpeta comprueba que Marta recibe 403 al registrar un veterinario y detiene el Runner.

Si ejecutas toda la colección desde el principio, también se detendrá aquí. La pausa es intencional. Al enviar solicitudes individualmente con **Send**, respeta tú el orden: el control de flujo solo se aplica al Runner/Newman.

## 3. Habilitar ADMIN en MySQL

Abre una terminal y entra al cliente MySQL del contenedor:

```powershell
docker exec -it vetturno-mysql mysql -u vetturno -p vetturno
```

Introduce la contraseña **de la base de datos** cuando la solicite. Puedes consultarla en el valor `DB_PASSWORD` de `config/application-local.properties`, generado por el script de preparacion. No es la contraseña de Paula ni la de Marta.

En las variables del entorno Postman, copia el valor de **sqlPromocionAdmin** y ejecútalo en MySQL. Revisa que se actualice exactamente una fila y que el correo de esta ronda tenga rol ADMIN. El SQL incluye una transacción y una consulta de comprobación.

Esta promoción es manual y controlada, conforme al taller. La colección no tiene un endpoint para cambiar roles. No edites el rol del JSON de registro para intentar obtener privilegios.

## 4. Ejecutar el flujo completo: carpetas 02, 03 y 04

En el Runner, selecciona **solo** estas carpetas, en orden:

1. **02 - ADMIN y veterinarios**: inicia sesión nuevamente como Marta y registra dos veterinarios.
2. **03 - Flujo de responsables mascotas y citas**: crea un responsable, su mascota y tres citas, y consulta las agendas.
3. **04 - Validaciones y permisos**: comprueba datos inválidos, referencias inexistentes, cruce de horario, permisos y autenticación.

Las respuestas guardan automáticamente los tokens y los identificadores en el entorno. No debes copiar ids a mano.

Se crea primero una cita a las 10:00 y luego otra a las 09:00 con el mismo veterinario. La consulta debe devolverlas en orden ascendente. La tercera cita usa otro veterinario a las 10:00 y debe permitirse.

No vuelvas a ejecutar la solicitud 01 entre estas fases: inicia una ronda nueva y borra sus variables anteriores. Repetir una creación de citas de la misma ronda puede producir el 400 por duplicidad esperado por la regla de negocio.

## 5. Comprobar persistencia: carpeta 05

Después de terminar las carpetas anteriores:

1. Conserva el entorno y sus valores, incluida `agendaAntesReinicio`.
2. Detén y vuelve a iniciar **la API**, manteniendo la base de datos.
3. Espera a que la API esté disponible.
4. Ejecuta **05 - Despues de reiniciar la API**.

La carpeta inicia sesión y compara las tres citas de la ronda con la instantánea guardada antes del reinicio. La colección no reinicia procesos por sí misma. Ejecutarla sin reiniciar solo comprueba que los datos siguen presentes, no demuestra persistencia tras reinicio.

No ejecutes de nuevo el registro inicial ni recrees las citas antes de esta comparación.

## 6. JWT vencido: carpeta 06, opcional

Los JWT duran **una hora**. Para comprobar vencimiento:

1. Guarda un token emitido por esta API en `expiredToken`.
2. Espera a que venza, conservando la misma clave de firma de la API.
3. Ejecuta **06 - Opcional JWT vencido**.

La respuesta esperada es 401. Si `expiredToken` está vacío, la solicitud se omite; eso no equivale a una prueba aprobada de vencimiento. Un token inventado verifica invalidez, pero no demuestra específicamente expiración.

Para continuar una sesión normal después de una hora, repite el login de Paula y/o Marta, no sus registros.

## Cobertura

| Método y ruta | Comprobación principal |
| --- | --- |
| POST /api/auth/register | Registro USER, rol enviado ignorado, validaciones y duplicado |
| POST /api/auth/login | Token y credenciales incorrectas |
| POST /api/propietarios | Creación y varios errores de campos |
| GET /api/propietarios | Consulta autenticada |
| POST /api/mascotas | Creación y propietario inexistente |
| GET /api/mascotas | Consulta y relación con propietario |
| POST /api/veterinarios | ADMIN permitido, USER prohibido y validaciones |
| GET /api/veterinarios | Consulta autenticada |
| POST /api/citas | Creación, pasado, segundos, referencias y cruce de horario |
| GET /api/citas | Orden ascendente, autenticación y persistencia |
| GET /api/citas/veterinario/{id} | Filtro, orden y veterinario inexistente |

Las pruebas verifican los códigos de respuesta, los datos creados y el formato de errores. Para el 400 de varios campos, consulta la creación inválida de propietario; para el cruce, consulta la cita duplicada.

## Variables y seguridad

| Variable | Uso |
| --- | --- |
| baseUrl | Dirección de la API |
| userEmail / userPassword | Cuenta USER de esta ronda |
| adminEmail / adminPassword | Cuenta promovida manualmente |
| userToken / adminToken | JWT guardados tras registro/login |
| propietarioId / mascotaId | Identificadores obtenidos al crear |
| veterinarioId / otroVeterinarioId | Veterinarios de la ronda |
| fechaFutura / fechaAnterior | Fechas futuras para comprobar orden y duplicidad |
| sqlPromocionAdmin | SQL limitado a la cuenta de esta ronda |
| agendaAntesReinicio | Instantánea para comprobar persistencia |
| expiredToken | Token vencido para la prueba opcional |

El entorno entregado no contiene credenciales reales. Los valores generados durante la ejecución sí son sensibles. No publiques ni subas a Git un entorno exportado con contraseñas o tokens. El marcado como secreto ayuda a ocultarlos en pantalla, pero no sustituye esa precaución.

## Interpretar fallos

- **No conecta:** revisa que la API esté iniciada en `baseUrl`.
- **401 en una consulta válida:** inicia sesión de nuevo y confirma que seleccionaste el entorno correcto.
- **403 al crear veterinarios como Marta:** revisa la promoción en MySQL y ejecuta otra vez su login.
- **400 en una creación válida:** revisa las variables y si ya ejecutaste esa creación. Si las fechas dejaron de ser futuras, comienza una ronda nueva.
- **Variables sin resolver:** selecciona el entorno y conserva sus valores en el Runner.
- **Falla la persistencia:** confirma que ejecutaste la consulta de agenda anterior al reinicio, que conservaste el entorno y que conectas a la misma base.

Cada ronda añade datos de prueba. El taller no ofrece endpoints DELETE; la colección no elimina registros ni limpia la base.

## Manejador global y seguridad

El manejador global transforma las validaciones y reglas de negocio en 400 y los fallos imprevistos en 500 con un mensaje genérico. La colección verifica errores de campos y cruce de horario.

La autenticación y la autorización se aplican en Spring Security, antes de que la solicitud llegue al controlador. Por eso el manejador global no reemplaza las reglas de seguridad: los casos sin token o con token inválido deben producir 401, y un USER que intente registrar veterinarios debe recibir 403.

No se añade un endpoint artificial para provocar un 500. Ese comportamiento se verifica con las pruebas automatizadas del proyecto. Tampoco una respuesta HTTP permite demostrar por sí sola que las contraseñas están almacenadas con BCrypt; esa comprobación corresponde a las pruebas y a la inspección controlada de la base.
