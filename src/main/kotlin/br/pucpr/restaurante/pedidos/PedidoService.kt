package br.pucpr.restaurante.pedidos

import br.pucpr.restaurante.exceptions.BadRequestException
import br.pucpr.restaurante.exceptions.NotFoundException
import br.pucpr.restaurante.produtos.ProdutoRepository
import br.pucpr.restaurante.users.SortDir
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class PedidoService(
    val repository: PedidoRepository,
    val produtoRepository: ProdutoRepository,
) {
    @Transactional
    fun insert(pedido: Pedido, produtoIds: Set<Long>): Pedido {
        produtoIds.forEach { id ->
            val produto = produtoRepository.findByIdOrNull(id)
                ?: throw NotFoundException("Produto $id não encontrado")
            if (!produto.disponivel) {
                throw BadRequestException("Produto '${produto.nome}' está indisponível")
            }
            pedido.produtos.add(produto)
        }
        return repository.save(pedido)
            .also { log.info("Pedido {} criado para cliente '{}' com {} produto(s)",
                it.id, it.cliente, it.produtos.size) }
    }

    fun findById(id: Long): Pedido =
        repository.findByIdOrNull(id) ?: throw NotFoundException(id)

    fun search(status: PedidoStatus?, cliente: String?, sortBy: String, sortDir: SortDir): List<Pedido> {
        val allowed = setOf("criadoEm", "cliente", "status")
        if (sortBy !in allowed) {
            throw BadRequestException("Campo de ordenação inválido: $sortBy. Aceitos: $allowed")
        }
        val direction = if (sortDir == SortDir.ASC) Sort.Direction.ASC else Sort.Direction.DESC
        log.debug("Buscando pedidos status={} cliente={} ordenado por {} {}",
            status, cliente, sortBy, sortDir)
        return repository.search(status, cliente, Sort.by(direction, sortBy))
    }

    @Transactional
    fun addProduto(pedidoId: Long, produtoId: Long): Pedido {
        val pedido = findById(pedidoId)
        if (pedido.status != PedidoStatus.CRIADO) {
            throw BadRequestException("Não é possível alterar pedido com status ${pedido.status}")
        }
        val produto = produtoRepository.findByIdOrNull(produtoId)
            ?: throw NotFoundException("Produto $produtoId não encontrado")
        if (!produto.disponivel) {
            throw BadRequestException("Produto '${produto.nome}' está indisponível")
        }
        if (pedido.produtos.any { it.id == produtoId }) {
            throw BadRequestException("Produto '${produto.nome}' já está neste pedido")
        }
        pedido.produtos.add(produto)
        log.info("Produto {} adicionado ao pedido {}", produtoId, pedidoId)
        return repository.save(pedido)
    }

    @Transactional
    fun removeProduto(pedidoId: Long, produtoId: Long): Pedido {
        val pedido = findById(pedidoId)
        if (pedido.status != PedidoStatus.CRIADO) {
            throw BadRequestException("Não é possível alterar pedido com status ${pedido.status}")
        }
        val produto = pedido.produtos.find { it.id == produtoId }
            ?: throw NotFoundException("Produto $produtoId não está neste pedido")
        pedido.produtos.remove(produto)
        log.info("Produto {} removido do pedido {}", produtoId, pedidoId)
        return repository.save(pedido)
    }

    @Transactional
    fun updateStatus(pedidoId: Long, novoStatus: PedidoStatus): Pedido {
        val pedido = findById(pedidoId)
        if (pedido.status == novoStatus) return pedido
        validateStatusTransition(pedido.status, novoStatus)
        log.info("Pedido {} mudou de {} para {}", pedidoId, pedido.status, novoStatus)
        pedido.status = novoStatus
        return repository.save(pedido)
    }

    fun delete(id: Long) {
        val pedido = findById(id)
        if (pedido.status == PedidoStatus.ENTREGUE) {
            throw BadRequestException("Pedido entregue não pode ser excluído")
        }
        repository.delete(pedido)
        log.warn("Pedido {} excluído (cliente: {}).", id, pedido.cliente)
    }

    private fun validateStatusTransition(atual: PedidoStatus, novo: PedidoStatus) {
        val allowed = when (atual) {
            PedidoStatus.CRIADO -> setOf(PedidoStatus.PAGO, PedidoStatus.CANCELADO)
            PedidoStatus.PAGO -> setOf(PedidoStatus.ENTREGUE, PedidoStatus.CANCELADO)
            PedidoStatus.ENTREGUE -> emptySet()
            PedidoStatus.CANCELADO -> emptySet()
        }
        if (novo !in allowed) {
            throw BadRequestException("Transição inválida: $atual -> $novo")
        }
    }

    companion object {
        val log = LoggerFactory.getLogger(PedidoService::class.java)
    }
}
