package com.approvers.devlazaApi.controller.search.project

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.repository.ProjectsRepository

class SearchWithRecruiting: ProjectSearcher<Int> {
    override fun search(projectsSet: Set<Projects>, param: Int, repository: ProjectsRepository): Set<Projects> {
        if (when(param){1,0 -> true else -> false}){
            val recruitingResult: List<Projects> = repository.findByRecruiting(param)
            return projectsSet.intersect(recruitingResult)
        }

        return projectsSet
    }
}