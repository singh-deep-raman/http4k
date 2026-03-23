package org.example.docs

import org.example.model.Cat
import org.example.model.CatDto
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

val catSample = Cat(
    id = UUID.randomUUID(),
    userId = "admin",
    name = "Cat Name",
    createdAt = Instant.parse("2020-04-04T00:00:00Z"),
    dateOfBirth = LocalDate.of(2022, 1, 1),
    breed = "Breed name",
    color = "brown greyish"
)

val catDataSample = CatDto(
    name = "Cat Name",
    dateOfBirth = LocalDate.of(2022, 1, 1),
    breed = "Breed name",
    color = "brown greyish"
)