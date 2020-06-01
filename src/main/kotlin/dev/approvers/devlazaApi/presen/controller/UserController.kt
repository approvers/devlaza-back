package dev.approvers.devlazaApi.presen.controller

import dev.approvers.devlazaApi.domain.data.NonAuthorizedUser
import dev.approvers.devlazaApi.domain.data.User
import dev.approvers.devlazaApi.domain.service.NonAuthorizedUserService
import dev.approvers.devlazaApi.domain.service.UserService
import dev.approvers.devlazaApi.security.RequireUser
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val nonAuthorizedUserService: NonAuthorizedUserService
) {
    @GetMapping
    fun getAll(
        @RequestParam(defaultValue = "20") limit: Int
    ): List<User> {
        return userService.getAll(limit)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun new(
        @RequestBody user: User
    ): NonAuthorizedUser {
        return nonAuthorizedUserService.create(user)
    }

    @PostMapping("auth/{token}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun authorize(@PathVariable token: String): User {
        return nonAuthorizedUserService.authorize(token)
    }

    @PatchMapping("{id}")
    @RequireUser
    fun edit(
        @PathVariable id: String,
        @RequestBody user: User
    ): User {
        return userService.edit(id, user)
    }
}
