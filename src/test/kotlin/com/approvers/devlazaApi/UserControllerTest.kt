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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.servlet.HandlerExceptionResolver

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserCreateTest(
    @Autowired private val userController: UserController,
    @Autowired private val handlerExceptionResolver: HandlerExceptionResolver
) {
    private val mapper = jacksonObjectMapper()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(userController)
        .setHandlerExceptionResolvers(handlerExceptionResolver)
        .build()

    private lateinit var tokenCache: String

    @BeforeEach
    internal fun setup() {
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
    }

    @AfterEach
    fun postProcessing(){
        val tokenPoster = UserController.TokenPoster(token = tokenCache)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        )
    }

    @Test
    fun createUser() {
        val params = UserPoster(
            name = "test",
            password = "password",
            showId = "testID",
            mailAddress = "email@fuga.com"
        )

        val json: String = mapper.writeValueAsString(params)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isOk)
        mockMvc.perform(get("/users")).andExpect(status().isOk)

        mockMvc.perform(
            post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json)
        ).andExpect(status().isBadRequest)

        mockMvc.perform(get("/users/${params.showId}")).andExpect(status().isOk)

    }

    @Test
    fun loginTest() {
        val loginParam = LoginPoster(
            address = "email@hoge.com",
            password = "password"
        )

        val loginJson: String = mapper.writeValueAsString(loginParam)

        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isOk)
    }

    @Test
    fun failLoginWithMailAddress() {
        val loginPoster = LoginPoster(
            address = "invalid@hoge.com",
            password = "password"
        )

        val loginJson = mapper.writeValueAsString(loginPoster)
        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isNotFound)
    }

    @Test
    fun failLoginWithPassword() {
        val loginPoster = LoginPoster(
            address = "email@hoge.com",
            password = "invalid"
        )

        val loginJson = mapper.writeValueAsString(loginPoster)
        mockMvc.perform(
            post("/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson)
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun deleteUser() {
        val tokenPoster = UserController.TokenPoster(token = tokenCache)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isOk)
    }

    @Test
    fun failDeleteUserWithInvalidToken() {
        val tokenPoster = UserController.TokenPoster(token = "afdzgasnglksjfg.afsdaf.fsdfsdw")
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isBadRequest)
    }

    @Test
    fun failDeleteUserWithDeletedUserToken() {
        val tokenPoster = UserController.TokenPoster(token = tokenCache)
        val tokenJson: String = mapper.writeValueAsString(tokenPoster)
        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isOk)

        mockMvc.perform(
            delete("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(tokenJson)
        ).andExpect(status().isNotFound)
    }
}

