package br.pucpr.restaurante

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class RestauranteApplication

fun main(args: Array<String>) {
    runApplication<RestauranteApplication>(*args)
}
