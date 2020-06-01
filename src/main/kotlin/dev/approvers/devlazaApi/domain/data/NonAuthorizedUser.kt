package dev.approvers.devlazaApi.domain.data

import java.util.UUID

data class NonAuthorizedUser(
    val user: User,
    val token: UUID
)
