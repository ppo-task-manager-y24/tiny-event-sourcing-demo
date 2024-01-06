package ru.quipy.status.dto

import java.util.*

class StatusViewModel(
        val statusId: UUID,
        val projectId: UUID,
        val statusName: String,
        val color: Int
)