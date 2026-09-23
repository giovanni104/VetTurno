# Modelo y responsabilidades

```mermaid
erDiagram
    PROPIETARIO ||--o{ MASCOTA : tiene
    MASCOTA ||--o{ CITA : recibe
    VETERINARIO ||--o{ CITA : atiende
    PROPIETARIO {
        bigint id PK
        varchar nombre
        varchar telefono
        varchar email
    }
    MASCOTA {
        bigint id PK
        varchar nombre
        varchar especie
        varchar raza
        bigint propietario_id FK
    }
    VETERINARIO {
        bigint id PK
        varchar nombre
        varchar especialidad
    }
    CITA {
        bigint id PK
        datetime fecha_hora
        varchar motivo
        bigint mascota_id FK
        bigint veterinario_id FK
    }
    USUARIO {
        bigint id PK
        varchar email UK
        varchar password
        varchar rol
    }
```

Ademas, citas tiene UNIQUE(veterinario_id, fecha_hora). Usuario representa al personal que utiliza la API; no es un propietario ni un veterinario. El doctor Andres no inicia sesion en este MVP.

La llave foranea queda del lado muchos: mascotas.propietario_id, citas.mascota_id y citas.veterinario_id. Las relaciones son unidireccionales porque los casos de uso no necesitan navegar colecciones desde Propietario o Veterinario. Los DTO evitan exponer entidades y ciclos JSON.

## Flujo de una cita

HTTP POST /api/citas -> Spring Security -> CitaController (@Valid) -> CitaService -> repositorios -> MySQL -> CitaDTO -> 201.

El servicio comprueba fecha, precision de minutos, mascota, veterinario y horario. La base de datos garantiza integridad incluso si dos solicitudes llegan simultaneamente. La conversion a DTO ocurre dentro de la transaccion.

## Conceptos para la defensa

- pom.xml declara dependencias, Java y construccion; VetTurnoApplication inicia Spring Boot.
- application.properties configura la ejecucion: MySQL, Hibernate, JWT y errores.
- JPA define el contrato de persistencia; Hibernate lo implementa; JpaRepository ofrece operaciones de acceso a datos.
- El controller traduce HTTP; el service aplica reglas; el repository accede a MySQL.
- La inyeccion por constructor hace explicitas las dependencias y permite sustituirlas por dobles en pruebas.
- Autenticar comprueba identidad; autorizar decide permisos.
- BCrypt verifica una contrasena comparandola con un hash; no la descifra.
- Un JWT esta firmado, no cifrado. No contiene contrasenas ni secretos.
- Stateless significa que cada solicitud presenta su Bearer token, sin sesion HTTP.
- @Valid activa las restricciones de los DTO; declarar anotaciones sin validar la entrada no basta.
- OpenAPI describe la API; Swagger UI permite inspeccionarla y probarla.
- La persistencia despues de reiniciar requiere una demostracion con MySQL; un test con mocks no la demuestra.

## Por que el manejador global no reemplaza la seguridad

GlobalExceptionHandler traduce excepciones de los controllers y servicios: validacion y negocio a 400, autenticacion del login a 401 y fallos imprevistos a 500 con mensaje generico. No decide quien puede acceder a una ruta.

Spring Security actua antes del controller mediante su cadena de filtros. JwtAuthFilter valida firma y expiracion y carga la identidad. SecurityConfig aplica los roles. SecurityErrorHandler implementa AuthenticationEntryPoint y AccessDeniedHandler para devolver el mismo ApiError ante 401 y 403 producidos en esa cadena.

## Fuentes tecnicas consultadas

- [Spring Boot: requisitos](https://docs.spring.io/spring-boot/system-requirements.html).
- [springdoc: integracion con Spring Boot](https://springdoc.org/).
- [JJWT: documentacion oficial](https://github.com/jwtk/jjwt).
- [Spring Security: DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html).
