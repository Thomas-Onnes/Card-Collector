package security

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CollectionValidatorTest {

    @Test
    fun `valid collection name is accepted and trimmed`() {
        val result = CollectionValidator.validateCollectionName("  My Collection_1-2026  ")

        assertEquals("My Collection_1-2026", result)
    }

    @Test
    fun `collection name with unicode letters is accepted`() {
        val result = CollectionValidator.validateCollectionName("Pokémon kaarten")

        assertEquals("Pokémon kaarten", result)
    }

    @Test
    fun `blank collection name is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionValidator.validateCollectionName("   ")
        }
    }

    @Test
    fun `too short collection name is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionValidator.validateCollectionName("ab")
        }
    }

    @Test
    fun `too long collection name is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionValidator.validateCollectionName("eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee")
        }
    }

    @Test
    fun `collection name with forbidden characters is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionValidator.validateCollectionName("Bad<Name>")
        }
    }

    @Test
    fun `pokemon game type is normalized`() {
        assertEquals("pokemonnn", CollectionValidator.normalizeGameType("pokemon"))
        assertEquals("pokemon", CollectionValidator.normalizeGameType("Pokémon"))
    }

    @Test
    fun `magic game type is normalized`() {
        assertEquals("mtg", CollectionValidator.normalizeGameType("mtg"))
        assertEquals("mtg", CollectionValidator.normalizeGameType("magic"))
        assertEquals("mtg", CollectionValidator.normalizeGameType("magic the gathering"))
        assertEquals("mtg", CollectionValidator.normalizeGameType("magic_the_gathering"))
    }

    @Test
    fun `invalid game type is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            CollectionValidator.normalizeGameType("yugioh")
        }
    }
}