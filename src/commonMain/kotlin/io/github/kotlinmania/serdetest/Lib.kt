// port-lint: source lib.rs
package io.github.kotlinmania.serdetest

/**
 * This package provides a convenient concise way to write unit tests for
 * implementations of `Serialize` and `Deserialize`.
 *
 * The `Serialize` impl for a value can be characterized by the sequence of
 * `Serializer` calls that are made in the course of serializing the value,
 * so this package provides a [Token] abstraction which corresponds roughly
 * to `Serializer` method calls. There is an [assertSerTokens] function to
 * test that a value serializes to a particular sequence of method calls, an
 * [assertDeTokens] function to test that a value can be deserialized from
 * a particular sequence of method calls, and an [assertTokens] function to
 * test both directions. There are also functions to test expected failure
 * conditions.
 */
public object SerdeTestInfo {
    /** Upstream serde_test crate version this port is transliterated from. */
    public const val CRATE_VERSION: String = "1.0.176"
}
