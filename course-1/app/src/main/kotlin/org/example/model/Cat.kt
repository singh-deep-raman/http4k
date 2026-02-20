package org.example.model

import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class Cat(
    val id: UUID,
    val userId: String,
    val createdAt: Instant,
    val name: String,
    val dateOfBirth: LocalDate,
    val breed: String,
    val color: String
)

// we need CatDto as we don't want to give outsiders permission to create id and createdAt
data class CatDto(
    val name: String,
    val dateOfBirth: LocalDate,
    val breed: String,
    val color: String,
)