package com.huellitas.vetturno.controller;

import com.huellitas.vetturno.dto.VeterinarioDTO;
import com.huellitas.vetturno.dto.VeterinarioRequest;
import com.huellitas.vetturno.service.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/veterinarios")
@Tag(name = "Veterinario", description = "Profesionales de Veterinaria Huellitas")
public class VeterinarioController {
    private final VeterinarioService service;

    public VeterinarioController(VeterinarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar veterinario (solo ADMIN)")
    @ApiResponse(responseCode = "201", description = "Registro creado")
    public ResponseEntity<VeterinarioDTO> crear(@Valid @RequestBody VeterinarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar veterinarios")
    public ResponseEntity<List<VeterinarioDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
