package com.approvers.devlazaApi.security

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthorizationFilter(
        authenticationManager: AuthenticationManager,
        private val userDetailsService: UserDetailsService
) : BasicAuthenticationFilter(authenticationManager) {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        val token = request.authentication ?: run {
            chain.doFilter(request, response)
            return
        }

        SecurityContextHolder.getContext().authentication = token
        chain.doFilter(request, response)
    }

    private val HttpServletRequest.authentication: UsernamePasswordAuthenticationToken?
        get() {
            val header = this.getHeader("Authorization")
            if (header == null || !header.startsWith("Bearer ")) {
                return null
            }

            val token = header.split(" ")[1]
            val mailAddress = token.mailAddress ?: return null

            val userDetails = try {
                userDetailsService.loadUserByUsername(mailAddress)
            } catch (ex: UsernameNotFoundException) {
                return null
            }

            if (!token.verifyToken(mailAddress)) {
                return null
            }

            return UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        }
}