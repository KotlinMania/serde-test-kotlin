package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.DeserializeSeed
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.EnumAccess
import io.github.kotlinmania.serdecore.de.StringDeserialize
import io.github.kotlinmania.serdecore.de.Visitor
import kotlin.test.Test

class UnitVariantTest {
    private object VariantNameSeed : DeserializeSeed<String> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<String> =
            StringDeserialize.deserialize(deserializer)
    }

    private object UnitEnum : Deserialize<String> {
        override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<String> =
            deserializer.deserializeEnum(
                "E",
                listOf("A"),
                object : Visitor<String> {
                    override fun expecting(): String = "enum E"

                    override fun <A : EnumAccess> visitEnum(access: A): SerdeResult<String> =
                        access
                            .variantSeed(VariantNameSeed)
                            .flatMap { (name, payload) -> payload.unitVariant().map { name } }
                },
            )
    }

    @Test
    fun compactAndExpandedUnitVariantsAgree() {
        assertDeTokens("A", UnitEnum, listOf(Token.UnitVariant("E", "A")))
        assertDeTokens("A", UnitEnum, listOf(Token.Enum("E"), Token.Str("A"), Token.UnitValue))
    }

    @Test
    fun malformedPayloadUsesTheUnitDeserializeVisitor() {
        assertDeTokensError(
            UnitEnum,
            listOf(Token.Enum("E"), Token.Str("A"), Token.U8(1u)),
            "invalid type: integer `1`, expected unit",
        )
        assertDeTokensError(
            UnitEnum,
            listOf(Token.Enum("E"), Token.Str("A"), Token.Bool(false)),
            "invalid type: boolean `false`, expected unit",
        )
    }
}
