package com.huellitas.vetturno.controller;

import com.huellitas.vetturno.dto.CitaDTO;
import com.huellitas.vetturno.dto.CitaRequest;
import com.huellitas.vetturno.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/citas")
@Tag(name = "Cita", description = "Agenda ordenada por fecha ascendente")
public class CitaController {
    private final CitaService service;

    public CitaController(CitaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Agendar una cita futura sin duplicar veterinario y hora")
    @ApiResponse(responseCode = "201", description = "Cita creada")
    public ResponseEntity<CitaDTO> crear(@Valid @RequestBody CitaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    @Operation(summary = "Consultar toda la agenda, incluidas citas pasadas, en orden ascendente")
    public ResponseEntity<List<CitaDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/veterinario/{id}")
    @Operation(summary = "Consultar agenda por veterinario en orden ascendente",
            description = "Veterinario existente sin citas: lista vacia. Veterinario inexistente: 400.")
    public ResponseEntity<List<CitaDTO>> listarPorVeterinario(@PathVariable Long id) {
        return ResponseEntity.ok(service.listarPorVeterinario(id));
    }
}
