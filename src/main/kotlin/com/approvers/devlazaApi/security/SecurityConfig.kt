package com.approvers.devlazaApi.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.CacheManager
import org.springframework.context.annotation.Bean
import org.springframework.security.authentication.CachingUserDetailsService
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@EnableWebSecurity
class SecurityConfig(
    @Qualifier("userDetailsServiceImpl")
    private val userDetailsService: UserDetailsService,
    private val cacheManager: CacheManager
) : WebSecurityConfigurerAdapter() {
    private val mapper = jacksonObjectMapper()

    override fun configure(http: HttpSecurity) {
        http.cors()
            .and()
            .csrf().disable()
            .authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .addFilter(JwtAuthenticationFilter(authenticationManager(), mapper))
            .addFilter(JwtAuthorizationFilter(authenticationManager(), cachingUserDetailsService(userDetailsService)))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        val cachingUserDetailsService = cachingUserDetailsService(userDetailsService)
        val userCache = SpringCacheBasedUserCache(cacheManager.getCache("jwt-cache"))
        cachingUserDetailsService.userCache = userCache

        auth.eraseCredentials(false)
        auth.userDetailsService(cachingUserDetailsService)
    }

    @get:Bean
    val passwordEncoder: PasswordEncoder
        get() = PasswordEncoderFactories.createDelegatingPasswordEncoder()

    private fun cachingUserDetailsService(delegate: UserDetailsService): CachingUserDetailsService {
        val ctor = CachingUserDetailsService::class.java.getDeclaredConstructor(UserDetailsService::class.java)
            ?: error("CachingUserDetailsService constructor is null.")

        ctor.isAccessible = true
        return BeanUtils.instantiateClass(ctor, delegate)
    }
}
