package br.pucpr.restaurante.produtos

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.math.BigDecimal

@Repository
interface ProdutoRepository : JpaRepository<Produto, Long> {
    fun findByNome(nome: String): Produto?

    @Query(
        """select p from Produto p
           where (:categoria is null or lower(p.categoria) = lower(:categoria))
             and (:disponivel is null or p.disponivel = :disponivel)
             and (:precoMin is null or p.preco >= :precoMin)
             and (:precoMax is null or p.preco <= :precoMax)"""
    )
    fun search(
        @Param("categoria") categoria: String?,
        @Param("disponivel") disponivel: Boolean?,
        @Param("precoMin") precoMin: BigDecimal?,
        @Param("precoMax") precoMax: BigDecimal?,
        sort: Sort,
    ): List<Produto>
}
