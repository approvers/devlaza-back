package com.approvers.devlazaApi

import com.approvers.devlazaApi.controller.UserController
import com.approvers.devlazaApi.model.AuthPoster
import com.approvers.devlazaApi.model.LoginPoster
import com.approvers.devlazaApi.model.UserPoster
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class AuthControllerTest(
    @Autowired private val mockMvc: MockMvc
) {
    private val mapper = jacksonObjectMapper()
    private lateinit var tokenCache: String

    @BeforeEach
    fun setup() {
        val params = UserPoster(
            name = "user",
            password = "password",
            showId = "ID",
            mailAddress = "email@hoge.com"
        )

        val json: String = mapper.writeValueAsString(params)

        mockMvc.perform(
            post("/test/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        )

        val loginPoster = LoginPoster(
            address = params.mailAddress,
            password = params.password
        )

        val loginJson: String = mapper.writeValueAsString(loginPoster)

        val response = mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andReturn()

        tokenCache = response.response.contentAsString
    }

    @AfterEach
    fun postProcessing() {
        val tokenPoster = UserController.TokenPoster(token = tokenCache)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        )
    }

    @Test
    fun getUserInfoTest() {
        val tokenPoster = AuthPoster(token = tokenCache)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)

        mockMvc.perform(
            post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isOk)
    }

    @Test
    fun failGetUserInfoWithInvalidTest() {
        val tokenPoster = AuthPoster(token = "GGAADGNA.ASGSBSGA.GA43GA2")
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)

        mockMvc.perform(
            post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun failGetUserInfoWithUserNotFound() {
        val invalidToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJBcHByb3ZlcnMiLCJVU0VSX0lEIjoiM2VkNDgyZWYtNDI0Ni00N2M5LTg5NjctNzNiMDI2MWVkZDMzIiwiVVNFUl9uYW1lIjoidXNlciIsImlhdCI6MTU4Njg5NTg0NywianRpIjoiYmQ0NDFlOTAtZTRiZC00MzM3LWJiYzctZjFmMWI0ZGI1YzRlIn0.yCJMv439yn8s9Hw5OxGPtM6yPx-rDDdmKJwJhKcbbws"
        val tokenPoster = AuthPoster(token = invalidToken)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)

        mockMvc.perform(
            post("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isNotFound)
    }
}
