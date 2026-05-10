package br.pucpr.restaurante.produtos.responses

import br.pucpr.restaurante.produtos.Produto
import java.math.BigDecimal

data class ProdutoResponse(
    val id: Long,
    val nome: String,
    val descricao: String,
    val preco: BigDecimal,
    val categoria: String,
    val disponivel: Boolean,
) {
    constructor(p: Produto) : this(
        id = p.id!!,
        nome = p.nome,
        descricao = p.descricao,
        preco = p.preco,
        categoria = p.categoria,
        disponivel = p.disponivel,
    )
}
