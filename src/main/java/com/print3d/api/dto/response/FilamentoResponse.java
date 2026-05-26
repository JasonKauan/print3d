package com.print3d.api.dto.response;

import com.print3d.api.model.Filamento;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class FilamentoResponse {
    private Long id;
    private String nome;
    private String marca;
    private String cor;
    private String tipo;
    private BigDecimal pesoTotalGramas;
    private BigDecimal pesoDisponivelGramas;
    private BigDecimal precoPago;
    private BigDecimal custoPorGrama;
    private Filamento.Status status;
    private LocalDate dataCompra;
    private LocalDateTime criadoEm;

    // Percentual usado do rolo
    private Integer percentualUsado;

    public static FilamentoResponse from(Filamento f) {
        int percentual = 0;
        if (f.getPesoTotalGramas() != null && f.getPesoTotalGramas().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal usado = f.getPesoTotalGramas().subtract(f.getPesoDisponivelGramas());
            percentual = usado.multiply(BigDecimal.valueOf(100))
                    .divide(f.getPesoTotalGramas(), 0, java.math.RoundingMode.HALF_UP)
                    .intValue();
        }

        return FilamentoResponse.builder()
                .id(f.getId())
                .nome(f.getNome())
                .marca(f.getMarca())
                .cor(f.getCor())
                .tipo(f.getTipo())
                .pesoTotalGramas(f.getPesoTotalGramas())
                .pesoDisponivelGramas(f.getPesoDisponivelGramas())
                .precoPago(f.getPrecoPago())
                .custoPorGrama(f.getCustoPorGrama())
                .status(f.getStatus())
                .dataCompra(f.getDataCompra())
                .criadoEm(f.getCriadoEm())
                .percentualUsado(percentual)
                .build();
    }
}