package com.approvers.devlazaApi.domain.service

import com.approvers.devlazaApi.domain.data.User
import com.approvers.devlazaApi.domain.repository.UserRepository
import com.approvers.devlazaApi.error.BadRequest
import com.approvers.devlazaApi.error.NotFound
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    fun getAll(limit: Int): List<User> = userRepository.getAll(limit)
        .map { it.apply { password = "" } }

    fun get(id: UUID): User = userRepository.get(id)?.apply { password = "" }
        ?: throw NotFound("User not found.")

    fun edit(id: String, user: User): User {
        if (user.id == null || "${user.id}" != id) throw BadRequest("User id is invalid.")
        return userRepository.update(user.apply { password = passwordEncoder.encode(password) })
    }
}
