package com.approvers.devlazaApi.database.table

import com.approvers.devlazaApi.data.AuthorizationState
import com.approvers.devlazaApi.data.UserRole
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.`java-time`.date

// TODO: メール認証の部分を別テーブルに切り出せないか確認する
object UsersTable: UUIDTable() {
    val name = varchar("name", 20)
    val birthday = date("birthday").nullable()
    val bio = varchar("bio", 200).nullable()
    val favoriteLang = varchar("favorite_lang", 20).nullable()
    val password = varchar("password", 60) // BECryptoのハッシュ化文字列は60文字
    val displayId = varchar("display_id", 20)
    val mailAuthorizeState = enumeration("mail_authorize_state", AuthorizationState::class)
    val mailAddress = varchar("mail_address", 25).index()
    val role = enumeration("role", UserRole::class)
}

