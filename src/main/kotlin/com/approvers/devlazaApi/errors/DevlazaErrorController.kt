package com.approvers.devlazaApi.errors

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DevlazaErrorController : ErrorController {
    @RequestMapping("/error")
    fun handleError() {
        throw InternalServerError("Happen some error in the process")
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}

