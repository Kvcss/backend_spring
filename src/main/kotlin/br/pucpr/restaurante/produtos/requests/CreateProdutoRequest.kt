package br.pucpr.restaurante.produtos.requests

import br.pucpr.restaurante.produtos.Produto
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class CreateProdutoRequest(
    @NotBlank
    val nome: String?,

    val descricao: String?,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "preço deve ser maior que zero")
    val preco: BigDecimal?,

    @NotBlank
    val categoria: String?,

    val disponivel: Boolean? = true,
) {
    fun toProduto() = Produto(
        nome = nome!!,
        descricao = descricao ?: "",
        preco = preco!!,
        categoria = categoria!!,
        disponivel = disponivel ?: true,
    )
}
