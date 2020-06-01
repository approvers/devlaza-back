package dev.approvers.devlazaApi.infra.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Table

object TagsTable : UUIDTable() {
    val name = varchar("name", 20)
}

object TagsWithProjects : Table() {
    val project = reference("project", ProjectsTable).index()
    val tag = reference("tag", TagsTable).index()
}
