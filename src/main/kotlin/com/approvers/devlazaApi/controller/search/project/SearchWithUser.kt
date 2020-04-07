package com.approvers.devlazaApi.controller.search.project

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.repository.ProjectsRepository
import com.approvers.devlazaApi.repository.UserRepository
import java.util.*

class SearchWithUser: ProjectSearcherWithOtherRepository<String?, UserRepository> {
    override fun search(projectsSet: Set<Projects>, param: String?, repository: ProjectsRepository, secondaryRepository: UserRepository): Set<Projects> {
        if (param !is String) return projectsSet
        val createdUserList = secondaryRepository.findByNameLike("%$param%")

        if (createdUserList.isEmpty()) return setOf()

        val createdUserID: UUID = createdUserList[0].id!!

        val result: Set<Projects> = repository.findByCreatedUserId(createdUserID).toSet()

        return projectsSet.intersect(result)
    }
}