package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.*;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Rol;
import com.huellitas.vetturno.model.Usuario;
import com.huellitas.vetturno.repository.UsuarioRepository;
import com.huellitas.vetturno.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {
    private final UsuarioRepository usuarios = mock(UsuarioRepository.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final AuthenticationManager authentication = mock(AuthenticationManager.class);
    private final JwtService jwt = mock(JwtService.class);
    private final AuthService service = new AuthService(usuarios, encoder, authentication, jwt);

    @Test
    void registroAsignaUserHasheaYNormalizaEmail() {
        when(jwt.generar("paula@example.com")).thenReturn("token-de-prueba");
        var response = service.registrar(new RegistroRequest("PAULA@example.com", "ClaveDePrueba123"));
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarios).saveAndFlush(captor.capture());
        Usuario usuario = captor.getValue();
        assertThat(usuario.getRol()).isEqualTo(Rol.USER);
        assertThat(usuario.getEmail()).isEqualTo("paula@example.com");
        assertThat(usuario.getPassword()).isNotEqualTo("ClaveDePrueba123").startsWith("$2");
        assertThat(encoder.matches("ClaveDePrueba123", usuario.getPassword())).isTrue();
        assertThat(response.token()).isEqualTo("token-de-prueba");
    }

    @Test
    void rechazaEmailRepetido() {
        when(usuarios.existsByEmail("paula@example.com")).thenReturn(true);
        assertThatThrownBy(() -> service.registrar(new RegistroRequest("paula@example.com", "ClaveDePrueba123")))
                .isInstanceOf(ReglaNegocioException.class);
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void rechazaClaveQueSuperaLimiteDeBytesDeBCrypt() {
        assertThatThrownBy(() -> service.registrar(new RegistroRequest("paula@example.com", "a".repeat(73))))
                .isInstanceOf(ReglaNegocioException.class);
        verify(usuarios, never()).saveAndFlush(any());
    }

    @Test
    void loginUsaAuthenticationManagerAntesDeEmitirToken() {
        when(authentication.authenticate(any())).thenReturn(
                UsernamePasswordAuthenticationToken.authenticated("paula@example.com", null, java.util.List.of()));
        when(jwt.generar("paula@example.com")).thenReturn("token");
        assertThat(service.login(new LoginRequest("PAULA@example.com", "ClaveDePrueba123")).token()).isEqualTo("token");
        var order = inOrder(authentication, jwt);
        order.verify(authentication).authenticate(any());
        order.verify(jwt).generar("paula@example.com");
    }

    @Test
    void loginIncorrectoNoEmiteToken() {
        when(authentication.authenticate(any())).thenThrow(new BadCredentialsException("No"));
        assertThatThrownBy(() -> service.login(new LoginRequest("paula@example.com", "incorrecta")))
                .isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwt);
    }
}
