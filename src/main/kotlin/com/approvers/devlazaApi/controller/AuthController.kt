package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.errors.NotFound
import com.approvers.devlazaApi.model.AuthPoster
import com.approvers.devlazaApi.model.Token
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.repository.TokenRepository
import com.approvers.devlazaApi.repository.UserRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/auth")
class AuthController(
		private val tokenRepository: TokenRepository,
		private val userRepository: UserRepository
) {
	private val notFound = User(
				name="User not found.",
				passWord="",
				showId="",
				mailAddress=""
		)

	@PostMapping("/")
	fun getUserInfo(@Valid @RequestBody authPoster: AuthPoster): User {
		val token: String = authPoster.token

		val userId: UUID = tokenRepository.getUserIdFromToken(token)

		val tmp: List<User> = userRepository.findById(userId)

		if (tmp.isEmpty()) return notFound

		return tmp[0]
	}

	private fun TokenRepository.getUserIdFromToken(token: String): UUID{
		val checkedToken: Token = this.checkToken(token)
		return checkedToken.userId
	}

	private fun TokenRepository.checkToken(token: String): Token{
		val tokenList: List<Token> = this.findByToken(token)
		if (tokenList.isEmpty()) throw NotFound("This token is invalid")
		return tokenList[0]
	}
}