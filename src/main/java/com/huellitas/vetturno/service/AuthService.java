package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.AuthResponse;
import com.huellitas.vetturno.dto.LoginRequest;
import com.huellitas.vetturno.dto.RegistroRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Rol;
import com.huellitas.vetturno.model.Usuario;
import com.huellitas.vetturno.repository.UsuarioRepository;
import com.huellitas.vetturno.security.JwtService;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authentication;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder,
                       AuthenticationManager authentication, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.authentication = authentication;
        this.jwt = jwt;
    }

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        String email = normalizar(request.email());
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ReglaNegocioException("La contrasena supera los 72 bytes admitidos por BCrypt");
        }
        if (usuarios.existsByEmail(email)) {
            throw new ReglaNegocioException("El email ya esta registrado");
        }
        try {
            usuarios.saveAndFlush(new Usuario(email, encoder.encode(request.password()), Rol.USER));
        } catch (DataIntegrityViolationException ex) {
            throw new ReglaNegocioException("No se pudo registrar: el email ya existe o los datos no son validos");
        }
        return new AuthResponse(jwt.generar(email));
    }

    public AuthResponse login(LoginRequest request) {
        var resultado = authentication.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(normalizar(request.email()), request.password()));
        return new AuthResponse(jwt.generar(resultado.getName()));
    }

    private String normalizar(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
