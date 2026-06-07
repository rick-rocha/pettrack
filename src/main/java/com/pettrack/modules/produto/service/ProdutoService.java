package com.pettrack.modules.produto.service;

import com.pettrack.modules.produto.domain.entity.Produto;
import com.pettrack.modules.produto.dto.request.ProdutoRequest;
import com.pettrack.modules.produto.dto.response.ProdutoResponse;
import com.pettrack.modules.produto.repository.ProdutoRepository;
import com.pettrack.shared.exception.NegocioException;
import com.pettrack.shared.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    public ProdutoResponse cadastrar(ProdutoRequest request) {
        if (produtoRepository.existsByCodigoSku(request.getCodigoSku())) {
            throw new NegocioException("Já existe um produto com o código SKU: " + request.getCodigoSku());
        }

        if (request.getCodigoEan() != null && produtoRepository.existsByCodigoEan(request.getCodigoEan())) {
            throw new NegocioException("Já existe um produto com o código EAN: " + request.getCodigoEan());
        }

        Produto produto = toEntity(request);
        produto = produtoRepository.save(produto);
        return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(UUID id) {
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + id));

        return toResponse(produto);

    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorSku(String sku) {
    Produto produto = produtoRepository.findByCodigoSku(sku).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o código SKU: " + sku));
            return toResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listarAtivos() {
        return produtoRepository.findByAtivoTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse atualizar(UUID id, ProdutoRequest request) {
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + id));
        if (!produto.getCodigoSku().equals(request.getCodigoSku()) &&  produtoRepository.existsByCodigoSku(request.getCodigoSku())) {
            throw new NegocioException("Já existe um produto com o código SKU: " + request.getCodigoSku());
        }

        produto.setNome(request.getNome());
        produto.setDescricao(request.getDescricao());
        produto.setCodigoSku(request.getCodigoSku());
        produto.setCodigoEan(request.getCodigoEan());
        produto.setCategoria(request.getCategoria());
        produto.setEspecieAnimal(request.getEspecieAnimal());
        produto.setTipoArmazenamento(request.getTipoArmazenamento());
        produto.setPesoKg(request.getPesoKg());
        produto.setPrecoCusto(request.getPrecoCusto());
        produto.setPrecoVenda(request.getPrecoVenda());
        produto.setFabricante(request.getFabricante());
        produto.setRequerReceita(request.isRequerReceita());
        produto.setControladoAnvisa(request.isControladoAnvisa());
        produto.setTempoValidadeDias(request.getTempoValidadeDias());

        return toResponse(produtoRepository.save(produto));

    }

    @Transactional
    public void inativar(UUID id) {
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + id));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private Produto toEntity(ProdutoRequest request) {
        return Produto.builder()
                .nome(request.getNome())
                .descricao(request.getDescricao())
                .codigoSku(request.getCodigoSku())
                .codigoEan(request.getCodigoEan())
                .categoria(request.getCategoria())
                .especieAnimal(request.getEspecieAnimal())
                .tipoArmazenamento(request.getTipoArmazenamento())
                .pesoKg(request.getPesoKg())
                .precoCusto(request.getPrecoCusto())
                .precoVenda(request.getPrecoVenda())
                .fabricante(request.getFabricante())
                .requerReceita(request.isRequerReceita())
                .controladoAnvisa(request.isControladoAnvisa())
                .tempoValidadeDias(request.getTempoValidadeDias())
                .build();
    }

    private ProdutoResponse toResponse(Produto produto) {
        return ProdutoResponse.builder()
                .id(produto.getId())
                .nome(produto.getNome())
                .descricao(produto.getDescricao())
                .codigoSku(produto.getCodigoSku())
                .codigoEan(produto.getCodigoEan())
                .categoria(produto.getCategoria())
                .especieAnimal(produto.getEspecieAnimal())
                .tipoArmazenamento(produto.getTipoArmazenamento())
                .pesoKg(produto.getPesoKg())
                .precoVenda(produto.getPrecoVenda())
                .fabricante(produto.getFabricante())
                .requerReceita(produto.isRequerReceita())
                .controladoAnvisa(produto.isControladoAnvisa())
                .tempoValidadeDias(produto.getTempoValidadeDias())
                .ativo(produto.isAtivo())
                .criadoEm(produto.getCriadoEm())
                .atualizadoEm(produto.getAtualizadoEm())
                .build();
    }

}