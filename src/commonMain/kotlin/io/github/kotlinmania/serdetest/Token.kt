// port-lint: source token.rs
package io.github.kotlinmania.serdetest

sealed interface Token {
    fun fmt(): kotlin.String = toString()

    data class Bool(
        val value: Boolean,
    ) : Token

    data class I8(
        val value: Byte,
    ) : Token

    data class I16(
        val value: Short,
    ) : Token

    data class I32(
        val value: Int,
    ) : Token

    data class I64(
        val value: Long,
    ) : Token

    data class U8(
        val value: UByte,
    ) : Token

    data class U16(
        val value: UShort,
    ) : Token

    data class U32(
        val value: UInt,
    ) : Token

    data class U64(
        val value: ULong,
    ) : Token

    data class F32(
        val value: Float,
    ) : Token

    data class F64(
        val value: Double,
    ) : Token

    data class CharValue(
        val value: Char,
    ) : Token

    data class Str(
        val value: kotlin.String,
    ) : Token

    data class BorrowedStr(
        val value: kotlin.String,
    ) : Token

    data class StringValue(
        val value: kotlin.String,
    ) : Token

    class Bytes(
        val value: ByteArray,
    ) : Token {
        override fun equals(other: Any?): Boolean = other is Bytes && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): kotlin.String = "Bytes(${value.contentToString()})"
    }

    class BorrowedBytes(
        val value: ByteArray,
    ) : Token {
        override fun equals(other: Any?): Boolean = other is BorrowedBytes && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): kotlin.String = "BorrowedBytes(${value.contentToString()})"
    }

    class ByteBuf(
        val value: ByteArray,
    ) : Token {
        override fun equals(other: Any?): Boolean = other is ByteBuf && value.contentEquals(other.value)

        override fun hashCode(): Int = value.contentHashCode()

        override fun toString(): kotlin.String = "ByteBuf(${value.contentToString()})"
    }

    data object None : Token

    data object Some : Token

    data object UnitValue : Token

    data class UnitStruct(
        val name: kotlin.String,
    ) : Token

    data class UnitVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
    ) : Token

    data class NewtypeStruct(
        val name: kotlin.String,
    ) : Token

    data class NewtypeVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
    ) : Token

    data class Seq(
        val len: Int?,
    ) : Token

    data object SeqEnd : Token

    data class Tuple(
        val len: Int,
    ) : Token

    data object TupleEnd : Token

    data class TupleStruct(
        val name: kotlin.String,
        val len: Int,
    ) : Token

    data object TupleStructEnd : Token

    data class TupleVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
        val len: Int,
    ) : Token

    data object TupleVariantEnd : Token

    data class Map(
        val len: Int?,
    ) : Token

    data object MapEnd : Token

    data class Struct(
        val name: kotlin.String,
        val len: Int,
    ) : Token

    data object StructEnd : Token

    data class StructVariant(
        val name: kotlin.String,
        val variant: kotlin.String,
        val len: Int,
    ) : Token

    data object StructVariantEnd : Token

    data class Enum(
        val name: kotlin.String,
    ) : Token
}
