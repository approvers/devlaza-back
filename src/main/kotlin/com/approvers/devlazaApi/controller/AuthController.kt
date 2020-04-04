package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.controller.utils.TokenUtils
import com.approvers.devlazaApi.model.AuthPoster
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.repository.ProjectsRepository
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
		private val userRepository: UserRepository,
		private val projectsRepository: ProjectsRepository
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
		val tokenUtils = TokenUtils(tokenRepository, userRepository, projectsRepository)

		val userId: UUID = tokenUtils.getUserIdFromToken(token) ?: return notFound

		val tmp: List<User> = userRepository.findById(userId)

		if (tmp.isEmpty()) return notFound

		return tmp[0]
	}
}