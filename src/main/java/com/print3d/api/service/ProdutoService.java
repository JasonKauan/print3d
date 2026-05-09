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
    private final Cloudinary cloudinary;  // injetado via CloudinaryConfig

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

    // Cria produto com upload de foto obrigatório
    public ProdutoResponse criar(String nome, String descricao,
                                 BigDecimal preco, Integer estoque,
                                 MultipartFile foto) throws IOException {

        // Faz upload da foto para o Cloudinary e pega a URL pública
        String fotoUrl = uploadFoto(foto);

        Produto produto = Produto.builder()
                .nome(nome)
                .descricao(descricao)
                .preco(preco != null ? preco : BigDecimal.ZERO)
                .estoque(estoque != null ? estoque : 0)
                .fotoUrl(fotoUrl)
                .build();

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    // Atualiza produto — foto é opcional no update
    public ProdutoResponse atualizar(Long id, String nome, String descricao,
                                     BigDecimal preco, Integer estoque,
                                     MultipartFile foto) throws IOException {

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado: " + id));

        produto.setNome(nome);
        if (descricao != null) produto.setDescricao(descricao);
        if (preco != null)     produto.setPreco(preco);
        if (estoque != null)   produto.setEstoque(estoque);

        // Só atualiza a foto se uma nova foi enviada
        if (foto != null && !foto.isEmpty()) {
            produto.setFotoUrl(uploadFoto(foto));
        }

        return ProdutoResponse.from(produtoRepository.save(produto));
    }

    public void deletar(Long id) {
        if (!produtoRepository.existsById(id)) {
            throw new RuntimeException("Produto não encontrado: " + id);
        }
        produtoRepository.deleteById(id);
    }

    // Faz o upload do arquivo para o Cloudinary e retorna a URL pública
    private String uploadFoto(MultipartFile foto) throws IOException {
        // Envia o array de bytes do arquivo para o Cloudinary
        // "folder" organiza as fotos em uma pasta dentro da sua conta
        Map<?, ?> resultado = cloudinary.uploader().upload(
                foto.getBytes(),
                ObjectUtils.asMap("folder", "print3d/produtos")
        );

        // "secure_url" é a URL HTTPS da imagem já disponível publicamente
        return (String) resultado.get("secure_url");
    }
}