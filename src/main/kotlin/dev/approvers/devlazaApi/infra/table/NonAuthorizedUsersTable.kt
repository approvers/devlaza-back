package dev.approvers.devlazaApi.infra.table

import dev.approvers.devlazaApi.domain.data.UserRole
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.date

object NonAuthorizedUsersTable : UUIDTable() {
    val name = varchar("name", 20)
    val birthday = date("birthday").nullable()
    val bio = varchar("bio", 200).nullable()
    val favoriteLang = varchar("favorite_lang", 20).nullable()
    val password = varchar("password", 60)
    val displayId = varchar("display_id", 20)
    val mailAddress = varchar("mail_address", 25).index()
    val role = enumeration("role", UserRole::class)

    val token = uuid("token").index()
}
