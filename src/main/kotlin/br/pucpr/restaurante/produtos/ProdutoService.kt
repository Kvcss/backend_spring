package br.pucpr.restaurante.produtos

import br.pucpr.restaurante.exceptions.BadRequestException
import br.pucpr.restaurante.exceptions.NotFoundException
import br.pucpr.restaurante.produtos.requests.UpdateProdutoRequest
import br.pucpr.restaurante.users.SortDir
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.math.BigDecimal

@Service
class ProdutoService(val repository: ProdutoRepository) {
    fun insert(produto: Produto): Produto {
        if (repository.findByNome(produto.nome) != null) {
            throw BadRequestException("Já existe um produto chamado '${produto.nome}'")
        }
        return repository.save(produto)
            .also { log.info("Produto {} ({}) cadastrado.", it.id, it.nome) }
    }

    fun findById(id: Long): Produto =
        repository.findByIdOrNull(id) ?: throw NotFoundException(id)

    fun search(
        categoria: String?,
        disponivel: Boolean?,
        precoMin: BigDecimal?,
        precoMax: BigDecimal?,
        sortBy: String,
        sortDir: SortDir,
    ): List<Produto> {
        val allowed = setOf("nome", "preco", "categoria")
        if (sortBy !in allowed) {
            throw BadRequestException("Campo de ordenação inválido: $sortBy. Aceitos: $allowed")
        }
        if (precoMin != null && precoMax != null && precoMin > precoMax) {
            throw BadRequestException("precoMin não pode ser maior que precoMax")
        }
        val direction = if (sortDir == SortDir.ASC) Sort.Direction.ASC else Sort.Direction.DESC
        log.debug(
            "Buscando produtos categoria={} disponivel={} preco=[{}, {}] ordenado por {} {}",
            categoria, disponivel, precoMin, precoMax, sortBy, sortDir
        )
        return repository.search(categoria, disponivel, precoMin, precoMax, Sort.by(direction, sortBy))
    }

    fun update(id: Long, req: UpdateProdutoRequest): Produto {
        val produto = findById(id)
        val novoNome = req.nome!!
        if (novoNome != produto.nome && repository.findByNome(novoNome) != null) {
            throw BadRequestException("Já existe um produto chamado '$novoNome'")
        }
        produto.nome = novoNome
        produto.descricao = req.descricao ?: ""
        produto.preco = req.preco!!
        produto.categoria = req.categoria!!
        produto.disponivel = req.disponivel ?: produto.disponivel
        return repository.save(produto)
            .also { log.info("Produto {} atualizado.", it.id) }
    }

    fun delete(id: Long) {
        val produto = findById(id)
        repository.delete(produto)
        log.warn("Produto {} ({}) removido.", id, produto.nome)
    }

    companion object {
        val log = LoggerFactory.getLogger(ProdutoService::class.java)
    }
}
