// port-lint: source assert.rs
package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.ser.Serialize

/**
 * Runs both [assertSerTokens] and [assertDeTokens].
 */
fun <T> assertTokens(
    value: T,
    deserialize: Deserialize<T>,
    tokens: List<Token>,
) where T : Serialize {
    assertSerTokens(value, tokens)
    assertDeTokens(value, deserialize, tokens)
}

/**
 * Asserts that [value] serializes to the given [tokens].
 */
fun assertSerTokens(
    value: Serialize,
    tokens: List<Token>,
) {
    val serializer = TokenSerializer.new(tokens)
    when (val result = value.serialize(serializer)) {
        is SerdeResult.Success -> Unit
        is SerdeResult.Failure -> error("value failed to serialize: ${result.error}")
    }
    assertNoRemainingTokens(serializer.remaining())
}

/**
 * Asserts that [value] serializes to the given [tokens], and then yields [error].
 */
fun assertSerTokensError(
    value: Serialize,
    tokens: List<Token>,
    error: String,
) {
    val serializer = TokenSerializer.new(tokens)
    when (val result = value.serialize(serializer)) {
        is SerdeResult.Success -> error("value serialized successfully")
        is SerdeResult.Failure -> {
            if (result.error.message != error) {
                error("expected error <$error>, actual <${result.error.message}>")
            }
        }
    }
    assertNoRemainingTokens(serializer.remaining())
}

/**
 * Asserts that the given [tokens] deserialize into [value].
 */
fun <T> assertDeTokens(
    value: T,
    deserialize: Deserialize<T>,
    tokens: List<Token>,
) {
    val deserializer = Deserializer(tokens)
    var deserializedValue =
        when (val result = deserialize.deserialize(deserializer)) {
            is SerdeResult.Success -> result.value
            is SerdeResult.Failure -> error("tokens failed to deserialize: ${result.error}")
        }
    if (deserializedValue != value) {
        error("deserialized value <$deserializedValue> did not equal expected value <$value>")
    }
    assertNoRemainingTokens(deserializer.remaining())

    val inPlaceDeserializer = Deserializer(tokens)
    when (
        val result =
            deserialize.deserializeInPlace(inPlaceDeserializer) {
                deserializedValue = it
            }
    ) {
        is SerdeResult.Success -> Unit
        is SerdeResult.Failure -> error("tokens failed to deserialize in place: ${result.error}")
    }
    if (deserializedValue != value) {
        error("in-place deserialized value <$deserializedValue> did not equal expected value <$value>")
    }
    assertNoRemainingTokens(inPlaceDeserializer.remaining())
}

/**
 * Asserts that the given [tokens] yield [error] when deserializing.
 */
fun <T> assertDeTokensError(
    deserialize: Deserialize<T>,
    tokens: List<Token>,
    error: String,
) {
    val deserializer = Deserializer(tokens)
    when (val result = deserialize.deserialize(deserializer)) {
        is SerdeResult.Success -> error("tokens deserialized successfully")
        is SerdeResult.Failure -> {
            if (result.error.message != error) {
                error("expected error <$error>, actual <${result.error.message}>")
            }
        }
    }

    deserializer.nextTokenOpt()
    assertNoRemainingTokens(deserializer.remaining())
}

private fun assertNoRemainingTokens(remaining: Int) {
    if (remaining > 0) error("$remaining remaining tokens")
}
