package br.pucpr.restaurante.produtos

import br.pucpr.restaurante.produtos.requests.CreateProdutoRequest
import br.pucpr.restaurante.produtos.requests.UpdateProdutoRequest
import br.pucpr.restaurante.produtos.responses.ProdutoResponse
import br.pucpr.restaurante.users.SortDir
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/produtos")
class ProdutoController(val service: ProdutoService) {

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @ApiResponse(responseCode = "201")
    fun insert(
        @RequestBody @Valid req: CreateProdutoRequest
    ): ResponseEntity<ProdutoResponse> =
        service.insert(req.toProduto())
            .let { ProdutoResponse(it) }
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    fun list(
        @RequestParam(required = false) categoria: String?,
        @RequestParam(required = false) disponivel: Boolean?,
        @RequestParam(required = false) precoMin: BigDecimal?,
        @RequestParam(required = false) precoMax: BigDecimal?,
        @RequestParam(required = false, defaultValue = "nome") sortBy: String,
        @RequestParam(required = false, defaultValue = "ASC") sortDir: String,
    ): ResponseEntity<List<ProdutoResponse>> =
        service.search(categoria, disponivel, precoMin, precoMax, sortBy, SortDir.find(sortDir))
            .map { ProdutoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) =
        service.findById(id)
            .let { ProdutoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid req: UpdateProdutoRequest
    ): ResponseEntity<ProdutoResponse> =
        service.update(id, req)
            .let { ProdutoResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long): ResponseEntity<Void> {
        service.delete(id)
        return ResponseEntity.noContent().build()
    }
}
