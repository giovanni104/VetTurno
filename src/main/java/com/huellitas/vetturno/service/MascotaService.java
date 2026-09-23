package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.MascotaDTO;
import com.huellitas.vetturno.dto.MascotaRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Mascota;
import com.huellitas.vetturno.model.Propietario;
import com.huellitas.vetturno.repository.MascotaRepository;
import com.huellitas.vetturno.repository.PropietarioRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class MascotaService {
    private final MascotaRepository repository;
    private final PropietarioRepository propietarios;

    public MascotaService(MascotaRepository repository, PropietarioRepository propietarios) {
        this.repository = repository;
        this.propietarios = propietarios;
    }

    @Transactional
    public MascotaDTO crear(MascotaRequest request) {
        Propietario propietario = propietarios.findById(request.propietarioId())
                .orElseThrow(() -> new ReglaNegocioException("El propietario indicado no existe"));
        return toDTO(repository.save(new Mascota(request.nombre(), request.especie(), request.raza(), propietario)));
    }

    public List<MascotaDTO> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::toDTO).toList();
    }

    private MascotaDTO toDTO(Mascota mascota) {
        return new MascotaDTO(mascota.getId(), mascota.getNombre(), mascota.getEspecie(), mascota.getRaza(),
                mascota.getPropietario().getId(), mascota.getPropietario().getNombre());
    }
}
