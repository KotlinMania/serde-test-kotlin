// port-lint: source de.rs
package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.DeserializeSeed
import io.github.kotlinmania.serdecore.de.EnumAccess
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.VariantAccess
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.de.value.MapAccessDeserializer
import io.github.kotlinmania.serdecore.de.value.SeqAccessDeserializer
import io.github.kotlinmania.serdecore.de.Deserializer as SerdeDeserializer

/** A deserializer that drives a value from a given list of tokens. */
internal class Deserializer(
    tokens: List<Token>,
) : SerdeDeserializer {
    companion object {
        fun new(tokens: List<Token>): Deserializer = Deserializer(tokens)
    }

    private val tokens = tokens.toMutableList()

    fun remaining(): Int = tokens.size

    fun nextTokenOpt(): Token? = tokens.removeFirstOrNull()

    private fun peekTokenOpt(): Token? = tokens.firstOrNull()

    private fun peekToken(): SerdeResult<Token> =
        peekTokenOpt()?.let { SerdeResult.success(it) } ?: endOfTokens()

    private fun nextToken(): SerdeResult<Token> =
        nextTokenOpt()?.let { SerdeResult.success(it) } ?: endOfTokens()

    private fun assertNextToken(expected: Token): SerdeResult<Unit> {
        val token = nextTokenOpt()
        return when {
            token == expected -> SerdeResult.success(Unit)
            token != null ->
                failure("expected Token::$token but deserialization wants Token::$expected")
            else -> failure("end of tokens but deserialization wants Token::$expected")
        }
    }

    private fun <V> visitSeq(
        len: Int?,
        end: Token,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        visitor.visitSeq(DeserializerSeqAccess(this, len, end)).flatMap { value ->
            assertNextToken(end).map { value }
        }

    private fun <V> visitMap(
        len: Int?,
        end: Token,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        visitor.visitMap(DeserializerMapAccess(this, len, end)).flatMap { value ->
            assertNextToken(end).map { value }
        }

    override fun <V> deserializeAny(visitor: Visitor<V>): SerdeResult<V> =
        nextToken().flatMap { token ->
            when (token) {
                is Token.Bool -> visitor.visitBool(token.value)
                is Token.I8 -> visitor.visitI8(token.value)
                is Token.I16 -> visitor.visitI16(token.value)
                is Token.I32 -> visitor.visitI32(token.value)
                is Token.I64 -> visitor.visitI64(token.value)
                is Token.U8 -> visitor.visitU8(token.value)
                is Token.U16 -> visitor.visitU16(token.value)
                is Token.U32 -> visitor.visitU32(token.value)
                is Token.U64 -> visitor.visitU64(token.value)
                is Token.F32 -> visitor.visitF32(token.value)
                is Token.F64 -> visitor.visitF64(token.value)
                is Token.CharValue -> visitor.visitChar(token.value)
                is Token.Str -> visitor.visitStr(token.value)
                is Token.BorrowedStr -> visitor.visitBorrowedStr(token.value)
                is Token.StringValue -> visitor.visitString(token.value)
                is Token.Bytes -> visitor.visitBytes(token.value)
                is Token.BorrowedBytes -> visitor.visitBorrowedBytes(token.value)
                is Token.ByteBuf -> visitor.visitByteBuf(token.value)
                Token.None -> visitor.visitNone()
                Token.Some -> visitor.visitSome(this)
                Token.UnitValue,
                is Token.UnitStruct,
                -> visitor.visitUnit()
                is Token.NewtypeStruct -> visitor.visitNewtypeStruct(this)
                is Token.Seq -> visitSeq(token.len, Token.SeqEnd, visitor)
                is Token.Tuple -> visitSeq(token.len, Token.TupleEnd, visitor)
                is Token.TupleStruct -> visitSeq(token.len, Token.TupleStructEnd, visitor)
                is Token.Map -> visitMap(token.len, Token.MapEnd, visitor)
                is Token.Struct -> visitMap(token.len, Token.StructEnd, visitor)
                is Token.Enum -> deserializeAnyEnum(visitor)
                is Token.UnitVariant -> visitor.visitStr(token.variant)
                is Token.NewtypeVariant ->
                    visitor.visitMap(EnumMapAccess(this, Token.Str(token.variant), EnumFormat.Any))
                is Token.TupleVariant ->
                    visitor.visitMap(EnumMapAccess(this, Token.Str(token.variant), EnumFormat.Seq))
                is Token.StructVariant ->
                    visitor.visitMap(EnumMapAccess(this, Token.Str(token.variant), EnumFormat.Map))
                Token.SeqEnd,
                Token.TupleEnd,
                Token.TupleStructEnd,
                Token.MapEnd,
                Token.StructEnd,
                Token.TupleVariantEnd,
                Token.StructVariantEnd,
                -> unexpected(token)
            }
        }

    override fun <V> deserializeBool(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI8(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI16(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI32(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI64(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI128(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU8(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU16(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU32(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU64(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU128(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeF32(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeF64(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeChar(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeStr(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeString(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeBytes(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeByteBuf(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeUnit(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeSeq(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeMap(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeIdentifier(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeIgnoredAny(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    private fun <V> deserializeAnyEnum(visitor: Visitor<V>): SerdeResult<V> =
        nextToken().flatMap { variant ->
            val next = peekTokenOpt()
            if (next == Token.UnitValue) {
                nextTokenOpt()
                when (variant) {
                    is Token.Str -> visitor.visitStr(variant.value)
                    is Token.BorrowedStr -> visitor.visitBorrowedStr(variant.value)
                    is Token.StringValue -> visitor.visitString(variant.value)
                    is Token.Bytes -> visitor.visitBytes(variant.value)
                    is Token.BorrowedBytes -> visitor.visitBorrowedBytes(variant.value)
                    is Token.ByteBuf -> visitor.visitByteBuf(variant.value)
                    is Token.U8 -> visitor.visitU8(variant.value)
                    is Token.U16 -> visitor.visitU16(variant.value)
                    is Token.U32 -> visitor.visitU32(variant.value)
                    is Token.U64 -> visitor.visitU64(variant.value)
                    else -> unexpected(variant)
                }
            } else {
                visitor.visitMap(EnumMapAccess(this, variant, EnumFormat.Any))
            }
        }

    override fun <V> deserializeOption(visitor: Visitor<V>): SerdeResult<V> =
        peekToken().flatMap { token ->
            when (token) {
                Token.UnitValue,
                Token.None,
                -> {
                    nextTokenOpt()
                    visitor.visitNone()
                }
                Token.Some -> {
                    nextTokenOpt()
                    visitor.visitSome(this)
                }
                else -> deserializeAny(visitor)
            }
        }

    override fun <V> deserializeEnum(
        name: String,
        variants: List<String>,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        peekToken().flatMap { token ->
            when (token) {
                is Token.Enum -> {
                    if (token.name != name) {
                        unexpected(token)
                    } else {
                        nextTokenOpt()
                        visitor.visitEnum(DeserializerEnumAccess(this))
                    }
                }
                is Token.UnitVariant -> visitNamedEnum(name, token.name, visitor)
                is Token.NewtypeVariant -> visitNamedEnum(name, token.name, visitor)
                is Token.TupleVariant -> visitNamedEnum(name, token.name, visitor)
                is Token.StructVariant -> visitNamedEnum(name, token.name, visitor)
                else -> deserializeAny(visitor)
            }
        }

    private fun <V> visitNamedEnum(
        expectedName: String,
        actualName: String,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        if (expectedName == actualName) {
            visitor.visitEnum(DeserializerEnumAccess(this))
        } else {
            unexpected(peekTokenOpt() ?: return endOfTokens())
        }

    override fun <V> deserializeUnitStruct(
        name: String,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        when (peekTokenOpt()) {
            is Token.UnitStruct -> assertNextToken(Token.UnitStruct(name)).flatMap { visitor.visitUnit() }
            else -> deserializeAny(visitor)
        }

    override fun <V> deserializeNewtypeStruct(
        name: String,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        when (peekTokenOpt()) {
            is Token.NewtypeStruct -> assertNextToken(Token.NewtypeStruct(name)).flatMap { visitor.visitNewtypeStruct(this) }
            else -> deserializeAny(visitor)
        }

    override fun <V> deserializeTuple(
        len: Int,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        when (val token = peekTokenOpt()) {
            Token.UnitValue,
            is Token.UnitStruct,
            -> {
                nextTokenOpt()
                visitor.visitUnit()
            }
            is Token.Seq -> {
                nextTokenOpt()
                visitSeq(len, Token.SeqEnd, visitor)
            }
            is Token.Tuple -> {
                nextTokenOpt()
                visitSeq(len, Token.TupleEnd, visitor)
            }
            is Token.TupleStruct -> {
                nextTokenOpt()
                visitSeq(len, Token.TupleStructEnd, visitor)
            }
            else -> deserializeAny(visitor)
        }

    override fun <V> deserializeTupleStruct(
        name: String,
        len: Int,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        when (val token = peekTokenOpt()) {
            Token.UnitValue -> {
                nextTokenOpt()
                visitor.visitUnit()
            }
            is Token.UnitStruct -> assertNextToken(Token.UnitStruct(name)).flatMap { visitor.visitUnit() }
            is Token.Seq -> {
                nextTokenOpt()
                visitSeq(len, Token.SeqEnd, visitor)
            }
            is Token.Tuple -> {
                nextTokenOpt()
                visitSeq(len, Token.TupleEnd, visitor)
            }
            is Token.TupleStruct ->
                assertNextToken(Token.TupleStruct(name, token.len)).flatMap {
                    visitSeq(len, Token.TupleStructEnd, visitor)
                }
            else -> deserializeAny(visitor)
        }

    override fun <V> deserializeStruct(
        name: String,
        fields: List<String>,
        visitor: Visitor<V>,
    ): SerdeResult<V> =
        when (val token = peekTokenOpt()) {
            is Token.Struct ->
                assertNextToken(Token.Struct(name, token.len)).flatMap {
                    visitMap(fields.size, Token.StructEnd, visitor)
                }
            is Token.Map -> {
                nextTokenOpt()
                visitMap(fields.size, Token.MapEnd, visitor)
            }
            else -> deserializeAny(visitor)
        }

    override fun isHumanReadable(): Boolean =
        error(
            "Types which have different human-readable and compact representations " +
                "must explicitly mark their test cases with Configure",
        )

    private class DeserializerSeqAccess(
        private val deserializer: Deserializer,
        private var len: Int?,
        private val end: Token,
    ) : SeqAccess {
        override fun <T> nextElementSeed(seed: DeserializeSeed<T>): SerdeResult<T?> {
            if (deserializer.peekTokenOpt() == end) return SerdeResult.success(null)
            len = len?.let { (it - 1).coerceAtLeast(0) }
            return seed.deserialize(deserializer).map<T?> { it }
        }

        override fun sizeHint(): Int? = len
    }

    private class DeserializerMapAccess(
        private val deserializer: Deserializer,
        private var len: Int?,
        private val end: Token,
    ) : MapAccess {
        override fun <K> nextKeySeed(seed: DeserializeSeed<K>): SerdeResult<K?> {
            if (deserializer.peekTokenOpt() == end) return SerdeResult.success(null)
            len = len?.let { (it - 1).coerceAtLeast(0) }
            return seed.deserialize(deserializer).map<K?> { it }
        }

        override fun <V> nextValueSeed(seed: DeserializeSeed<V>): SerdeResult<V> = seed.deserialize(deserializer)

        override fun sizeHint(): Int? = len
    }

    private class DeserializerEnumAccess(
        private val deserializer: Deserializer,
    ) : EnumAccess,
        VariantAccess {
        override fun <V> variantSeed(seed: DeserializeSeed<V>): SerdeResult<Pair<V, VariantAccess>> {
            val token = deserializer.peekTokenOpt()
            val value =
                when (token) {
                    is Token.UnitVariant -> seed.deserialize(TokenDeserializer(Token.Str(token.variant)))
                    is Token.NewtypeVariant -> seed.deserialize(TokenDeserializer(Token.Str(token.variant)))
                    is Token.TupleVariant -> seed.deserialize(TokenDeserializer(Token.Str(token.variant)))
                    is Token.StructVariant -> seed.deserialize(TokenDeserializer(Token.Str(token.variant)))
                    else -> seed.deserialize(deserializer)
                }
            return value.map { it to this }
        }

        override fun unitVariant(): SerdeResult<Unit> =
            when (deserializer.peekTokenOpt()) {
                is Token.UnitVariant,
                Token.UnitValue,
                -> {
                    deserializer.nextTokenOpt()
                    SerdeResult.success(Unit)
                }
                else -> deserializer.nextToken().flatMap(::unexpected)
            }

        override fun <T> newtypeVariantSeed(seed: DeserializeSeed<T>): SerdeResult<T> {
            if (deserializer.peekTokenOpt() is Token.NewtypeVariant) deserializer.nextTokenOpt()
            return seed.deserialize(deserializer)
        }

        override fun <V> tupleVariant(
            len: Int,
            visitor: Visitor<V>,
        ): SerdeResult<V> =
            when (val token = deserializer.peekTokenOpt()) {
                is Token.TupleVariant -> {
                    deserializer.nextTokenOpt()
                    if (token.len == len) {
                        deserializer.visitSeq(len, Token.TupleVariantEnd, visitor)
                    } else {
                        unexpected(token)
                    }
                }
                is Token.Seq -> {
                    deserializer.nextTokenOpt()
                    if (token.len == len) {
                        deserializer.visitSeq(len, Token.SeqEnd, visitor)
                    } else {
                        unexpected(token)
                    }
                }
                else -> deserializer.deserializeAny(visitor)
            }

        override fun <V> structVariant(
            fields: List<String>,
            visitor: Visitor<V>,
        ): SerdeResult<V> =
            when (val token = deserializer.peekTokenOpt()) {
                is Token.StructVariant -> {
                    deserializer.nextTokenOpt()
                    if (token.len == fields.size) {
                        deserializer.visitMap(fields.size, Token.StructVariantEnd, visitor)
                    } else {
                        unexpected(token)
                    }
                }
                is Token.Map -> {
                    deserializer.nextTokenOpt()
                    if (token.len == fields.size) {
                        deserializer.visitMap(fields.size, Token.MapEnd, visitor)
                    } else {
                        unexpected(token)
                    }
                }
                else -> deserializer.deserializeAny(visitor)
            }
    }

    private enum class EnumFormat {
        Seq,
        Map,
        Any,
    }

    private class EnumMapAccess(
        private val deserializer: Deserializer,
        private var variant: Token?,
        private val format: EnumFormat,
    ) : MapAccess {
        override fun <K> nextKeySeed(seed: DeserializeSeed<K>): SerdeResult<K?> {
            val token = variant ?: return SerdeResult.success(null)
            variant = null
            return seed.deserialize(TokenDeserializer(token)).map<K?> { it }
        }

        override fun <V> nextValueSeed(seed: DeserializeSeed<V>): SerdeResult<V> =
            when (format) {
                EnumFormat.Seq -> {
                    val access = DeserializerSeqAccess(deserializer, null, Token.TupleVariantEnd)
                    seed.deserialize(SeqAccessDeserializer.new(access)).flatMap { value ->
                        deserializer.assertNextToken(Token.TupleVariantEnd).map { value }
                    }
                }
                EnumFormat.Map -> {
                    val access = DeserializerMapAccess(deserializer, null, Token.StructVariantEnd)
                    seed.deserialize(MapAccessDeserializer.new(access)).flatMap { value ->
                        deserializer.assertNextToken(Token.StructVariantEnd).map { value }
                    }
                }
                EnumFormat.Any -> seed.deserialize(deserializer)
            }
    }
}

private abstract class DeserializeAnyForwarder : SerdeDeserializer {
    abstract override fun <V> deserializeAny(visitor: Visitor<V>): SerdeResult<V>

    override fun <V> deserializeBool(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI8(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI16(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI32(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI64(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeI128(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU8(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU16(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU32(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU64(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeU128(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeF32(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeF64(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeChar(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeStr(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeString(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeBytes(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeByteBuf(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeOption(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeUnit(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeUnitStruct(name: String, visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeNewtypeStruct(name: String, visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeSeq(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeTuple(len: Int, visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeTupleStruct(name: String, len: Int, visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeMap(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeStruct(name: String, fields: List<String>, visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeEnum(name: String, variants: List<String>, visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeIdentifier(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)

    override fun <V> deserializeIgnoredAny(visitor: Visitor<V>): SerdeResult<V> = deserializeAny(visitor)
}

private class TokenDeserializer(
    private val token: Token,
) : DeserializeAnyForwarder() {
    override fun <V> deserializeAny(visitor: Visitor<V>): SerdeResult<V> =
        when (token) {
            is Token.Str -> visitor.visitStr(token.value)
            is Token.BorrowedStr -> visitor.visitBorrowedStr(token.value)
            is Token.StringValue -> visitor.visitString(token.value)
            is Token.Bytes -> visitor.visitBytes(token.value)
            is Token.BorrowedBytes -> visitor.visitBorrowedBytes(token.value)
            is Token.ByteBuf -> visitor.visitByteBuf(token.value)
            is Token.U8 -> visitor.visitU8(token.value)
            is Token.U16 -> visitor.visitU16(token.value)
            is Token.U32 -> visitor.visitU32(token.value)
            is Token.U64 -> visitor.visitU64(token.value)
            else -> unexpected(token)
        }
}

private fun <T> failure(message: String): SerdeResult<T> = SerdeResult.failure(SerdeError.custom(message))

private fun <T> unexpected(token: Token): SerdeResult<T> =
    failure("deserialization did not expect this token: $token")

private fun <T> endOfTokens(): SerdeResult<T> = failure("ran out of tokens to deserialize")
