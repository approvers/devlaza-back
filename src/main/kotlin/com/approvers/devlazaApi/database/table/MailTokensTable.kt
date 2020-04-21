package com.approvers.devlazaApi.database.table

import org.jetbrains.exposed.sql.Table

object MailTokensTable : Table() {
    // TODO: lengthを確認
    val token = varchar("token", 20).uniqueIndex()
    val user = reference("user", UsersTable)
}