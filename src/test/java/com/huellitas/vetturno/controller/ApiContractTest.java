package com.huellitas.vetturno.controller;

import com.huellitas.vetturno.config.TimeConfig;
import com.huellitas.vetturno.dto.AuthResponse;
import com.huellitas.vetturno.dto.VeterinarioDTO;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.security.*;
import com.huellitas.vetturno.service.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, PropietarioController.class, MascotaController.class,
        VeterinarioController.class, CitaController.class})
@Import({SecurityConfig.class, JwtAuthFilter.class, JwtService.class, SecurityErrorHandler.class, TimeConfig.class})
@TestPropertySource(properties = {
        "app.jwt.secret=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=",
        "app.jwt.expiration=PT1H"})
class ApiContractTest {
    @Autowired MockMvc mvc;
    @Autowired JwtService jwt;
    @MockitoBean UsuarioDetailsService users;
    @MockitoBean AuthService auth;
    @MockitoBean PropietarioService propietarios;
    @MockitoBean MascotaService mascotas;
    @MockitoBean VeterinarioService veterinarios;
    @MockitoBean CitaService citas;

    private String token(String role) {
        String email = role.toLowerCase() + "@example.com";
        when(users.loadUserByUsername(email)).thenReturn(
                User.withUsername(email).password("hash-no-usado").roles(role).build());
        return "Bearer " + jwt.generar(email);
    }

    @Test
    void registroInvalidoDevuelveVariosErroresPorCampo() throws Exception {
        mvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"email\":\"invalido\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errores.email").isArray())
                .andExpect(jsonPath("$.errores.password").isArray())
                .andExpect(jsonPath("$.timestamp").exists());
        verifyNoInteractions(auth);
    }

    @Test
    void propietarioInvalidoDevuelveTodosLosCampos() throws Exception {
        mvc.perform(post("/api/propietarios").header("Authorization", token("USER"))
                        .contentType("application/json").content("{\"nombre\":\"\",\"telefono\":\"12\",\"email\":\"mal\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.nombre").isArray())
                .andExpect(jsonPath("$.errores.telefono").isArray()).andExpect(jsonPath("$.errores.email").isArray());
        verifyNoInteractions(propietarios);
    }

    @Test
    void registroEsPublicoYElRolNoFormaParteDelContrato() throws Exception {
        when(auth.registrar(any())).thenReturn(new AuthResponse("token"));
        mvc.perform(post("/api/auth/register").contentType("application/json")
                        .content("{\"email\":\"paula@example.com\",\"password\":\"ClaveDePrueba123\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void sinTokenDevuelve401ConApiError() throws Exception {
        mvc.perform(get("/api/citas")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401)).andExpect(jsonPath("$.errores").isMap());
    }

    @Test
    void tokenInvalidoDevuelve401() throws Exception {
        mvc.perform(get("/api/citas").header("Authorization", "Bearer falso"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void userNoPuedeCrearVeterinario() throws Exception {
        mvc.perform(post("/api/veterinarios").header("Authorization", token("USER"))
                        .contentType("application/json").content("{\"nombre\":\"Andres\",\"especialidad\":\"General\"}"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(veterinarios);
    }

    @Test
    void adminPuedeCrearVeterinario() throws Exception {
        when(veterinarios.crear(any())).thenReturn(new VeterinarioDTO(1L, "Andres", "General"));
        mvc.perform(post("/api/veterinarios").header("Authorization", token("ADMIN"))
                        .contentType("application/json").content("{\"nombre\":\"Andres\",\"especialidad\":\"General\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void adminTambienPuedeConsultarAgendaSinCrearSesion() throws Exception {
        when(citas.listar()).thenReturn(List.of());
        mvc.perform(get("/api/citas").header("Authorization", token("ADMIN")))
                .andExpect(status().isOk()).andExpect(content().json("[]")).andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void cruceDeHorarioSeTraduceEn400() throws Exception {
        when(citas.crear(any())).thenThrow(new ReglaNegocioException("El veterinario ya tiene una cita en ese horario"));
        mvc.perform(post("/api/citas").header("Authorization", token("USER")).contentType("application/json")
                        .content("{\"fechaHora\":\"2099-01-01T09:00:00\",\"motivo\":\"Control\",\"mascotaId\":1,\"veterinarioId\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").value(containsString("horario")));
    }

    @Test
    void fechaPasadaEIdsAusentesProducen400() throws Exception {
        mvc.perform(post("/api/citas").header("Authorization", token("USER"))
                        .contentType("application/json").content("{\"fechaHora\":\"2000-01-01T09:00:00\",\"motivo\":\"\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errores.fechaHora").isArray())
                .andExpect(jsonPath("$.errores.mascotaId").isArray()).andExpect(jsonPath("$.errores.veterinarioId").isArray());
        verifyNoInteractions(citas);
    }

    @Test
    void errorInesperadoNoFiltraDetallesInternos() throws Exception {
        when(citas.listar()).thenThrow(new IllegalStateException("jdbc:mysql://privado clave-interna"));
        mvc.perform(get("/api/citas").header("Authorization", token("USER")))
                .andExpect(status().isInternalServerError()).andExpect(jsonPath("$.status").value(500))
                .andExpect(content().string(not(containsString("jdbc"))))
                .andExpect(content().string(not(containsString("clave-interna")))).andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void loginIncorrectoEs401() throws Exception {
        when(auth.login(any())).thenThrow(new BadCredentialsException("privado"));
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"paula@example.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensaje").value("Credenciales invalidas"));
    }

    @Test
    void jsonMalFormadoEs400() throws Exception {
        mvc.perform(post("/api/propietarios").header("Authorization", token("USER"))
                        .contentType("application/json").content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }
}
