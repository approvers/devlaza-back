package com.approvers.devlazaApi.domain.repository

import com.approvers.devlazaApi.domain.data.Project
import java.util.UUID

interface ProjectRepository {
    fun get(id: UUID): Project?
    fun getAll(limit: Int): List<Project>
    fun delete(id: UUID)
    fun create(project: Project): Project
    fun update(project: Project): Project
}
