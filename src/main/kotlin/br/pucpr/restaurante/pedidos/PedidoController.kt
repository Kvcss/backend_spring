package br.pucpr.restaurante.pedidos

import br.pucpr.restaurante.pedidos.requests.CreatePedidoRequest
import br.pucpr.restaurante.pedidos.requests.UpdateStatusRequest
import br.pucpr.restaurante.pedidos.responses.PedidoResponse
import br.pucpr.restaurante.users.SortDir
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pedidos")
class PedidoController(val service: PedidoService) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiResponse(responseCode = "201")
    fun insert(
        @RequestBody @Valid req: CreatePedidoRequest
    ): ResponseEntity<PedidoResponse> =
        service.insert(req.toPedido(), req.produtoIds)
            .let { PedidoResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    fun list(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) cliente: String?,
        @RequestParam(required = false, defaultValue = "criadoEm") sortBy: String,
        @RequestParam(required = false, defaultValue = "DESC") sortDir: String,
    ): ResponseEntity<List<PedidoResponse>> {
        val statusEnum = status?.let { PedidoStatus.find(it) }
        return service.search(statusEnum, cliente, sortBy, SortDir.find(sortDir))
            .map { PedidoResponse(it) }
            .let { ResponseEntity.ok(it) }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun getById(@PathVariable id: Long): ResponseEntity<PedidoResponse> =
        service.findById(id)
            .let { PedidoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PutMapping("/{pedidoId}/produtos/{produtoId}")
    @PreAuthorize("isAuthenticated()")
    fun addProduto(
        @PathVariable pedidoId: Long,
        @PathVariable produtoId: Long,
    ): ResponseEntity<PedidoResponse> =
        service.addProduto(pedidoId, produtoId)
            .let { PedidoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{pedidoId}/produtos/{produtoId}")
    @PreAuthorize("isAuthenticated()")
    fun removeProduto(
        @PathVariable pedidoId: Long,
        @PathVariable produtoId: Long,
    ): ResponseEntity<PedidoResponse> =
        service.removeProduto(pedidoId, produtoId)
            .let { PedidoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody @Valid req: UpdateStatusRequest,
    ): ResponseEntity<PedidoResponse> =
        service.updateStatus(id, PedidoStatus.find(req.status!!))
            .let { PedidoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
