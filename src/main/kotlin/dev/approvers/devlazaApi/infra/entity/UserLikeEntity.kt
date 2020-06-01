package dev.approvers.devlazaApi.infra.entity

import dev.approvers.devlazaApi.domain.data.User
import dev.approvers.devlazaApi.domain.data.UserRole
import java.time.LocalDate
import java.util.UUID

interface UserLikeEntity {
    val userId: UUID
    var name: String
    var birthday: LocalDate?
    var bio: String?
    var favoriteLang: String?
    var password: String
    var displayId: String
    var mailAddress: String
    var role: UserRole
}

fun UserLikeEntity.toData(): User = User(
    id = this.userId,
    name = this.name,
    birthday = this.birthday,
    bio = this.bio,
    favoriteLang = this.favoriteLang,
    password = this.password,
    displayId = this.displayId,
    mailAddress = this.mailAddress,
    role = this.role
)

fun <T : UserLikeEntity> T.apply(user: User): T = apply {
    name = user.name
    birthday = user.birthday
    bio = user.bio
    favoriteLang = user.favoriteLang
    password = user.password
    displayId = user.displayId
    mailAddress = user.mailAddress
    role = user.role
}
