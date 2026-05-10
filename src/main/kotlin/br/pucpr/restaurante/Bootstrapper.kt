package br.pucpr.restaurante

import br.pucpr.restaurante.produtos.Produto
import br.pucpr.restaurante.produtos.ProdutoRepository
import br.pucpr.restaurante.roles.Role
import br.pucpr.restaurante.roles.RoleRepository
import br.pucpr.restaurante.users.User
import br.pucpr.restaurante.users.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class Bootstrapper(
    val userRepository: UserRepository,
    val roleRepository: RoleRepository,
    val produtoRepository: ProdutoRepository,
) : ApplicationListener<ContextRefreshedEvent> {
    override fun onApplicationEvent(event: ContextRefreshedEvent) {
        val adminRole = roleRepository.findByName("ADMIN")
            ?: roleRepository.save(Role(name = "ADMIN", description = "Administrador do sistema"))

        roleRepository.findByName("CLIENT")
            ?: roleRepository.save(Role(name = "CLIENT", description = "Cliente do restaurante"))

        if (userRepository.findByRole("ADMIN").isEmpty()) {
            val admin = User(
                email = "admin@restaurante.com",
                password = "Admin@123",
                name = "Administrador do Restaurante",
            )
            admin.roles.add(adminRole)
            userRepository.save(admin)
            log.info("Usuário admin inicial criado: admin@restaurante.com / Admin@123")
        }

        if (produtoRepository.count() == 0L) {
            produtoRepository.saveAll(listOf(
                Produto(nome = "Pizza Margherita", descricao = "Molho, mussarela e manjericão",
                    preco = BigDecimal("45.90"), categoria = "PIZZA"),
                Produto(nome = "Pizza Calabresa", descricao = "Calabresa e cebola",
                    preco = BigDecimal("48.90"), categoria = "PIZZA"),
                Produto(nome = "Hambúrguer Clássico", descricao = "Carne, queijo e alface",
                    preco = BigDecimal("32.50"), categoria = "BURGER"),
                Produto(nome = "Coca-Cola Lata", descricao = "350ml",
                    preco = BigDecimal("7.00"), categoria = "BEBIDA"),
                Produto(nome = "Suco de Laranja", descricao = "Natural, 500ml",
                    preco = BigDecimal("12.00"), categoria = "BEBIDA"),
            ))
            log.info("Catálogo inicial de produtos cadastrado.")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(Bootstrapper::class.java)
    }
}
