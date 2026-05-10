package br.pucpr.restaurante.roles

import br.pucpr.restaurante.roles.requests.CreateRoleRequest
import br.pucpr.restaurante.roles.responses.RoleResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/roles")
class RoleController(val roleService: RoleService) {
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ApiResponse(responseCode = "201")
    fun insert(
        @Valid @RequestBody role: CreateRoleRequest
    ) = roleService.insert(role.toRole())
        ?.let { RoleResponse(it) }
        ?.let { ResponseEntity.status(HttpStatus.CREATED).body(it) }
        ?: ResponseEntity.badRequest().build()

    @GetMapping
    fun list() = roleService.findAll().map { RoleResponse(it) }
}
