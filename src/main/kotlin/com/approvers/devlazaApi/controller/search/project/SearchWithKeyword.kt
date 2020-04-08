package com.approvers.devlazaApi.controller.search.project

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.repository.ProjectsRepository

class SearchWithKeyword : ProjectSearcher<String?> {
    override fun search(projectsSet: Set<Projects>, param: String?, repository: ProjectsRepository): Set<Projects> {
        var projects: Set<Projects> = projectsSet

        if (param !is String) return projects

        val params: List<String> = param.split(" ")

        for (key in params) {
            val searchResults: Set<Projects> = repository.findByNameLike("%$key%").toSet()
            projects = projects.intersect(searchResults)
        }

        return projects
    }
}
