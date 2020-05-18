package dev.approvers.devlazaApi.domain.data

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDate
import java.util.UUID

data class User(
    val id: UUID? = null,
    var name: String,
    var birthday: LocalDate?,
    var bio: String?,
    var favoriteLang: String?,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var password: String,
    var displayId: String,
    var mailAddress: String,
    var role: UserRole
)
