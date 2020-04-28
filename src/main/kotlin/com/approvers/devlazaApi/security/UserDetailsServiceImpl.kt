package com.approvers.devlazaApi.security

import com.approvers.devlazaApi.domain.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserDetailsServiceImpl(
        @Autowired private val userRepository: UserRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val entity = userRepository.getWithMailAddress(username) ?: throw UsernameNotFoundException("User not found")
        return entity.toLoginUser()
    }
}