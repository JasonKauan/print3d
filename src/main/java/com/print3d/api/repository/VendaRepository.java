package com.print3d.api.repository;

import com.print3d.api.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VendaRepository extends JpaRepository<Venda, Long> {

    // Todas as vendas de um membro específico
    List<Venda> findByMembroId(Long membroId);

    // Vendas por status do repasse (PENDENTE ou PAGO)
    List<Venda> findByStatusRepasse(Venda.StatusRepasse status);

    // Vendas pendentes de um membro específico
    List<Venda> findByMembroIdAndStatusRepasse(Long membroId, Venda.StatusRepasse status);

    // Soma total das vendas de um membro — usado no resumo financeiro
    @Query("SELECT COALESCE(SUM(v.valorTotal), 0) FROM Venda v WHERE v.membro.id = :membroId")
    BigDecimal somarVendasPorMembro(Long membroId);

    // Soma dos repasses já pagos a um membro
    @Query("SELECT COALESCE(SUM(v.repasse), 0) FROM Venda v WHERE v.membro.id = :membroId AND v.statusRepasse = 'PAGO'")
    BigDecimal somarRepassePagoPorMembro(Long membroId);
}