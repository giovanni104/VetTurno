package com.huellitas.vetturno.security;

import com.huellitas.vetturno.model.Usuario;
import com.huellitas.vetturno.repository.UsuarioRepository;
import java.util.Locale;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Busca en MySQL los datos de usuario que necesita Spring Security.
 * En este proyecto usamos el email para iniciar sesion, aunque el metodo
 * de la interfaz se llame loadUserByUsername.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository repository;

    public UsuarioDetailsService(UsuarioRepository repository) {
        this.repository = repository;
    }

    /**
     * Devuelve email, hash de contrasena y rol de la cuenta.
     * Se usa tanto en el login como al identificar al usuario de un JWT.
     * Si la cuenta no existe, informa un error de autenticacion.
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        // Quitamos espacios al inicio/final y usamos minusculas, igual que al registrar.
        Usuario usuario = repository.findByEmail(email.strip().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
        // Adaptamos nuestra entidad Usuario al formato UserDetails de Spring.
        // password ya contiene el hash; aqui no lo ciframos ni lo comparamos.
        // roles agrega el prefijo ROLE_ que Spring usa para comprobar permisos.
        return User.withUsername(usuario.getEmail()).password(usuario.getPassword())
                .roles(usuario.getRol().name()).build();
    }
}
