package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.PropietarioDTO;
import com.huellitas.vetturno.dto.PropietarioRequest;
import com.huellitas.vetturno.model.Propietario;
import com.huellitas.vetturno.repository.PropietarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PropietarioService {
    private final PropietarioRepository repository;

    public PropietarioService(PropietarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PropietarioDTO crear(PropietarioRequest request) {
        return toDTO(repository.save(new Propietario(request.nombre(), request.telefono(), request.email())));
    }

    public List<PropietarioDTO> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::toDTO).toList();
    }

    private PropietarioDTO toDTO(Propietario entidad) {
        return new PropietarioDTO(entidad.getId(), entidad.getNombre(), entidad.getTelefono(), entidad.getEmail());
    }
}
