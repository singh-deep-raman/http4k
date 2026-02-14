package org.example.service

import org.example.model.Cat
import org.example.model.CatDto
import org.example.repository.CatsRepository
import java.time.Clock
import java.util.*

// for JsonApprovalTesting, we can't test it with a random uuid, so injecting one
class CatService(
    private val catsRepository: CatsRepository,
    private val clock: Clock,
    private val uuidProvider: () -> UUID = { UUID.randomUUID() }
    ) {

    private val cats = mutableListOf<Cat>()

    fun getCat(id: UUID): Cat? {
        return catsRepository.getCatById(id)
    }

    fun listCats(): List<Cat> {
        return catsRepository.getCats()
    }

    fun deleteCat(id: UUID): Cat? {
        return catsRepository.getCatById(id)
            ?.let {
                catsRepository.deleteCatById(id)
                it
            }
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
        catsRepository.createCat(cat)
        return cat
    }
}