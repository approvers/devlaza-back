package com.approvers.devlazaApi.database.repository

import com.approvers.devlazaApi.data.Project
import java.util.UUID

interface ProjectRepository {
    fun get(id: UUID): Project?
    fun getAll(limit: Int): List<Project>
    fun delete(id: UUID)
    fun create(project: Project): Project
    fun update(project: Project): Project
}
