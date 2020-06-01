package dev.approvers.devlazaApi.util

import dev.approvers.devlazaApi.error.BadRequest
import java.util.UUID
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun String.toUuid(): UUID = try {
    UUID.fromString(this)
} catch (ex: Exception) {
    throw BadRequest("Invalid UUID: $this")
}

@OptIn(ExperimentalContracts::class)
fun UUID?.checkNotNull(target: String) {
    contract {
        returns() implies (this@checkNotNull != null)
    }

    if (this == null) throw BadRequest("$target id is must not be null.")
}
