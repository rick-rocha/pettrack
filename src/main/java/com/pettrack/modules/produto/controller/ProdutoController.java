package com.pettrack.modules.produto.controller;

import com.pettrack.modules.produto.dto.request.ProdutoRequest;
import com.pettrack.modules.produto.dto.response.ProdutoResponse;
import com.pettrack.modules.produto.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ProdutoResponse> cadastrar(@Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse response = produtoService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoResponse> buscarPorId(@PathVariable UUID id) {
        ProdutoResponse response = produtoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProdutoResponse> buscarPorSku(@PathVariable String sku) {
        ProdutoResponse response = produtoService.buscarPorSku(sku);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProdutoResponse>> listarAtivos() {
        List<ProdutoResponse> response = produtoService.listarAtivos();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponse> atualizar(@PathVariable UUID id, @Valid @RequestBody ProdutoRequest request) {
        ProdutoResponse response = produtoService.atualizar(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProdutoResponse> inativar(@PathVariable UUID id) {
        produtoService.inativar(id);
        return ResponseEntity.noContent().build();
    }

}