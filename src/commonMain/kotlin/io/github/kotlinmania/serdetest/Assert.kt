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
    val ser = Serializer.new(tokens)
    when (val result = value.serialize(ser)) {
        is SerdeResult.Success -> Unit
        is SerdeResult.Failure -> error("value failed to serialize: ${result.error}")
    }

    if (ser.remaining() > 0) {
        error("${ser.remaining()} remaining tokens")
    }
}

/**
 * Asserts that [value] serializes to the given [tokens], and then yields [error].
 */
fun assertSerTokensError(
    value: Serialize,
    tokens: List<Token>,
    error: String,
) {
    val ser = Serializer.new(tokens)
    when (val result = value.serialize(ser)) {
        is SerdeResult.Success -> {
            error("value serialized successfully")
        }

        is SerdeResult.Failure -> {
            if (result.error.message != error) {
                error("expected error <$error>, actual <${result.error.message}>")
            }
        }
    }

    if (ser.remaining() > 0) {
        error("${ser.remaining()} remaining tokens")
    }
}

/**
 * Asserts that the given [tokens] deserialize into [value].
 */
fun <T> assertDeTokens(
    value: T,
    deserialize: Deserialize<T>,
    tokens: List<Token>,
) {
    val de = Deserializer(tokens)
    var deserializedVal =
        when (val result = deserialize.deserialize(de)) {
            is SerdeResult.Success -> {
                if (result.value != value) {
                    error("deserialized value <${result.value}> did not equal expected value <$value>")
                }
                result.value
            }

            is SerdeResult.Failure -> {
                error("tokens failed to deserialize: ${result.error}")
            }
        }
    if (de.remaining() > 0) {
        error("${de.remaining()} remaining tokens")
    }

    val deInPlace = Deserializer(tokens)
    when (
        val result =
            deserialize.deserializeInPlace(deInPlace) {
                deserializedVal = it
            }
    ) {
        is SerdeResult.Success -> {
            if (deserializedVal != value) {
                error("in-place deserialized value <$deserializedVal> did not equal expected value <$value>")
            }
        }

        is SerdeResult.Failure -> {
            error("tokens failed to deserialize in place: ${result.error}")
        }
    }
    if (deInPlace.remaining() > 0) {
        error("${deInPlace.remaining()} remaining tokens")
    }
}

/**
 * Asserts that the given [tokens] yield [error] when deserializing.
 */
fun <T> assertDeTokensError(
    deserialize: Deserialize<T>,
    tokens: List<Token>,
    error: String,
) {
    val de = Deserializer(tokens)
    when (val result = deserialize.deserialize(de)) {
        is SerdeResult.Success -> {
            error("tokens deserialized successfully")
        }

        is SerdeResult.Failure -> {
            if (result.error.message != error) {
                error("expected error <$error>, actual <${result.error.message}>")
            }
        }
    }

    de.nextTokenOpt()

    if (de.remaining() > 0) {
        error("${de.remaining()} remaining tokens")
    }
}
