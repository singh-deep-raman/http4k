package org.example.repository

import org.example.Cats
import org.example.CatsQueries
import org.example.model.Cat
import java.time.ZoneOffset
import java.util.UUID

class CatsRepository(
    private val catsQueries: CatsQueries
) {

    fun getCats(): List<Cat> {
        return catsQueries.listCats()
            .executeAsList()
            .map { it.toCat() }
    }

    fun getCatById(id: UUID): Cat? {
        return catsQueries.getCat(id.toString())
            .executeAsOneOrNull()
            ?.toCat()
    }

    fun createCat(cat: Cat) {
        catsQueries.createCat(
            cat.id.toString(),
            cat.createdAt.atOffset(ZoneOffset.UTC),
            cat.name,
            cat.dateOfBirth,
            cat.breed,
            cat.color
        )
    }

    fun deleteCatById(id: UUID) {
        catsQueries.deleteCat(id.toString())
    }
}

private fun Cats.toCat() = Cat(
    id = UUID.fromString(id),
    createdAt = created_at.toInstant(),
    color = color,
    breed = breed,
    name = name,
    dateOfBirth = birth_date
)
