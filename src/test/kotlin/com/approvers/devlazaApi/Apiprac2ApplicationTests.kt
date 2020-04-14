package com.approvers.devlazaApi

// import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class DevlazaApiApplicationTests(
    @Autowired private val mockMvc: MockMvc
) {
//    @Test
//    fun contextLoads() {
//    }

    @Test
    fun requestToProjects() {
        mockMvc.perform(get("/projects")).andExpect(status().isNoContent)
    }

}
