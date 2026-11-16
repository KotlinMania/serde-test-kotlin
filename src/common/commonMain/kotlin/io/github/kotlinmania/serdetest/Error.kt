// port-lint: source error.rs
package io.github.kotlinmania.serdetest

/**
 * An error type for serialization and deserialization testing.
 *
 * Corresponds to Error in SerdeTest.
 */
data class Error(
    val msg: String,
) {
    /** Returns the underlying error message. */
    val message: String get() = msg

    /** Returns the description of the error. */
    fun description(): String = msg

    /** Checks equality with a string message. */
    fun eq(other: String): Boolean = msg == other

    /** Formats this error using the standard representation. */
    fun fmt(): String = msg

    override fun toString(): String = msg

    companion object {
        /**
         * Creates a custom error from a string message.
         */
        fun custom(msg: String): Error = Error(msg)

        /**
         * Creates a custom error from any displayable object.
         */
        fun custom(msg: Any): Error = Error(msg.toString())
    }
}
