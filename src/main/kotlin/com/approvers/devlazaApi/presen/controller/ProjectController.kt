package com.approvers.devlazaApi.presen.controller

import com.approvers.devlazaApi.domain.data.Project
import com.approvers.devlazaApi.domain.data.ProjectMemberModifyRequest
import com.approvers.devlazaApi.domain.data.ProjectSearchOption
import com.approvers.devlazaApi.domain.service.ProjectService
import com.approvers.devlazaApi.error.BadRequest
import com.approvers.devlazaApi.infra.table.RecruitingState
import com.approvers.devlazaApi.security.RequireUser
import com.approvers.devlazaApi.security.isAdmin
import com.approvers.devlazaApi.security.user
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/projects")
class ProjectController(
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
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestBody project: Project
    ): Project {
        return projectService.create(project)
    }

    @GetMapping("{id}")
    fun get(
        @PathVariable id: String
    ): Project {
        return projectService.get(id)
    }

    @DeleteMapping("{id}")
    @RequireUser
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: String,
        @AuthenticationPrincipal token: UsernamePasswordAuthenticationToken
    ) {
        projectService.delete(id, token.user.id, token.isAdmin)
    }

    @GetMapping("search")
    fun search(
        @RequestParam
        sort: String,
        @RequestParam(required = false)
        user: String?,
        @RequestParam(required = false)
        tags: String?,
        @RequestParam
        recruiting: String,
        @RequestParam(required = false)
        name: String?,
        @RequestParam(required = false)
        createdBefore: LocalDate?,
        @RequestParam(required = false)
        createdAfter: LocalDate?,
        @RequestParam(defaultValue = "0")
        start: Long,
        @RequestParam(defaultValue = "20")
        end: Long
    ): List<Project> {
        return projectService.search {
            this.sort = try {
                ProjectSearchOption.Sort.valueOf(sort.toUpperCase())
            } catch (ex: Exception) {
                throw BadRequest("Sort option $sort is invalid.")
            }
            this.user = user
            this.tags = tags?.split("+") ?: listOf()
            this.recruiting = try {
                RecruitingState.valueOf(recruiting)
            } catch (ex: Exception) {
                throw BadRequest("Recruiting option $recruiting is invalid.")
            }
            this.name = name
            this.createdBefore = createdBefore
            this.createdAfter = createdAfter
            this.start = start
            this.end = end
        }
    }

    @PostMapping("{id}/members")
    @RequireUser
    fun addMember(
        @PathVariable id: String,
        @RequestBody request: ProjectMemberModifyRequest,
        @AuthenticationPrincipal token: UsernamePasswordAuthenticationToken
    ): Project {
        return projectService.addMember(id, request.userId, token.isAdmin)
    }

    @DeleteMapping("{id}/members")
    @RequireUser
    fun deleteMember(
        @PathVariable id: String,
        @RequestBody request: ProjectMemberModifyRequest,
        @AuthenticationPrincipal token: UsernamePasswordAuthenticationToken
    ): Project {
        return projectService.deleteMember(id, request.userId, token.isAdmin)
    }

    @PostMapping("{id}/favorite")
    @RequireUser
    fun addFavorite(
        @PathVariable id: String,
        @AuthenticationPrincipal token: UsernamePasswordAuthenticationToken
    ): Project {
        return projectService.addFavorite(id, token.user.id)
    }

    @DeleteMapping("{id}/favorite")
    @RequireUser
    fun removeFavorite(
        @PathVariable id: String,
        @AuthenticationPrincipal token: UsernamePasswordAuthenticationToken
    ): Project {
        return projectService.removeFavorite(id, token.user.id)
    }
}
