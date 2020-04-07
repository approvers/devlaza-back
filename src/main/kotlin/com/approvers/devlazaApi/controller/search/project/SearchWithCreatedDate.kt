package com.approvers.devlazaApi.controller.search.project

import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.repository.ProjectsRepository
import java.time.LocalDate
import java.time.LocalDateTime

class SearchWithCreatedDate: ProjectSearcher<List<String?>>{
    override fun search(projectsSet: Set<Projects>, param: List<String?>, repository: ProjectsRepository): Set<Projects> {
        var result: Set<Projects> = projectsSet
        if (param[0] is String){
            result = findBySearchStartDate(param[0]!!, result)
        }

        if (param[1] is String){
            result = findBySearchEndDate(param[1]!!, result)
        }
        return result
    }

    private fun findBySearchStartDate(startDate: String, projects: Set<Projects>): Set<Projects>{
        val result: MutableSet<Projects> = mutableSetOf()

        val startSearchDate: LocalDateTime = LocalDate.parse(startDate).atTime(0, 0, 0)
        for (project in projects){
            if (startSearchDate.isBefore(project.created_at)){
                result.add(project)
            }
        }

        return projects.intersect(result)
    }

    private fun findBySearchEndDate(endDate: String, projects: Set<Projects>): Set<Projects>{
        val result: MutableSet<Projects> = mutableSetOf()

        val endSearchDate: LocalDateTime = LocalDate.parse(endDate).atTime(23, 59, 59)
        for (project in projects){
            if(endSearchDate.isAfter(project.created_at)){
                result.add(project)
            }
        }

        return projects.intersect(result)
    }
}