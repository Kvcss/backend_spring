package br.pucpr.restaurante.produtos.requests

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

data class UpdateProdutoRequest(
    @NotBlank
    val nome: String?,

    val descricao: String?,

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false, message = "preço deve ser maior que zero")
    val preco: BigDecimal?,

    @NotBlank
    val categoria: String?,

    val disponivel: Boolean?,
)
