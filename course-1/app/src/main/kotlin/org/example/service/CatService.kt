package org.example.service

import org.example.model.Cat
import org.example.model.CatDto
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID

class CatService(private val clock: Clock) {

    private val cats = mutableListOf<Cat>()

    fun getCat(id: UUID): Cat? {
        return cats.find { it.id == id }
    }

    fun listCats(): List<Cat> {
        // if we return cats, callers get the reference, so they can change it
        // that's why we are returning immutable list
        return cats.toList()
    }

    fun deleteCat(id: UUID): Cat? {
        val cat = cats.find { it.id == id }
        cat?.let {
            cats.remove(it)
            return cat
        }
        return null
    }

    fun addCat(catDto: CatDto) {
        val cat = Cat(
            id = UUID.randomUUID(),
            createdAt = clock.instant(),
            name = catDto.name,
            dateOfBirth = catDto.dateOfBirth,
            breed = catDto.breed,
            color = catDto.color,
        )
        cats += cat
    }
}