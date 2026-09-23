package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.CitaDTO;
import com.huellitas.vetturno.dto.CitaRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.Cita;
import com.huellitas.vetturno.model.Mascota;
import com.huellitas.vetturno.model.Veterinario;
import com.huellitas.vetturno.repository.CitaRepository;
import com.huellitas.vetturno.repository.MascotaRepository;
import com.huellitas.vetturno.repository.VeterinarioRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CitaService {
    private final CitaRepository citas;
    private final MascotaRepository mascotas;
    private final VeterinarioRepository veterinarios;
    private final Clock clock;

    public CitaService(CitaRepository citas, MascotaRepository mascotas,
                       VeterinarioRepository veterinarios, Clock clock) {
        this.citas = citas;
        this.mascotas = mascotas;
        this.veterinarios = veterinarios;
        this.clock = clock;
    }

    @Transactional
    public CitaDTO crear(CitaRequest request) {
        LocalDateTime fecha = request.fechaHora();
        if (fecha == null || !fecha.isAfter(LocalDateTime.now(clock))) {
            throw new ReglaNegocioException("La cita debe tener una fecha futura");
        }
        if (fecha.getSecond() != 0 || fecha.getNano() != 0) {
            throw new ReglaNegocioException("La cita debe indicarse con precision de minutos, sin segundos");
        }
        Mascota mascota = mascotas.findById(request.mascotaId())
                .orElseThrow(() -> new ReglaNegocioException("La mascota indicada no existe"));
        Veterinario veterinario = veterinarios.findById(request.veterinarioId())
                .orElseThrow(() -> new ReglaNegocioException("El veterinario indicado no existe"));
        if (citas.existsByVeterinarioIdAndFechaHora(request.veterinarioId(), fecha)) {
            throw new ReglaNegocioException("El veterinario ya tiene una cita en ese horario");
        }
        try {
            // El UNIQUE en MySQL tambien protege frente a solicitudes simultaneas.
            return toDTO(citas.saveAndFlush(new Cita(fecha, request.motivo(), mascota, veterinario)));
        } catch (DataIntegrityViolationException ex) {
            throw new ReglaNegocioException("No se pudo agendar: el horario esta ocupado o los datos no son validos");
        }
    }

    public List<CitaDTO> listar() {
        return citas.findAllByOrderByFechaHoraAscIdAsc().stream().map(this::toDTO).toList();
    }

    public List<CitaDTO> listarPorVeterinario(Long id) {
        if (!veterinarios.existsById(id)) {
            throw new ReglaNegocioException("El veterinario indicado no existe");
        }
        return citas.findByVeterinarioIdOrderByFechaHoraAscIdAsc(id).stream().map(this::toDTO).toList();
    }

    private CitaDTO toDTO(Cita cita) {
        return new CitaDTO(cita.getId(), cita.getFechaHora(), cita.getMotivo(),
                cita.getMascota().getNombre(), cita.getMascota().getPropietario().getNombre(),
                cita.getVeterinario().getNombre());
    }
}
