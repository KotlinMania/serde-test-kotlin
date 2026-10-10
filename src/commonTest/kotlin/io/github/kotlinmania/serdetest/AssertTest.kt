// port-lint: tests assert.rs
package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.BooleanDeserialize
import io.github.kotlinmania.serdecore.de.I32Deserialize
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.Serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AssertTest {
    @Test
    fun testAssertSerTokensSuccess() {
        val value = BoolValue(true)
        assertSerTokens(value, listOf(Token.Bool(true)))
    }

    @Test
    fun testAssertSerTokensErrorMismatchedToken() {
        val value = BoolValue(true)
        val error =
            assertFailsWith<IllegalStateException> {
                assertSerTokens(value, listOf(Token.Bool(false)))
            }
        assertEquals(
            "value failed to serialize: expected Token::Bool(value=false) but serialized as Bool(value=true)",
            error.message,
        )
    }

    @Test
    fun testAssertSerTokensErrorExpectedFailure() {
        val value = BoolValue(true)
        assertSerTokensError(
            value,
            listOf(Token.Bool(false)),
            "expected Token::Bool(value=false) but serialized as Bool(value=true)",
        )
    }

    @Test
    fun testAssertDeTokensSuccess() {
        val expected = 42
        assertDeTokens(
            expected,
            I32Deserialize,
            listOf(Token.I32(42)),
        )
    }

    @Test
    fun testAssertDeTokensErrorExpectedFailure() {
        assertDeTokensError(
            BooleanDeserialize,
            listOf(Token.I32(100)),
            "invalid type: Signed(value=100), expected a boolean",
        )
    }

    @Test
    fun testConfigureReadableAndCompact() {
        val value = AdaptiveValue("human", 123)
        assertSerTokens(
            Readable(value),
            listOf(Token.Str("human")),
        )
        assertSerTokens(
            Compact(value),
            listOf(Token.I32(123)),
        )
    }

    @Test
    fun testSerdeTestInfoCrateVersion() {
        assertEquals("1.0.176", SerdeTestInfo.CRATE_VERSION)
    }
}

private data class BoolValue(
    val value: Boolean,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        serializer.serializeBool(value)
}

private data class AdaptiveValue(
    val text: String,
    val number: Int,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        if (serializer.isHumanReadable()) {
            serializer.serializeStr(text)
        } else {
            serializer.serializeI32(number)
        }
}
