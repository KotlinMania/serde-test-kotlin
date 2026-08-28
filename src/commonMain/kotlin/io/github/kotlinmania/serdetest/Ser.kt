// port-lint: source ser.rs
package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeError
import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.SerializeMap
import io.github.kotlinmania.serdecore.ser.SerializeSeq
import io.github.kotlinmania.serdecore.ser.SerializeStruct
import io.github.kotlinmania.serdecore.ser.SerializeStructVariant
import io.github.kotlinmania.serdecore.ser.SerializeTuple
import io.github.kotlinmania.serdecore.ser.SerializeTupleStruct
import io.github.kotlinmania.serdecore.ser.SerializeTupleVariant
import io.github.kotlinmania.serdecore.ser.Serializer as SerdeSerializer

typealias Serializer = TokenSerializer


/** A serializer that ensures that a value serializes to a given list of tokens. */
class TokenSerializer private constructor(
    tokens: List<Token>,
) : SerdeSerializer<Unit> {
    companion object {
        fun new(tokens: List<Token>): TokenSerializer = TokenSerializer(tokens)
    }

    private val tokens = tokens.toMutableList()

    fun remaining(): Int = tokens.size

    internal fun nextToken(): Token? = tokens.removeFirstOrNull()

    internal fun peekToken(): Token? = tokens.firstOrNull()

    internal fun expect(actual: Token): SerdeResult<Unit> {
        val expected = nextToken()
        return when {
            expected == actual -> SerdeResult.success(Unit)
            expected != null ->
                SerdeResult.failure(
                    SerdeError.custom("expected Token::$expected but serialized as $actual"),
                )
            else ->
                SerdeResult.failure(
                    SerdeError.custom("expected end of tokens, but $actual was serialized"),
                )
        }
    }

    override fun serializeBool(v: Boolean): SerdeResult<Unit> = expect(Token.Bool(v))

    override fun serializeI8(v: Byte): SerdeResult<Unit> = expect(Token.I8(v))

    override fun serializeI16(v: Short): SerdeResult<Unit> = expect(Token.I16(v))

    override fun serializeI32(v: Int): SerdeResult<Unit> = expect(Token.I32(v))

    override fun serializeI64(v: Long): SerdeResult<Unit> = expect(Token.I64(v))

    override fun serializeU8(v: UByte): SerdeResult<Unit> = expect(Token.U8(v))

    override fun serializeU16(v: UShort): SerdeResult<Unit> = expect(Token.U16(v))

    override fun serializeU32(v: UInt): SerdeResult<Unit> = expect(Token.U32(v))

    override fun serializeU64(v: ULong): SerdeResult<Unit> = expect(Token.U64(v))

    override fun serializeF32(v: Float): SerdeResult<Unit> = expect(Token.F32(v))

    override fun serializeF64(v: Double): SerdeResult<Unit> = expect(Token.F64(v))

    override fun serializeChar(v: Char): SerdeResult<Unit> = expect(Token.CharValue(v))

    override fun serializeStr(v: String): SerdeResult<Unit> =
        when (peekToken()) {
            is Token.BorrowedStr -> expect(Token.BorrowedStr(v))
            is Token.StringValue -> expect(Token.StringValue(v))
            else -> expect(Token.Str(v))
        }

    override fun serializeBytes(v: ByteArray): SerdeResult<Unit> =
        when (peekToken()) {
            is Token.BorrowedBytes -> expect(Token.BorrowedBytes(v))
            is Token.ByteBuf -> expect(Token.ByteBuf(v))
            else -> expect(Token.Bytes(v))
        }

    override fun serializeUnit(): SerdeResult<Unit> = expect(Token.UnitValue)

    override fun serializeUnitStruct(name: String): SerdeResult<Unit> = expect(Token.UnitStruct(name))

    override fun serializeUnitVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
    ): SerdeResult<Unit> {
        if (peekToken() == Token.Enum(name)) {
            nextToken()
            return expect(Token.Str(variant)).flatMap { expect(Token.UnitValue) }
        }
        return expect(Token.UnitVariant(name, variant))
    }

    override fun <T : Serialize> serializeNewtypeStruct(
        name: String,
        value: T,
    ): SerdeResult<Unit> = expect(Token.NewtypeStruct(name)).flatMap { value.serialize(this) }

    override fun <T : Serialize> serializeNewtypeVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        value: T,
    ): SerdeResult<Unit> {
        val header =
            if (peekToken() == Token.Enum(name)) {
                nextToken()
                expect(Token.Str(variant))
            } else {
                expect(Token.NewtypeVariant(name, variant))
            }
        return header.flatMap { value.serialize(this) }
    }

    override fun serializeNone(): SerdeResult<Unit> = expect(Token.None)

    override fun <T : Serialize> serializeSome(value: T): SerdeResult<Unit> =
        expect(Token.Some).flatMap { value.serialize(this) }

    override fun serializeSeq(len: Int?): SerdeResult<SerializeSeq<Unit>> =
        expect(Token.Seq(len)).map { SequenceState(this, Token.SeqEnd) }

    override fun serializeTuple(len: Int): SerdeResult<SerializeTuple<Unit>> =
        expect(Token.Tuple(len)).map { TupleState(this) }

    override fun serializeTupleStruct(
        name: String,
        len: Int,
    ): SerdeResult<SerializeTupleStruct<Unit>> =
        expect(Token.TupleStruct(name, len)).map { TupleStructState(this) }

    override fun serializeTupleVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        len: Int,
    ): SerdeResult<SerializeTupleVariant<Unit>> {
        if (peekToken() == Token.Enum(name)) {
            nextToken()
            return expect(Token.Str(variant))
                .flatMap { expect(Token.Seq(len)) }
                .map { TupleVariantState(this, Token.SeqEnd) }
        }
        return expect(Token.TupleVariant(name, variant, len))
            .map { TupleVariantState(this, Token.TupleVariantEnd) }
    }

    override fun serializeMap(len: Int?): SerdeResult<SerializeMap<Unit>> =
        expect(Token.Map(len)).map { MapState(this) }

    override fun serializeStruct(
        name: String,
        len: Int,
    ): SerdeResult<SerializeStruct<Unit>> =
        expect(Token.Struct(name, len)).map { StructState(this) }

    override fun serializeStructVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        len: Int,
    ): SerdeResult<SerializeStructVariant<Unit>> {
        if (peekToken() == Token.Enum(name)) {
            nextToken()
            return expect(Token.Str(variant))
                .flatMap { expect(Token.Map(len)) }
                .map { StructVariantState(this, Token.MapEnd) }
        }
        return expect(Token.StructVariant(name, variant, len))
            .map { StructVariantState(this, Token.StructVariantEnd) }
    }

    override fun isHumanReadable(): Boolean =
        error(
            "Types which have different human-readable and compact representations " +
                "must explicitly mark their test cases with Configure",
        )
}

private class SequenceState(
    private val serializer: TokenSerializer,
    private val end: Token,
) : SerializeSeq<Unit> {
    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> = value.serialize(serializer)

    override fun end(): SerdeResult<Unit> = serializer.expect(end)
}

private class TupleState(
    private val serializer: TokenSerializer,
) : SerializeTuple<Unit> {
    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> = value.serialize(serializer)

    override fun end(): SerdeResult<Unit> = serializer.expect(Token.TupleEnd)
}

private class TupleStructState(
    private val serializer: TokenSerializer,
) : SerializeTupleStruct<Unit> {
    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> = value.serialize(serializer)

    override fun end(): SerdeResult<Unit> = serializer.expect(Token.TupleStructEnd)
}

private class TupleVariantState(
    private val serializer: TokenSerializer,
    private val end: Token,
) : SerializeTupleVariant<Unit> {
    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> = value.serialize(serializer)

    override fun end(): SerdeResult<Unit> = serializer.expect(end)
}

private class MapState(
    private val serializer: TokenSerializer,
) : SerializeMap<Unit> {
    override fun <T : Serialize> serializeKey(key: T): SerdeResult<Unit> = key.serialize(serializer)

    override fun <T : Serialize> serializeValue(value: T): SerdeResult<Unit> = value.serialize(serializer)

    override fun end(): SerdeResult<Unit> = serializer.expect(Token.MapEnd)
}

private class StructState(
    private val serializer: TokenSerializer,
) : SerializeStruct<Unit> {
    override fun <T : Serialize> serializeField(
        key: String,
        value: T,
    ): SerdeResult<Unit> = serializer.serializeStr(key).flatMap { value.serialize(serializer) }

    override fun end(): SerdeResult<Unit> = serializer.expect(Token.StructEnd)
}

private class StructVariantState(
    private val serializer: TokenSerializer,
    private val end: Token,
) : SerializeStructVariant<Unit> {
    override fun <T : Serialize> serializeField(
        key: String,
        value: T,
    ): SerdeResult<Unit> = serializer.serializeStr(key).flatMap { value.serialize(serializer) }

    override fun end(): SerdeResult<Unit> = serializer.expect(end)
}
