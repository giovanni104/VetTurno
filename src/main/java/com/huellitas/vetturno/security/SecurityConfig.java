package com.huellitas.vetturno.security;

import jakarta.servlet.DispatcherType;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Reune las reglas de seguridad de la API.
 * Aqui indicamos que rutas son publicas, cuales necesitan login y cuales exigen ADMIN.
 * Los metodos con Bean crean objetos que Spring comparte con las otras clases.
 */
@Configuration
public class SecurityConfig {
    /**
     * BCrypt transforma la contrasena en un hash para guardarla.
     * En el login permite comprobarla sin guardar ni recuperar el texto original.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Prepara la comprobacion del email y la contrasena durante el login.
     * UsuarioDetailsService busca la cuenta y el encoder compara la contrasena.
     */
    @Bean
    public AuthenticationManager authenticationManager(UsuarioDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    /**
     * Define los filtros y los permisos que se aplican antes del controlador.
     * Las reglas de rutas se revisan en orden: la primera que coincide se utiliza.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwt,
                                                   SecurityErrorHandler errors) throws Exception {
        return http
                // Esta API recibe el JWT en Authorization y no usa cookies de sesion.
                // Por ese motivo desactivamos CSRF en esta configuracion.
                .csrf(AbstractHttpConfigurer::disable)
                // El login se hace con nuestro endpoint, sin formulario ni HTTP Basic.
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                // STATELESS: cada solicitud debe traer su token; no guardamos una sesion.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Respondemos con JSON cuando falta autenticacion o permiso.
                .exceptionHandling(handler -> handler.authenticationEntryPoint(errors).accessDeniedHandler(errors))
                .authorizeHttpRequests(auth -> auth
                        // Permite procesar un error interno sin exigir otro login.
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        // Esta regla va antes de la general: solo ADMIN crea veterinarios.
                        .requestMatchers(HttpMethod.POST, "/api/veterinarios").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated())
                // Revisamos nuestro JWT antes del filtro de usuario y contrasena.
                .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Evita que el servidor registre otra vez el filtro marcado como Component.
     * Ya lo agregamos a Spring Security; debe ejecutarse desde esa cadena.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthFilter> jwtFilterRegistration(JwtAuthFilter filter) {
        // Evita ejecutar el filtro tanto en el contenedor como en Spring Security.
        FilterRegistrationBean<JwtAuthFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
