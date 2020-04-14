package com.approvers.devlazaApi

import com.approvers.devlazaApi.controller.UserController
import com.approvers.devlazaApi.model.LoginPoster
import com.approvers.devlazaApi.model.ProjectPoster
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ProjectControllerTest(
    @Autowired private val mockMvc: MockMvc
) {

    private val mapper = jacksonObjectMapper()
    private lateinit var tokenCache: String

    @BeforeEach
    fun createAndLoginUser() {
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
        val projectParam = ProjectPoster(
            name = "test",
            introduction = "intro",
            token = tokenCache,
            sites = "testSite,url",
            tags = "testTag"
        )

        val postJson = mapper.writeValueAsString(projectParam)

        mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson)
        )

        val projectParam2 = ProjectPoster(
            name = "test2",
            introduction = "intro",
            token = tokenCache,
            sites = "site,url",
            tags = "testTag2"
        )

        val postJson2 = mapper.writeValueAsString(projectParam2)

        mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson2)
        )
    }

    @AfterEach
    fun deleteUser() {
        val tokenPoster = UserController.TokenPoster(token = tokenCache)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        )
    }

    @Test
    fun createProject() {
        val projectParam = ProjectPoster(
            name = "projectName1",
            introduction = "intro",
            token = tokenCache,
            sites = "site,url",
            tags = "tag1+tag2"
        )

        val postJson = mapper.writeValueAsString(projectParam)

        mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson)
        )

        mockMvc.perform(
            get("/projects")
        ).andExpect(status().isOk)
    }

    @Test
    fun failCreateProjectWithInvalidToken() {
        val projectParam = ProjectPoster(
            name = "projectName1",
            introduction = "intro",
            token = "GAGGVSGRBRZS.FAGSEHGRE.VSEZRS",
            sites = "site,url",
            tags = "tag1+tag2"
        )

        val postJson = mapper.writeValueAsString(projectParam)

        mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun failCreateProjectWithWrongRequestBody(){
        val projectParam = ProjectPoster(
            name = "projectName2",
            introduction = "intro",
            token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJBcHByb3ZlcnMiLCJVU0VSX0lEIjoiMWVhOWM0ZGItNjQxMS00N2EwLWE2YTEtOWU4OTQ0M2MzZDI1IiwiVVNFUl9uYW1lIjoidXNlciIsImlhdCI6MTU4Njg4MzA3NCwianRpIjoiOTc5YzJmMTQtYzk2ZC00NTIxLWI4OWYtZTYwYjE4YjlhZTljIn0.0FjFSojUL3bUccH6B_QmTM4ZM6nFlnJwISFEwSfBdR0",
            sites = "site,url",
            tags = "tag1+tag2"
        )

        val postJson = mapper.writeValueAsString(projectParam)
        mockMvc.perform(
            post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(postJson)
        ).andExpect(status().isNotFound)
    }
}