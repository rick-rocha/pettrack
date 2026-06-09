package com.pettrack.modules.recebimento.controller;

import com.pettrack.modules.recebimento.domain.enums.StatusRecebimento;
import com.pettrack.modules.recebimento.dto.request.ConferirItemRequest;
import com.pettrack.modules.recebimento.dto.request.OrdemRecebimentoRequest;
import com.pettrack.modules.recebimento.dto.response.ItemRecebimentoResponse;
import com.pettrack.modules.recebimento.dto.response.OrdemRecebimentoResponse;
import com.pettrack.modules.recebimento.service.RecebimentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recebimento")
@RequiredArgsConstructor
public class RecebimentoController {

    private final RecebimentoService recebimentoService;

    @PostMapping("/ordens")
    public ResponseEntity<OrdemRecebimentoResponse> criarOrdem(
            @RequestBody @Valid OrdemRecebimentoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(recebimentoService.criarOrdem(request));
    }

    @PatchMapping("/ordens/{id}/iniciar-conferencia")
    public ResponseEntity<OrdemRecebimentoResponse> iniciarConferencia(@PathVariable UUID id) {
        return ResponseEntity.ok(recebimentoService.iniciarConferencia(id));
    }

    @PatchMapping("/ordens/{ordemId}/itens/{itemId}/conferir")
    public ResponseEntity<ItemRecebimentoResponse> conferirItem(
            @PathVariable UUID ordemId,
            @PathVariable UUID itemId,
            @RequestBody @Valid ConferirItemRequest request) {
        return ResponseEntity.ok(recebimentoService.conferirItem(ordemId, itemId, request));
    }

    @PatchMapping("/ordens/{id}/finalizar")
    public ResponseEntity<OrdemRecebimentoResponse> finalizarOrdem(@PathVariable UUID id) {
        return ResponseEntity.ok(recebimentoService.finalizarOrdem(id));
    }

    @GetMapping("/ordens/{id}")
    public ResponseEntity<OrdemRecebimentoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(recebimentoService.buscarPorId(id));
    }

    @GetMapping("/ordens")
    public ResponseEntity<List<OrdemRecebimentoResponse>> listarPorStatus(
            @RequestParam(required = false) StatusRecebimento status) {
        return ResponseEntity.ok(recebimentoService.listarPorStatus(status));
    }

}