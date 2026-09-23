package com.huellitas.vetturno.service;

import com.huellitas.vetturno.dto.CitaRequest;
import com.huellitas.vetturno.exception.ReglaNegocioException;
import com.huellitas.vetturno.model.*;
import com.huellitas.vetturno.repository.*;
import java.time.*;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CitaServiceTest {
    private final CitaRepository citas = mock(CitaRepository.class);
    private final MascotaRepository mascotas = mock(MascotaRepository.class);
    private final VeterinarioRepository veterinarios = mock(VeterinarioRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2030-01-01T15:00:00Z"), ZoneId.of("America/Bogota"));
    private final CitaService service = new CitaService(citas, mascotas, veterinarios, clock);
    private final LocalDateTime futuro = LocalDateTime.of(2030, 1, 2, 9, 0);
    private final Propietario propietario = new Propietario("Marta", "3001234567", null);
    private final Mascota mascota = new Mascota("Luna", "Canino", null, propietario);
    private final Veterinario veterinario = new Veterinario("Andres", "General");

    @BeforeEach
    void relaciones() {
        when(mascotas.findById(1L)).thenReturn(Optional.of(mascota));
        when(veterinarios.findById(2L)).thenReturn(Optional.of(veterinario));
    }

    private CitaRequest request(LocalDateTime fecha) {
        return new CitaRequest(fecha, "Control", 1L, 2L);
    }

    @Test
    void creaCitaConRelacionesYDtoPlano() {
        when(citas.saveAndFlush(any(Cita.class))).thenAnswer(i -> i.getArgument(0));
        var dto = service.crear(request(futuro));
        assertThat(dto.mascota()).isEqualTo("Luna");
        assertThat(dto.propietario()).isEqualTo("Marta");
        assertThat(dto.veterinario()).isEqualTo("Andres");
        assertThat(dto.fechaHora()).isEqualTo(futuro);
    }

    @Test
    void rechazaPasadoYMomentoActualEnColombia() {
        assertThatThrownBy(() -> service.crear(request(LocalDateTime.of(2030, 1, 1, 9, 59))))
                .isInstanceOf(ReglaNegocioException.class);
        assertThatThrownBy(() -> service.crear(request(LocalDateTime.of(2030, 1, 1, 10, 0))))
                .isInstanceOf(ReglaNegocioException.class);
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    void rechazaSegundosYFraccionesSinRedondear() {
        assertThatThrownBy(() -> service.crear(request(futuro.plusSeconds(1))))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("minutos");
        assertThatThrownBy(() -> service.crear(request(futuro.plusNanos(1))))
                .isInstanceOf(ReglaNegocioException.class);
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    void rechazaMascotaInexistente() {
        when(mascotas.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crear(request(futuro)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("mascota");
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    void rechazaVeterinarioInexistente() {
        when(veterinarios.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.crear(request(futuro)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("veterinario");
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    void rechazaHorarioOcupadoAntesDeGuardar() {
        when(citas.existsByVeterinarioIdAndFechaHora(2L, futuro)).thenReturn(true);
        assertThatThrownBy(() -> service.crear(request(futuro)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("horario");
        verify(citas, never()).saveAndFlush(any());
    }

    @Test
    void transformaConflictoConcurrenteDeBaseEnReglaDeNegocio() {
        when(citas.saveAndFlush(any())).thenThrow(new DataIntegrityViolationException("uk_cita_veterinario_fecha"));
        assertThatThrownBy(() -> service.crear(request(futuro)))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("horario");
    }

    @Test
    void solicitaAgendaCompletaOrdenadaYConservaPasadas() {
        when(citas.findAllByOrderByFechaHoraAscIdAsc()).thenReturn(List.of(
                new Cita(futuro.minusYears(1), "Anterior", mascota, veterinario),
                new Cita(futuro, "Control", mascota, veterinario)));
        assertThat(service.listar()).extracting("motivo").containsExactly("Anterior", "Control");
    }

    @Test
    void veterinarioExistenteSinCitasDevuelveListaVacia() {
        when(veterinarios.existsById(2L)).thenReturn(true);
        when(citas.findByVeterinarioIdOrderByFechaHoraAscIdAsc(2L)).thenReturn(List.of());
        assertThat(service.listarPorVeterinario(2L)).isEmpty();
    }

    @Test
    void filtroRechazaVeterinarioInexistente() {
        assertThatThrownBy(() -> service.listarPorVeterinario(99L))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
