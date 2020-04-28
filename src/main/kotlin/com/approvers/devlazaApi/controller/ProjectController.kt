package com.approvers.devlazaApi.controller

import com.approvers.devlazaApi.database.entity.ProjectEntity
import com.approvers.devlazaApi.request.project.NewProjectRequest
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
class ProjectController {
    @GetMapping
    fun get(
            @RequestParam("max", defaultValue = "20") max: Int
    ): ResponseEntity<List<ProjectEntity>> {
        val res = transaction {
            ProjectEntity.all()
                    .limit(max)
                    .sortedByDescending { it.createdAt }
        }
        return ResponseEntity.ok(res)
    }

    @PostMapping
    fun addProject(
            @RequestBody project: NewProjectRequest
    ) {

    }
}