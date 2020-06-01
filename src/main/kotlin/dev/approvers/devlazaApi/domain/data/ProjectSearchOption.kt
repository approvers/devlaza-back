package dev.approvers.devlazaApi.domain.data

import dev.approvers.devlazaApi.infra.table.RecruitingState
import java.time.LocalDate

class ProjectSearchOption {
    enum class Sort {
        ASC,
        DESC,
        POPULAR
    }

    var sort: Sort = Sort.ASC
    var user: String? = null
    var tags: List<String> = listOf()
    var recruiting: RecruitingState = RecruitingState.RECRUITING
    var name: String? = null
    var createdBefore: LocalDate? = null
    var createdAfter: LocalDate? = null
    var start: Long = 0
    var end: Long = 20
}
