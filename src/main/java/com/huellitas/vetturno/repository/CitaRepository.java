package com.huellitas.vetturno.repository;

import com.huellitas.vetturno.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

/**
 * Permite guardar citas y consultar la agenda en MySQL.
 *
 * Usamos EntityGraph en las consultas que devuelven citas porque CitaService
 * necesita los nombres de la mascota, su propietario y el veterinario para crear CitaDTO.
 * Estas relaciones son LAZY: normalmente sus datos se cargan cuando se accede a ellos.
 * Al recorrer una lista, eso puede provocar consultas adicionales a la base de datos.
 *
 * EntityGraph indica que relaciones deben cargarse al consultar la agenda:
 * mascota, mascota.propietario (el propietario de esa mascota) y veterinario.
 * Son nombres de atributos Java, no nombres de columnas de MySQL.
 * Asi buscamos reducir las consultas adicionales, un problema conocido como N+1.
 * Hibernate decide el SQL que utiliza; la anotacion no garantiza por si sola
 * que cualquier consulta se resuelva siempre con una unica sentencia SQL.
 *
 * Es una mejora de la carga de datos, no una regla del negocio.
 * No cambia los filtros ni el orden, ni convierte todas las relaciones en EAGER.
 * Solo se aplica a los metodos donde esta escrita. Sin ella, la conversion a DTO
 * dentro de la transaccion del servicio puede funcionar, pero hacer mas consultas.
 */
public interface CitaRepository extends JpaRepository<Cita, Long> {
    // Solo comprueba si el horario esta ocupado; no necesita cargar las relaciones.
    boolean existsByVeterinarioIdAndFechaHora(Long veterinarioId, LocalDateTime fechaHora);

    /**
     * Trae la agenda completa con las relaciones que necesita la respuesta.
     * El nombre del metodo ordena por fecha y, si coinciden, por id.
     */
    @EntityGraph(attributePaths = {"mascota", "mascota.propietario", "veterinario"})
    List<Cita> findAllByOrderByFechaHoraAscIdAsc();

    /**
     * Trae las citas de un veterinario con los mismos datos y orden de la agenda.
     * El filtro por veterinario lo define el nombre del metodo, no EntityGraph.
     */
    @EntityGraph(attributePaths = {"mascota", "mascota.propietario", "veterinario"})
    List<Cita> findByVeterinarioIdOrderByFechaHoraAscIdAsc(Long veterinarioId);
}
