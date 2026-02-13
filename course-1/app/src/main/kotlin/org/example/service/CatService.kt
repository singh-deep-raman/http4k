package org.example.service

import org.example.model.Cat
import org.example.model.CatDto
import java.time.Clock
import java.util.*

// for JsonApprovalTesting, we can't test it with a random uuid, so injecting one
class CatService(private val clock: Clock,
    private val uuidProvider: () -> UUID = { UUID.randomUUID() }
    ) {

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

    fun addCat(catDto: CatDto): Cat {
        val cat = Cat(
            id = uuidProvider(),
            createdAt = clock.instant(),
            name = catDto.name,
            dateOfBirth = catDto.dateOfBirth,
            breed = catDto.breed,
            color = catDto.color,
        )
        cats += cat
        return cat
    }
}