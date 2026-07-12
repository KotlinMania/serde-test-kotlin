// port-lint: tests lib.rs
package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serde.serdeCatching
import io.github.kotlinmania.serdecore.de.CharDeserialize
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.U32Deserialize
import io.github.kotlinmania.serdecore.de.mapDeserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import kotlin.test.Test

class LibTest {
    @Test
    fun testSerDeEmpty() {
        val map = LinkedMap(emptyList())
        assertTokens(
            map,
            LinkedMapDeserialize,
            listOf(
                Token.Map(0),
                Token.MapEnd,
            ),
        )
    }

    @Test
    fun testSerDe() {
        val map =
            LinkedMap(
                listOf(
                    'b' to 20u,
                    'a' to 10u,
                    'c' to 30u,
                ),
            )
        assertTokens(
            map,
            LinkedMapDeserialize,
            listOf(
                Token.Map(3),
                Token.CharValue('b'),
                Token.U32(20u),
                Token.CharValue('a'),
                Token.U32(10u),
                Token.CharValue('c'),
                Token.U32(30u),
                Token.MapEnd,
            ),
        )
    }
}

private data class LinkedMap(
    val entries: List<Pair<Char, UInt>>,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serdeCatching {
            val map = serializer.serializeMap(entries.size).getOrThrow()
            for ((key, value) in entries) {
                map.serializeEntry(CharValue(key), U32Value(value)).getOrThrow()
            }
            map.end().getOrThrow()
        }
}

private data class CharValue(
    val value: Char,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> = serializer.serializeChar(value)
}

private data class U32Value(
    val value: UInt,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> = serializer.serializeU32(value)
}

private data object LinkedMapDeserialize : Deserialize<LinkedMap> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<LinkedMap> =
        mapDeserialize(CharDeserialize, U32Deserialize).deserialize(deserializer).map { map ->
            LinkedMap(map.entries.map { it.key to it.value })
        }
}
