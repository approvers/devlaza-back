package com.approvers.devlazaApi

import com.approvers.devlazaApi.errors.BadRequest
import com.approvers.devlazaApi.model.User
import com.approvers.devlazaApi.model.UserPoster
import com.approvers.devlazaApi.repository.UserRepository
import org.jetbrains.annotations.TestOnly
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/test")
class TestOnlyController(
    private val userRepository: UserRepository
) {
    @PostMapping("/users")
    @TestOnly
    fun createUserWithOutMailAuthorize(
        @Valid @RequestBody userPoster: UserPoster
    ): ResponseEntity<User> {
        val sameMailAddressChecker: List<User> = userRepository.findByMailAddress(userPoster.mailAddress)
        if (sameMailAddressChecker.isNotEmpty()) throw BadRequest("The email address is already in use.")

        val newUser = User(
            name = userPoster.name,
            passWord = userPoster.password,
            showId = userPoster.showId,
            mailAddress = userPoster.mailAddress,
            mailAuthorized = 1
        )

        return ResponseEntity.ok(userRepository.save(newUser))
    }
}
