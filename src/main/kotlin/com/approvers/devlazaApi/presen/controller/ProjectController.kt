package com.approvers.devlazaApi.presen.controller

import com.approvers.devlazaApi.domain.data.Project
import com.approvers.devlazaApi.domain.service.ProjectService
import com.approvers.devlazaApi.security.RequireUser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/projects")
class ProjectController(
    @Autowired
    private val projectService: ProjectService
) {
    @GetMapping
    fun getAll(
        @RequestParam("limit") limit: Int
    ): ResponseEntity<List<Project>> {
        val result = projectService.getAll(limit)
        return ResponseEntity.ok(result)
    }

    @PostMapping
    @RequireUser
    fun create(
        @AuthenticationPrincipal token: UsernamePasswordAuthenticationToken
    ) {

    }
}
