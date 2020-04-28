package com.approvers.devlazaApi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@SpringBootApplication
@EnableCaching
class DevlazaApplication

fun main(args: Array<String>) {
    runApplication<DevlazaApplication>(*args)
}
