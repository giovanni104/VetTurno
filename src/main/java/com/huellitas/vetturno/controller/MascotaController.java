package com.huellitas.vetturno.controller;

import com.huellitas.vetturno.dto.MascotaDTO;
import com.huellitas.vetturno.dto.MascotaRequest;
import com.huellitas.vetturno.service.MascotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mascotas")
@Tag(name = "Mascota", description = "Pacientes de Veterinaria Huellitas")
public class MascotaController {
    private final MascotaService service;

    public MascotaController(MascotaService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar mascota")
    @ApiResponse(responseCode = "201", description = "Registro creado")
    public ResponseEntity<MascotaDTO> crear(@Valid @RequestBody MascotaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar mascotas")
    public ResponseEntity<List<MascotaDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
