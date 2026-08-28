// port-lint: source token.rs
package io.github.kotlinmania.serdetest

/**
 * An abstraction of the sequence of Serializer / Deserializer calls that are
 * made in the course of serializing or deserializing a value.
 */
sealed interface Token {
    /** Formats this token using its standard representation. */
    fun fmt(): kotlin.String = toString()

    /** A serialized boolean. */
    data class Bool(
        val value: Boolean,
    ) : Token

    /** A serialized 8-bit signed integer. */
    data class I8(
        val value: Byte,
    ) : Token

    /** A serialized 16-bit signed integer. */
    data class I16(
        val value: Short,
    ) : Token

    /** A serialized 32-bit signed integer. */
    data class I32(
        val value: Int,
    ) : Token

    /** A serialized 64-bit signed integer. */
    data class I64(
        val value: Long,
    ) : Token

    /** A serialized 8-bit unsigned integer. */
    data class U8(
        val value: UByte,
    ) : Token

    /** A serialized 16-bit unsigned integer. */
    data class U16(
        val value: UShort,
    ) : Token

    /** A serialized 32-bit unsigned integer. */
    data class U32(
        val value: UInt,
    ) : Token

    /** A serialized 64-bit unsigned integer. */
    data class U64(
        val value: ULong,
    ) : Token

    /** A serialized 32-bit floating point number. */
    data class F32(
        val value: Float,
    ) : Token

    /** A serialized 64-bit floating point number. */
    data class F64(
        val value: Double,
    ) : Token

    /** A serialized character. */
    data class CharValue(
        val value: Char,
    ) : Token

    /** A serialized string slice. */
    data class Str(
        val value: kotlin.String,
    ) : Token

    /** A borrowed string slice. */
    data class BorrowedStr(
        val value: kotlin.String,
    ) : Token

    /** A serialized owned string. */
    data class StringValue(
        val value: kotlin.String,
    ) : Token

    /** A serialized byte array. */
    class Bytes(
        val value: ByteArray,
    ) : Token {
        override fun equals(other: Any?): Boolean = other is Bytes && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): kotlin.String = "Bytes(${value.contentToString()})"
    }

    /** A borrowed byte array. */
    class BorrowedBytes(
        val value: ByteArray,
    ) : Token {
        override fun equals(other: Any?): Boolean = other is BorrowedBytes && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): kotlin.String = "BorrowedBytes(${value.contentToString()})"
    }

    /** A serialized byte buffer. */
    class ByteBuf(
        val value: ByteArray,
    ) : Token {
        override fun equals(other: Any?): Boolean = other is ByteBuf && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): kotlin.String = "ByteBuf(${value.contentToString()})"
    }

    /** A serialized optional containing nothing (None / null). */
    data object None : Token

    /**
     * The header to a serialized optional containing a value (Some).
     * The tokens of the value follow after this header.
     */
    data object Some : Token

    /** A serialized unit / empty tuple. */
    data object UnitValue : Token

    /** A serialized unit struct of the given name. */
    data class UnitStruct(
        val name: kotlin.String,
    ) : Token

    /** A unit variant of an enum. */
    data class UnitVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
    ) : Token

    /**
     * The header to a serialized newtype struct of the given name.
     * After this header is the value contained in the newtype struct.
     */
    data class NewtypeStruct(
        val name: kotlin.String,
    ) : Token

    /**
     * The header to a newtype variant of an enum.
     * After this header is the value contained in the newtype variant.
     */
    data class NewtypeVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
    ) : Token

    /**
     * The header to a sequence.
     * After this header are the elements of the sequence, followed by [SeqEnd].
     */
    data class Seq(
        val len: Int?,
    ) : Token

    /** An indicator of the end of a sequence. */
    data object SeqEnd : Token

    /**
     * The header to a tuple.
     * After this header are the elements of the tuple, followed by [TupleEnd].
     */
    data class Tuple(
        val len: Int,
    ) : Token

    /** An indicator of the end of a tuple. */
    data object TupleEnd : Token

    /**
     * The header to a tuple struct.
     * After this header are the fields of the tuple struct, followed by [TupleStructEnd].
     */
    data class TupleStruct(
        val name: kotlin.String,
        val len: Int,
    ) : Token

    /** An indicator of the end of a tuple struct. */
    data object TupleStructEnd : Token

    /**
     * The header to a tuple variant of an enum.
     * After this header are the fields of the tuple variant, followed by [TupleVariantEnd].
     */
    data class TupleVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
        val len: Int,
    ) : Token

    /** An indicator of the end of a tuple variant. */
    data object TupleVariantEnd : Token

    /**
     * The header to a map.
     * After this header are the entries of the map, followed by [MapEnd].
     */
    data class Map(
        val len: Int?,
    ) : Token

    /** An indicator of the end of a map. */
    data object MapEnd : Token

    /**
     * The header of a struct.
     * After this header are the fields of the struct, followed by [StructEnd].
     */
    data class Struct(
        val name: kotlin.String,
        val len: Int,
    ) : Token

    /** An indicator of the end of a struct. */
    data object StructEnd : Token

    /**
     * The header of a struct variant of an enum.
     * After this header are the fields of the struct variant, followed by [StructVariantEnd].
     */
    data class StructVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
        val len: Int,
    ) : Token

    /** An indicator of the end of a struct variant. */
    data object StructVariantEnd : Token

    /** The header to an enum of the given name. */
    data class Enum(
        val name: kotlin.String,
    ) : Token
}
