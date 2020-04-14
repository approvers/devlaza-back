package com.approvers.devlazaApi

import com.approvers.devlazaApi.controller.ProjectsController
import com.approvers.devlazaApi.model.LoginPoster
import com.approvers.devlazaApi.model.ProjectPoster
import com.approvers.devlazaApi.model.Projects
import com.approvers.devlazaApi.model.UserPoster
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ProjectLeaveFailTest(
    @Autowired private val mockMvc: MockMvc
) {
    private val mapper = jacksonObjectMapper()

    private lateinit var tokenCache: String
    private lateinit var joinUserTokenCache: String
    private lateinit var projectIDCache: UUID

    @BeforeEach
    fun setup() {
        mapper.registerModule(JavaTimeModule())
        val params = UserPoster(
            name = "user",
            password = "password",
            showId = "ID",
            mailAddress = "email@hoge.com"
        )

        val json: String = mapper.writeValueAsString(params)

        mockMvc.perform(
            post("/users")
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

        val joinUserParams = UserPoster(
            name = "joinUser",
            password = "password",
            showId = "joinID",
            mailAddress = "test@hoge.com"
        )

        val joinUserJson: String = mapper.writeValueAsString(joinUserParams)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(joinUserJson)
        )

        val joinUserLoginPoster = LoginPoster(
            address = joinUserParams.mailAddress,
            password = joinUserParams.password
        )

        val joinUserLoginJson: String = mapper.writeValueAsString(joinUserLoginPoster)

        val joinUserResponse = mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(joinUserLoginJson)
        ).andReturn()

        joinUserTokenCache = joinUserResponse.response.contentAsString

        val projectParam = ProjectPoster(
            name = "test",
            introduction = "intro",
            token = tokenCache,
            sites = "testSite,url",
            tags = "testTag"
        )

        val postJson = mapper.writeValueAsString(projectParam)

        val projectPostResult = mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson)
        ).andReturn()

        val projects: Projects = mapper.readValue(projectPostResult.response.contentAsString, Projects::class.java)
        projectIDCache = projects.id!!
        val tokenJson: String = generateTokenJson(joinUserTokenCache)
        postRequestToProject("/${projectIDCache}/join", tokenJson)
    }

    @AfterEach
    fun postProcessing() {
        val tokenJson: String = generateTokenJson(tokenCache)
        postRequestToProject("/${projectIDCache}/leave", tokenJson)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        )
    }

    @Test
    fun failLeaveFromProjectWithInvalidToken() {
        val invalidToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJBcHByb3ZlcnMiLCJVU0VSX0lEIjoiM2VkNDgyZWYtNDI0Ni00N2M5LTg5NjctNzNiMDI2MWVkZDMzIiwiVVNFUl9uYW1lIjoidXNlciIsImlhdCI6MTU4Njg5NTg0NywianRpIjoiYmQ0NDFlOTAtZTRiZC00MzM3LWJiYzctZjFmMWI0ZGI1YzRlIn0.yCJMv439yn8s9Hw5OxGPtM6yPx-rDDdmKJwJhKcbbws"
        val invalidTokenJson: String = generateTokenJson(invalidToken)

        postRequestToProject("/${projectIDCache}/leave", invalidTokenJson).andExpect(status().isNotFound)
    }

    @Test
    fun failLeaveFromProjectWithProjectIDNotFound() {
        val tokenJson: String = generateTokenJson(joinUserTokenCache)
        val invalidProjectID: String = UUID.randomUUID().toString()

        postRequestToProject("/${invalidProjectID}/leave", tokenJson).andExpect(status().isNotFound)
    }

    @Test
    fun failLeaveFromProjectWithLeaveOwner() {
        val creatorTokenJson: String = generateTokenJson(tokenCache)

        postRequestToProject("/${projectIDCache}/leave", creatorTokenJson).andExpect(status().isBadRequest)
    }

    @Test
    fun failLeaveFromProjectWithDidNotJoinToProject(){
        val tokenJson: String = generateTokenJson(joinUserTokenCache)
        postRequestToProject("/${projectIDCache}/leave", tokenJson)
            .andExpect(status().isNoContent)
        postRequestToProject("/${projectIDCache}/leave", tokenJson)
            .andExpect(status().isNotFound)
    }

    private fun postRequestToProject(
        path: String = "",
        contentJson: String = ""
    ): ResultActions {
        return mockMvc.perform(
            post("/projects${path}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(contentJson)
        )
    }

    private fun generateTokenJson(token: String): String {
        val tokenParam = ProjectsController.TokenPoster(token = token)
        return mapper.writeValueAsString(tokenParam)
    }
}