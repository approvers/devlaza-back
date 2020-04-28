package com.approvers.devlazaApi.security

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilter(
        authenticationManager: AuthenticationManager,
        private val mapper: ObjectMapper
) : UsernamePasswordAuthenticationFilter() {
    init {
        this.authenticationManager = authenticationManager
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        // POSTで飛んできたusernameとpasswordでログインする
        val username = request.getParameter("username")
        val password = request.getParameter("password")
        val token = UsernamePasswordAuthenticationToken(username, password)
        setDetails(request, token)
        return authenticationManager.authenticate(token)
    }

    override fun successfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain, authResult: Authentication) {
        handleResponse(request, response, authResult, null)
    }

    override fun unsuccessfulAuthentication(request: HttpServletRequest, response: HttpServletResponse, failed: AuthenticationException) {
        handleResponse(request, response, null, failed)
    }

    private fun handleResponse(
            @Suppress("UNUSED_PARAMETER") request: HttpServletRequest,
            response: HttpServletResponse,
            result: Authentication?,
            @Suppress("UNUSED_PARAMETER") failed: AuthenticationException?
    ) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")

        if(result != null) {
            val user = result.principal as LoginUser
            val token = user.sign()
            val entity = ResponseEntity.ok("Bearer $token")
            response.writer.write(mapper.writeValueAsString(entity))
        }else{
            val entity = ResponseEntity.badRequest()
            response.writer.write(mapper.writeValueAsString(entity))
        }
    }
}