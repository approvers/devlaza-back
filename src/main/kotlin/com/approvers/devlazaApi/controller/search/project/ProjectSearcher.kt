package com.approvers.devlazaApi.controller.search.project

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.repository.ProjectsRepository

interface ProjectSearcher<T> {
    fun search(projectsSet:Set<Projects>, param:T, repository: ProjectsRepository): Set<Projects>
}

interface ProjectSearcherWithOtherRepository<T, Repository>{
    fun search(projectsSet: Set<Projects>, param: T, repository: ProjectsRepository, secondaryRepository: Repository): Set<Projects>
}
