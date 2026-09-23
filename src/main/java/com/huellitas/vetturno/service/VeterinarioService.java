package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.VeterinarioDTO;
import com.huellitas.vetturno.dto.VeterinarioRequest;
import com.huellitas.vetturno.model.Veterinario;
import com.huellitas.vetturno.repository.VeterinarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VeterinarioService {
    private final VeterinarioRepository repository;

    public VeterinarioService(VeterinarioRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public VeterinarioDTO crear(VeterinarioRequest request) {
        return toDTO(repository.save(new Veterinario(request.nombre(), request.especialidad())));
    }

    public List<VeterinarioDTO> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::toDTO).toList();
    }

    private VeterinarioDTO toDTO(Veterinario entidad) {
        return new VeterinarioDTO(entidad.getId(), entidad.getNombre(), entidad.getEspecialidad());
    }
}
