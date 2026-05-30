package com.print3d.api.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.print3d.api.dto.response.ProdutoResponse;
import com.print3d.api.model.Produto;
import com.print3d.api.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final Cloudinary cloudinary;
    private final MovimentacaoEstoqueService movimentacaoService;

    public List<ProdutoResponse> listarTodos() {
        return produtoRepository.findAll()
                .stream()
                .map(ProdutoResponse::from)
                .collect(Collectors.toList());
    }

    public ProdutoResponse buscarPorId(Long id) {
        return ProdutoResponse.from(produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id)));
    }

    public ProdutoResponse criar(String nome, String descricao,
                                 BigDecimal preco, Integer estoque,
                                 String categoria, MultipartFile foto) throws IOException {
        String fotoUrl = uploadFoto(foto);

        Produto produto = Produto.builder()
                .nome(nome)
                .descricao(descricao)
                .preco(preco != null ? preco : BigDecimal.ZERO)
                .estoque(estoque != null ? estoque : 0)
                .categoria(categoria)
                .fotoUrl(fotoUrl)
                .build();

        Produto salvo = produtoRepository.save(produto);
        movimentacaoService.registrarEntradaProduto(salvo, null);
        return ProdutoResponse.from(salvo);
    }

    public ProdutoResponse atualizar(Long id, String nome, String descricao,
                                     BigDecimal preco, Integer estoque,
                                     String categoria, MultipartFile foto) throws IOException {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));

        produto.setNome(nome);
        if (descricao != null)  produto.setDescricao(descricao);
        if (preco != null)      produto.setPreco(preco);
        if (estoque != null)    produto.setEstoque(estoque);
        if (categoria != null)  produto.setCategoria(categoria);

        if (foto != null && !foto.isEmpty()) {
            produto.setFotoUrl(uploadFoto(foto));
        }

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    public ProdutoResponse atualizarCategoria(Long id, String categoria) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));
        produto.setCategoria(categoria != null && !categoria.isBlank() ? categoria : null);
        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado: " + id);
        }
        produtoRepository.deleteById(id);
    }

    private String uploadFoto(MultipartFile foto) throws IOException {
        Map<?, ?> resultado = cloudinary.uploader().upload(
                foto.getBytes(),
                ObjectUtils.asMap("folder", "print3d/produtos")
        );
        return (String) resultado.get("secure_url");
    }
}