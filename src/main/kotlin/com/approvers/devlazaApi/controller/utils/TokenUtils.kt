package com.approvers.devlazaApi.controller.utils

import com.approvers.devlazaApi.model.Token
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.TokenRepository
import com.approvers.devlazaApi.repository.UserRepository
import java.util.*

class TokenUtils(
		private val tokenRepository: TokenRepository,
		private val userRepository: UserRepository,
		private val projectsRepository: ProjectsRepository
) {
	fun getUserIdFromToken(token: String): UUID?{
		val tokenList: List<Token> = tokenRepository.findByToken(token)

		if (tokenList.isEmpty()) return null

		return tokenList[0].userId
	}

	fun tokenCheck(token: String): Token?{
		val tokenList: List<Token> = tokenRepository.findByToken(token)
		if (tokenList.isEmpty()) return null
		return tokenList[0]
	}

	fun convertToTokenAndProjectIdMap(token: String, rawId: String): Pair<UUID, UUID>?{
		val utils = BaseUtils()
		val userId: UUID = getUserIdFromToken(token)?: return null

		val projectId: UUID = utils.convertStringToUUID(rawId)?: return null

		if (userRepository.findById(userId).isEmpty()) return null
		if (projectsRepository.findById(projectId).isEmpty()) return null

		return Pair(userId, projectId)
	}
}