// port-lint: source configure.rs
package io.github.kotlinmania.serdetest

import io.github.kotlinmania.serde.SerdeResult
import io.github.kotlinmania.serdecore.de.Deserialize
import io.github.kotlinmania.serdecore.de.DeserializeSeed
import io.github.kotlinmania.serdecore.de.Deserializer
import io.github.kotlinmania.serdecore.de.EnumAccess
import io.github.kotlinmania.serdecore.de.MapAccess
import io.github.kotlinmania.serdecore.de.SeqAccess
import io.github.kotlinmania.serdecore.de.VariantAccess
import io.github.kotlinmania.serdecore.de.Visitor
import io.github.kotlinmania.serdecore.ser.Serialize
import io.github.kotlinmania.serdecore.ser.SerializeMap
import io.github.kotlinmania.serdecore.ser.SerializeSeq
import io.github.kotlinmania.serdecore.ser.SerializeStruct
import io.github.kotlinmania.serdecore.ser.SerializeStructVariant
import io.github.kotlinmania.serdecore.ser.SerializeTuple
import io.github.kotlinmania.serdecore.ser.SerializeTupleStruct
import io.github.kotlinmania.serdecore.ser.SerializeTupleVariant
import io.github.kotlinmania.serdecore.ser.Serializer

/**
 * Trait to determine whether a value is represented in human-readable or
 * compact form.
 */
interface Configure<T : Serialize> {
    /** Marks this value as using human-readable format. */
    fun readable(): Readable<T>

    /** Marks this value as using compact format. */
    fun compact(): Compact<T>
}

// generic by design: callers choose the serializable value type.
data class Readable<T : Serialize>(
    val value: T,
) : Serialize, Configure<T> {
    override fun readable(): Readable<T> = this

    override fun compact(): Compact<T> = Compact(value)

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        value.serialize(ConfiguredSerializer(serializer, true))
}

// generic by design: callers choose the serializable value type.
data class Compact<T : Serialize>(
    val value: T,
) : Serialize, Configure<T> {
    override fun readable(): Readable<T> = Readable(value)

    override fun compact(): Compact<T> = this

    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        value.serialize(ConfiguredSerializer(serializer, false))
}

/** Marks this value as using a human-readable representation. */
fun <T : Serialize> T.readable(): Readable<T> = Readable(this)

/** Marks this value as using a compact representation. */
fun <T : Serialize> T.compact(): Compact<T> = Compact(this)

/** Marks this deserializer as using a human-readable representation. */
fun <T> Deserialize<T>.readable(): Deserialize<T> = ConfiguredDeserialize(this, true)

/** Marks this deserializer as using a compact representation. */
fun <T> Deserialize<T>.compact(): Deserialize<T> = ConfiguredDeserialize(this, false)

private data class ConfiguredValue<T : Serialize>(
    val value: T,
    val humanReadable: Boolean,
) : Serialize {
    override fun <Ok> serialize(serializer: Serializer<Ok>): SerdeResult<Ok> =
        value.serialize(ConfiguredSerializer(serializer, humanReadable))
}

private class ConfiguredSerializer<Ok>(
    private val delegate: Serializer<Ok>,
    private val humanReadable: Boolean,
) : Serializer<Ok> {
    override fun isHumanReadable(): Boolean = humanReadable

    override fun serializeBool(v: Boolean): SerdeResult<Ok> = delegate.serializeBool(v)

    override fun serializeI8(v: Byte): SerdeResult<Ok> = delegate.serializeI8(v)

    override fun serializeI16(v: Short): SerdeResult<Ok> = delegate.serializeI16(v)

    override fun serializeI32(v: Int): SerdeResult<Ok> = delegate.serializeI32(v)

    override fun serializeI64(v: Long): SerdeResult<Ok> = delegate.serializeI64(v)

    override fun serializeI128(value: String): SerdeResult<Ok> = delegate.serializeI128(value)

    override fun serializeU8(v: UByte): SerdeResult<Ok> = delegate.serializeU8(v)

    override fun serializeU16(v: UShort): SerdeResult<Ok> = delegate.serializeU16(v)

    override fun serializeU32(v: UInt): SerdeResult<Ok> = delegate.serializeU32(v)

    override fun serializeU64(v: ULong): SerdeResult<Ok> = delegate.serializeU64(v)

    override fun serializeU128(value: String): SerdeResult<Ok> = delegate.serializeU128(value)

    override fun serializeF32(v: Float): SerdeResult<Ok> = delegate.serializeF32(v)

    override fun serializeF64(v: Double): SerdeResult<Ok> = delegate.serializeF64(v)

    override fun serializeChar(v: Char): SerdeResult<Ok> = delegate.serializeChar(v)

    override fun serializeStr(v: String): SerdeResult<Ok> = delegate.serializeStr(v)

    override fun serializeBytes(v: ByteArray): SerdeResult<Ok> = delegate.serializeBytes(v)

    override fun serializeUnit(): SerdeResult<Ok> = delegate.serializeUnit()

    override fun serializeUnitStruct(name: String): SerdeResult<Ok> = delegate.serializeUnitStruct(name)

    override fun serializeUnitVariant(name: String, variantIndex: UInt, variant: String): SerdeResult<Ok> =
        delegate.serializeUnitVariant(name, variantIndex, variant)

    override fun serializeNone(): SerdeResult<Ok> = delegate.serializeNone()

    override fun <T : Serialize> serializeSome(value: T): SerdeResult<Ok> =
        delegate.serializeSome(ConfiguredValue(value, humanReadable))

    override fun <T : Serialize> serializeNewtypeStruct(name: String, value: T): SerdeResult<Ok> =
        delegate.serializeNewtypeStruct(name, ConfiguredValue(value, humanReadable))

    override fun <T : Serialize> serializeNewtypeVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        value: T,
    ): SerdeResult<Ok> =
        delegate.serializeNewtypeVariant(name, variantIndex, variant, ConfiguredValue(value, humanReadable))

    override fun serializeSeq(len: Int?): SerdeResult<SerializeSeq<Ok>> =
        delegate.serializeSeq(len).map { ConfiguredSeq(it, humanReadable) }

    override fun serializeTuple(len: Int): SerdeResult<SerializeTuple<Ok>> =
        delegate.serializeTuple(len).map { ConfiguredTuple(it, humanReadable) }

    override fun serializeTupleStruct(name: String, len: Int): SerdeResult<SerializeTupleStruct<Ok>> =
        delegate.serializeTupleStruct(name, len).map { ConfiguredTupleStruct(it, humanReadable) }

    override fun serializeTupleVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        len: Int,
    ): SerdeResult<SerializeTupleVariant<Ok>> =
        delegate
            .serializeTupleVariant(name, variantIndex, variant, len)
            .map { ConfiguredTupleVariant(it, humanReadable) }

    override fun serializeMap(len: Int?): SerdeResult<SerializeMap<Ok>> =
        delegate.serializeMap(len).map { ConfiguredMap(it, humanReadable) }

    override fun serializeStruct(name: String, len: Int): SerdeResult<SerializeStruct<Ok>> =
        delegate.serializeStruct(name, len).map { ConfiguredStruct(it, humanReadable) }

    override fun serializeStructVariant(
        name: String,
        variantIndex: UInt,
        variant: String,
        len: Int,
    ): SerdeResult<SerializeStructVariant<Ok>> =
        delegate
            .serializeStructVariant(name, variantIndex, variant, len)
            .map { ConfiguredStructVariant(it, humanReadable) }
}

private class ConfiguredSeq<Ok>(
    private val delegate: SerializeSeq<Ok>,
    private val humanReadable: Boolean,
) : SerializeSeq<Ok> {
    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> =
        delegate.serializeElement(ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredTuple<Ok>(
    private val delegate: SerializeTuple<Ok>,
    private val humanReadable: Boolean,
) : SerializeTuple<Ok> {
    override fun <T : Serialize> serializeElement(value: T): SerdeResult<Unit> =
        delegate.serializeElement(ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredTupleStruct<Ok>(
    private val delegate: SerializeTupleStruct<Ok>,
    private val humanReadable: Boolean,
) : SerializeTupleStruct<Ok> {
    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> =
        delegate.serializeField(ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredTupleVariant<Ok>(
    private val delegate: SerializeTupleVariant<Ok>,
    private val humanReadable: Boolean,
) : SerializeTupleVariant<Ok> {
    override fun <T : Serialize> serializeField(value: T): SerdeResult<Unit> =
        delegate.serializeField(ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredMap<Ok>(
    private val delegate: SerializeMap<Ok>,
    private val humanReadable: Boolean,
) : SerializeMap<Ok> {
    override fun <T : Serialize> serializeKey(key: T): SerdeResult<Unit> =
        delegate.serializeKey(ConfiguredValue(key, humanReadable))

    override fun <T : Serialize> serializeValue(value: T): SerdeResult<Unit> =
        delegate.serializeValue(ConfiguredValue(value, humanReadable))

    override fun <K : Serialize, V : Serialize> serializeEntry(key: K, value: V): SerdeResult<Unit> =
        delegate.serializeEntry(key, ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredStruct<Ok>(
    private val delegate: SerializeStruct<Ok>,
    private val humanReadable: Boolean,
) : SerializeStruct<Ok> {
    override fun <T : Serialize> serializeField(key: String, value: T): SerdeResult<Unit> =
        delegate.serializeField(key, ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredStructVariant<Ok>(
    private val delegate: SerializeStructVariant<Ok>,
    private val humanReadable: Boolean,
) : SerializeStructVariant<Ok> {
    override fun <T : Serialize> serializeField(key: String, value: T): SerdeResult<Unit> =
        delegate.serializeField(key, ConfiguredValue(value, humanReadable))

    override fun end(): SerdeResult<Ok> = delegate.end()
}

private class ConfiguredDeserialize<T>(
    private val delegate: Deserialize<T>,
    private val humanReadable: Boolean,
) : Deserialize<T> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T> =
        delegate.deserialize(ConfiguredDeserializer(deserializer, humanReadable))

    override fun <D : Deserializer> deserializeInPlace(
        deserializer: D,
        place: (T) -> Unit,
    ): SerdeResult<Unit> =
        delegate.deserializeInPlace(ConfiguredDeserializer(deserializer, humanReadable), place)
}

private class ConfiguredSeed<T>(
    private val delegate: DeserializeSeed<T>,
    private val humanReadable: Boolean,
) : DeserializeSeed<T> {
    override fun <D : Deserializer> deserialize(deserializer: D): SerdeResult<T> =
        delegate.deserialize(ConfiguredDeserializer(deserializer, humanReadable))
}

private class ConfiguredDeserializer(
    private val delegate: Deserializer,
    private val humanReadable: Boolean,
) : Deserializer {
    override fun isHumanReadable(): Boolean = humanReadable

    override fun <V> deserializeAny(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeAny(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeBool(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeBool(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeI8(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeI8(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeI16(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeI16(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeI32(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeI32(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeI64(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeI64(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeI128(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeI128(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeU8(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeU8(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeU16(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeU16(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeU32(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeU32(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeU64(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeU64(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeU128(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeU128(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeF32(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeF32(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeF64(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeF64(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeChar(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeChar(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeStr(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeStr(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeString(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeString(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeBytes(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeBytes(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeByteBuf(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeByteBuf(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeOption(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeOption(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeUnit(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeUnit(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeUnitStruct(name: String, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeUnitStruct(name, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeNewtypeStruct(name: String, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeNewtypeStruct(name, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeSeq(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeSeq(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeTuple(len: Int, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeTuple(len, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeTupleStruct(name: String, len: Int, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeTupleStruct(name, len, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeMap(visitor: Visitor<V>): SerdeResult<V> = delegate.deserializeMap(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeStruct(name: String, fields: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeStruct(name, fields, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeEnum(name: String, variants: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeEnum(name, variants, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeIdentifier(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeIdentifier(ConfiguredVisitor(visitor, humanReadable))

    override fun <V> deserializeIgnoredAny(visitor: Visitor<V>): SerdeResult<V> =
        delegate.deserializeIgnoredAny(ConfiguredVisitor(visitor, humanReadable))
}

private class ConfiguredVisitor<T>(
    private val delegate: Visitor<T>,
    private val humanReadable: Boolean,
) : Visitor<T> {
    override fun expecting(): String = delegate.expecting()

    override fun visitBool(v: Boolean): SerdeResult<T> = delegate.visitBool(v)

    override fun visitI8(v: Byte): SerdeResult<T> = delegate.visitI8(v)

    override fun visitI16(v: Short): SerdeResult<T> = delegate.visitI16(v)

    override fun visitI32(v: Int): SerdeResult<T> = delegate.visitI32(v)

    override fun visitI64(v: Long): SerdeResult<T> = delegate.visitI64(v)

    override fun visitI128(v: String): SerdeResult<T> = delegate.visitI128(v)

    override fun visitU8(v: UByte): SerdeResult<T> = delegate.visitU8(v)

    override fun visitU16(v: UShort): SerdeResult<T> = delegate.visitU16(v)

    override fun visitU32(v: UInt): SerdeResult<T> = delegate.visitU32(v)

    override fun visitU64(v: ULong): SerdeResult<T> = delegate.visitU64(v)

    override fun visitU128(v: String): SerdeResult<T> = delegate.visitU128(v)

    override fun visitF32(v: Float): SerdeResult<T> = delegate.visitF32(v)

    override fun visitF64(v: Double): SerdeResult<T> = delegate.visitF64(v)

    override fun visitChar(v: Char): SerdeResult<T> = delegate.visitChar(v)

    override fun visitStr(v: String): SerdeResult<T> = delegate.visitStr(v)

    override fun visitBorrowedStr(v: String): SerdeResult<T> = delegate.visitBorrowedStr(v)

    override fun visitString(v: String): SerdeResult<T> = delegate.visitString(v)

    override fun visitBytes(v: ByteArray): SerdeResult<T> = delegate.visitBytes(v)

    override fun visitBorrowedBytes(v: ByteArray): SerdeResult<T> = delegate.visitBorrowedBytes(v)

    override fun visitByteBuf(v: ByteArray): SerdeResult<T> = delegate.visitByteBuf(v)

    override fun visitNone(): SerdeResult<T> = delegate.visitNone()

    override fun visitUnit(): SerdeResult<T> = delegate.visitUnit()

    override fun <D : Deserializer> visitSome(deserializer: D): SerdeResult<T> =
        delegate.visitSome(ConfiguredDeserializer(deserializer, humanReadable))

    override fun <D : Deserializer> visitNewtypeStruct(deserializer: D): SerdeResult<T> =
        delegate.visitNewtypeStruct(ConfiguredDeserializer(deserializer, humanReadable))

    override fun <A : SeqAccess> visitSeq(access: A): SerdeResult<T> = delegate.visitSeq(ConfiguredSeqAccess(access, humanReadable))

    override fun <A : MapAccess> visitMap(access: A): SerdeResult<T> = delegate.visitMap(ConfiguredMapAccess(access, humanReadable))

    override fun <A : EnumAccess> visitEnum(access: A): SerdeResult<T> = delegate.visitEnum(ConfiguredEnumAccess(access, humanReadable))
}

private class ConfiguredSeqAccess(
    private val delegate: SeqAccess,
    private val humanReadable: Boolean,
) : SeqAccess {
    override fun <T> nextElementSeed(seed: DeserializeSeed<T>): SerdeResult<T?> =
        delegate.nextElementSeed(ConfiguredSeed(seed, humanReadable))

    override fun sizeHint(): Int? = delegate.sizeHint()
}

private class ConfiguredMapAccess(
    private val delegate: MapAccess,
    private val humanReadable: Boolean,
) : MapAccess {
    override fun <K> nextKeySeed(seed: DeserializeSeed<K>): SerdeResult<K?> =
        delegate.nextKeySeed(ConfiguredSeed(seed, humanReadable))

    override fun <V> nextValueSeed(seed: DeserializeSeed<V>): SerdeResult<V> =
        delegate.nextValueSeed(ConfiguredSeed(seed, humanReadable))

    override fun <K, V> nextEntrySeed(
        keySeed: DeserializeSeed<K>,
        valueSeed: DeserializeSeed<V>,
    ): SerdeResult<Pair<K, V>?> =
        delegate.nextEntrySeed(
            ConfiguredSeed(keySeed, humanReadable),
            ConfiguredSeed(valueSeed, humanReadable),
        )

    override fun sizeHint(): Int? = delegate.sizeHint()
}

private class ConfiguredEnumAccess(
    private val delegate: EnumAccess,
    private val humanReadable: Boolean,
) : EnumAccess {
    override fun <V> variantSeed(seed: DeserializeSeed<V>): SerdeResult<Pair<V, VariantAccess>> =
        delegate.variantSeed(ConfiguredSeed(seed, humanReadable)).map { (value, variant) ->
            value to ConfiguredVariantAccess(variant, humanReadable)
        }
}

private class ConfiguredVariantAccess(
    private val delegate: VariantAccess,
    private val humanReadable: Boolean,
) : VariantAccess {
    override fun unitVariant(): SerdeResult<Unit> = delegate.unitVariant()

    override fun <T> newtypeVariantSeed(seed: DeserializeSeed<T>): SerdeResult<T> =
        delegate.newtypeVariantSeed(ConfiguredSeed(seed, humanReadable))

    override fun <V> tupleVariant(len: Int, visitor: Visitor<V>): SerdeResult<V> =
        delegate.tupleVariant(len, ConfiguredVisitor(visitor, humanReadable))

    override fun <V> structVariant(fields: List<String>, visitor: Visitor<V>): SerdeResult<V> =
        delegate.structVariant(fields, ConfiguredVisitor(visitor, humanReadable))
}
