package br.pucpr.restaurante.pedidos

import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PedidoRepository : JpaRepository<Pedido, Long> {

    @Query(
        """select p from Pedido p
           where (:status is null or p.status = :status)
             and (:cliente is null or lower(p.cliente) like lower(concat('%', :cliente, '%')))"""
    )
    fun search(
        @Param("status") status: PedidoStatus?,
        @Param("cliente") cliente: String?,
        sort: Sort,
    ): List<Pedido>
}
