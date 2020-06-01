package dev.approvers.devlazaApi.domain.service

import dev.approvers.devlazaApi.domain.data.NonAuthorizedUser
import dev.approvers.devlazaApi.domain.data.User
import dev.approvers.devlazaApi.domain.repository.NonAuthorizedUserRepository
import dev.approvers.devlazaApi.domain.repository.UserRepository
import dev.approvers.devlazaApi.error.BadRequest
import dev.approvers.devlazaApi.error.Conflict
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class NonAuthorizedUserService(
    private val nonAuthorizedUserRepository: NonAuthorizedUserRepository,
    private val userRepository: UserRepository,
    private val mailService: MailService,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    fun create(user: User): NonAuthorizedUser {
        if (nonAuthorizedUserRepository.existsByMailAddress(user.mailAddress) || userRepository.existsByMailAddress(user.mailAddress)) {
            throw Conflict("this mailAddress was used by other user.")
        }

        return nonAuthorizedUserRepository.create(user.apply {
            password = passwordEncoder.encode(password)
        }).also {
            mailService.send {
                setFrom("noreply@devlaza.com")
                setTo(user.mailAddress)
                setSubject("[Devlaza] メールアドレス確認")
                setText("以下のURLに移動してメールアドレスを確認してください。\n https://devlaza.com/authorize/${it.token}")
            }
        }
    }

    fun authorize(rawToken: String): User {
        val token = try {
            UUID.fromString(rawToken)
        } catch (ex: IllegalArgumentException) {
            throw BadRequest("Token must be UUID.")
        }

        val user = nonAuthorizedUserRepository.getWithToken(token)
            ?: throw BadRequest("Token was expired.")

        return userRepository.create(user.user)
    }
}
