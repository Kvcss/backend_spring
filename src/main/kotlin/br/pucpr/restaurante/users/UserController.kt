package br.pucpr.restaurante.users

import br.pucpr.restaurante.users.requests.CreateUserRequest
import br.pucpr.restaurante.users.requests.LoginRequest
import br.pucpr.restaurante.users.requests.UpdateUserRequest
import br.pucpr.restaurante.users.responses.UserResponse
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
@RequestMapping("/users")
class UserController(val userService: UserService) {
    @GetMapping("/ping")
    fun ping() = mapOf("status" to "ok")

    @PostMapping
    @ApiResponse(responseCode = "201")
    fun insert(
        @RequestBody @Valid user: CreateUserRequest
    ) = userService.insert(user.toUser())
        .let { UserResponse(it) }
        .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun list(
        @RequestParam sortDir: String?,
        @RequestParam role: String?
    ): ResponseEntity<List<UserResponse>> {
        val users = if (role != null) userService.findByRole(role)
        else userService.findAll(SortDir.find(sortDir ?: "ASC"))
        return users.map { UserResponse(it) }.let { ResponseEntity.ok(it) }
    }

    @PostMapping("/login")
    fun login(
        @RequestBody @Valid user: LoginRequest
    ) = userService.login(user.email!!, user.password!!)

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long) =
        userService.findById(id)
            .let { UserResponse(it) }
            .let { ResponseEntity.ok(it) }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: Long) = userService.delete(id)

    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    fun update(
        @PathVariable id: Long,
        @RequestBody @Valid user: UpdateUserRequest
    ) = userService.update(id, user.name!!)
        ?.let { UserResponse(it) }
        ?.let { ResponseEntity.ok(it) }
        ?: ResponseEntity.noContent().build()

    @PutMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    fun grant(
        @PathVariable id: Long,
        @PathVariable roleName: String
    ): ResponseEntity<Void> =
        userService.addRole(id, roleName)
            .let { if (it) ResponseEntity.ok().build() else ResponseEntity.noContent().build() }
}
