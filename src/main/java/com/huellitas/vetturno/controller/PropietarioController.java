package com.huellitas.vetturno.controller;

import com.huellitas.vetturno.dto.PropietarioDTO;
import com.huellitas.vetturno.dto.PropietarioRequest;
import com.huellitas.vetturno.service.PropietarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/propietarios")
@Tag(name = "Propietario", description = "Responsables de las mascotas")
public class PropietarioController {
    private final PropietarioService service;

    public PropietarioController(PropietarioService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Registrar propietario")
    @ApiResponse(responseCode = "201", description = "Registro creado")
    public ResponseEntity<PropietarioDTO> crear(@Valid @RequestBody PropietarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }

    @GetMapping
    @Operation(summary = "Listar propietarios")
    public ResponseEntity<List<PropietarioDTO>> listar() {
        return ResponseEntity.ok(service.listar());
    }
}
