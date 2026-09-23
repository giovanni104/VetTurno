# Decisiones confirmadas

El PDF del taller define el alcance. Estas decisiones aclaran sus vacios sin agregar funcionalidades.

| Tema | Decision |
| --- | --- |
| Objetivo | Entrega universitaria del modulo 3, no despliegue de produccion. |
| Java / Spring Boot | Java 17 / Spring Boot 4.1.0. |
| JWT / Swagger | JJWT 0.13.0 / springdoc 3.1.0. |
| Cruce | Mismo veterinario y misma fecha/hora de inicio; no se modela duracion. |
| Registro | Publico, siempre USER. Un rol enviado por el cliente no se utiliza. |
| Primer ADMIN | Promocion controlada en MySQL y nuevo login. |
| Fecha | America/Bogota; LocalDateTime sin offset; segundos y fracciones en cero. |
| Agenda | Fecha ascendente; desempate por id para un resultado estable. |
| Contrasena | Obligatoria, minimo 8 caracteres, BCrypt. El limite tecnico de BCrypt es 72 bytes. |
| Telefono | Obligatorio; 7 a 15 digitos, con + inicial opcional. |
| Email propietario | Opcional; valido si se proporciona. |
| Email usuario | Obligatorio y unico. Se normaliza a minusculas. |
| Raza | Opcional. |
| JWT | Una hora de vigencia; luego se inicia sesion nuevamente. |
| Respuestas | Creaciones del negocio: 201; registro/login y consultas: 200. |
| Errores | Validacion/negocio: 400; autenticacion: 401; permisos: 403; imprevistos: 500 generico. |

## Decisiones tecnicas para cumplir el contrato

- Relaciones ManyToOne unidireccionales. No hay colecciones inversas innecesarias.
- DTO planos: CitaDTO muestra los nombres en mascota, propietario y veterinario.
- Restriccion unica compuesta en MySQL y consulta derivada en CitaRepository.
- Transacciones en servicios y conversion a DTO dentro de ellas; open-in-view desactivado.
- El rol se carga de MySQL en cada solicitud autenticada; no se confia en un rol del cliente.
- MySQL e Hibernate update, como propone el taller. No se incorpora H2 como sustituto.
- Orden ascendente incluye las citas pasadas; no existe filtro de fechas adicional en el alcance.
- Veterinario inexistente: regla de negocio 400; existente sin citas: 200 con lista vacia.
- La actividad extra ejecuta API y MySQL 8.4.11 con Docker Compose. Como alternativa, se puede ejecutar Java localmente y MySQL en Docker en el puerto 3307.
- Pruebas automatizadas adicionales apoyan el control de calidad; no sustituyen las 15 pruebas manuales.
- Swagger publico es la excepcion tecnica indicada en la parte 7.
