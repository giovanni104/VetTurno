package com.huellitas.vetturno.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI vetTurnoOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("VetTurno - Veterinaria Huellitas").version("1.0")
                        .description("API de responsables, mascotas, veterinarios y citas. "
                                + "Fechas en America/Bogota, precision de minutos. JWT vigente durante una hora."))
                .components(new Components().addSecuritySchemes("bearerAuth",
                        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
