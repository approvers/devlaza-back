package dev.approvers.devlazaApi.domain.service

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import dev.approvers.devlazaApi.domain.data.User
import dev.approvers.devlazaApi.domain.data.UserRole
import dev.approvers.devlazaApi.domain.repository.UserRepository
import dev.approvers.devlazaApi.error.BadRequest
import dev.approvers.devlazaApi.error.NotFound
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UserServiceTest
@Autowired
constructor(
    private val passwordEncoder: PasswordEncoder
) {
    private val users = listOf(
        User(
            id = UUID.fromString("dcaa50c5-c031-46ac-b2b9-44d1f10ee6c3"),
            name = "isso424",
            birthday = LocalDate.of(2020, 5, 19),
            bio = "限界開発鯖",
            favoriteLang = "Kotlin",
            displayId = "isso424",
            password = "password",
            mailAddress = "test@example.com",
            role = UserRole.USER
        ),
        User(
            id = UUID.fromString("52f50bed-0faf-4f13-b73f-550c314d7b00"),
            name = "mirror-kt",
            birthday = LocalDate.of(2020, 5, 20),
            bio = "Hello World",
            favoriteLang = "Kotlin",
            displayId = "mirror-kt",
            password = "mypassword",
            mailAddress = "mirrorkt@exapmle.com",
            role = UserRole.ADMIN
        )
    )

    @Test
    @DisplayName("ユーザーを指定件数取得(期待値:成功)")
    fun getAll_success_Test() {
        val userRepository = mock<UserRepository> {
            whenever(mock.getAll(any())).then {
                val limit = it.getArgument<Int>(0)
                users.take(limit)
            }
        }
        val userService = UserService(userRepository, passwordEncoder)

        val result = userService.getAll(20)
        assertTrue(result.size < 20)
        assertFalse(result.any { it.password.isNotEmpty() })
    }

    @Test
    @DisplayName("ユーザーを指定件数取得(期待値:空)")
    fun getAll_empty_Test() {
        val userRepository = mock<UserRepository> {
            whenever(mock.getAll(any())).then {
                listOf<User>()
            }
        }
        val userService = UserService(userRepository, passwordEncoder)

        val result = userService.getAll(20)
        assertTrue(result.isEmpty())
    }

    @Test
    @DisplayName("ユーザーをIDから取得(期待値:成功)")
    fun get_success_Test() {
        val userRepository = mock<UserRepository> {
            whenever(mock.get(any())).then {
                val id = it.getArgument<UUID>(0)
                users.singleOrNull { user -> user.id == id }
            }
        }
        val userService = UserService(userRepository, passwordEncoder)

        val id = UUID.fromString("dcaa50c5-c031-46ac-b2b9-44d1f10ee6c3")
        val result = userService.get(id)
        assertEquals(result.id, id)
        assertTrue(result.password.isEmpty())
    }

    @Test
    @DisplayName("ユーザーをIDから取得(期待値:失敗)")
    fun get_fail_Test() {
        val userRepository = mock<UserRepository> {
            whenever(mock.get(any())).then {
                null
            }
        }
        val userService = UserService(userRepository, passwordEncoder)

        val id = UUID.randomUUID()
        assertFailsWith<NotFound> {
            userService.get(id)
        }
    }

    @Test
    @DisplayName("ユーザー情報の更新(期待値:成功)")
    fun edit_success_Test() {
        val userRepository = mock<UserRepository> {
            whenever(mock.update(any())).then {
                val user = it.getArgument<User>(0)
                user
            }
        }
        val userService = UserService(userRepository, passwordEncoder)

        val user = users[0]
        val id = user.id.toString()

        val result = userService.edit(id, user.apply {
            name = "fooo"
            password = "MyWorld"
        })
        assertEquals(user, result)
    }

    @Test
    @DisplayName("ユーザー情報の更新(期待値:失敗)")
    fun edit_fail_Test() {
        val userRepository = mock<UserRepository> {
            whenever(mock.update(any())).then {
                val user = it.getArgument<User>(0)
                user
            }
        }
        val userService = UserService(userRepository, passwordEncoder)

        val user = users[0]

        assertFailsWith<BadRequest> {
            userService.edit("aaaaaaaaaa", user.apply {
                name = "fooo"
                password = "MyWorld"
            })
        }
    }
}
