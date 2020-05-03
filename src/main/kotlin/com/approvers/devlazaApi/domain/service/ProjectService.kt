package com.approvers.devlazaApi.domain.service

import com.approvers.devlazaApi.domain.data.Project
import com.approvers.devlazaApi.domain.repository.ProjectRepository
import org.springframework.stereotype.Service

@Service
class ProjectService(
    private val projectRepository: ProjectRepository
) {
    fun getAll(limit: Int): List<Project> = projectRepository.getAll(limit)
}
