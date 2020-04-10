package com.approvers.devlazaApi

// import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DevlazaApiApplicationTests(
    @Autowired private val restTemplate: TestRestTemplate
) {

//    @Test
//    fun contextLoads() {
//    }

    @Test
    fun requestToProjects() {
//        this.mockMvc.perform(get("/projects/")).andExpect(status().isOk)
        val entity = restTemplate.getForEntity<String>("/projects/")
        assert(entity.statusCode.is2xxSuccessful)
    }

}
