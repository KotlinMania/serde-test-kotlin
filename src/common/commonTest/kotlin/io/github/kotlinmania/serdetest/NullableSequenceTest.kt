package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serdecore.de.U32Deserialize
import io.github.kotlinmania.serdecore.de.mutableListDeserialize
import io.github.kotlinmania.serdecore.de.nullableDeserialize
import kotlin.test.Test
import kotlin.test.assertEquals

class NullableSequenceTest {
    private val deserialize = mutableListDeserialize(nullableDeserialize(U32Deserialize))
    private val tokens = listOf(Token.Seq(null), Token.None, Token.Some, Token.U32(7u), Token.None, Token.SeqEnd)

    @Test
    fun tokenSequenceRetainsNullAndConsumesEndToken() {
        val input = Deserializer.new(tokens)
        assertEquals(listOf(null, 7u, null), deserialize.deserialize(input).getOrThrow())
        assertEquals(0, input.remaining())
    }

    @Test
    fun configuredSequenceRetainsNullAndConsumesEndToken() {
        val input = Deserializer.new(tokens)
        assertEquals(listOf(null, 7u, null), deserialize.compact().deserialize(input).getOrThrow())
        assertEquals(0, input.remaining())
    }
}
