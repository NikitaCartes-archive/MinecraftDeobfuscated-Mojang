package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class NbtOps implements DynamicOps<Tag> {
	public static final NbtOps INSTANCE = new NbtOps();

	protected NbtOps() {
	}

	public Tag empty() {
		return EndTag.INSTANCE;
	}

	public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
		switch (tag.getId()) {
			case 0:
				return dynamicOps.empty();
			case 1:
				return dynamicOps.createByte(((NumericTag)tag).getAsByte());
			case 2:
				return dynamicOps.createShort(((NumericTag)tag).getAsShort());
			case 3:
				return dynamicOps.createInt(((NumericTag)tag).getAsInt());
			case 4:
				return dynamicOps.createLong(((NumericTag)tag).getAsLong());
			case 5:
				return dynamicOps.createFloat(((NumericTag)tag).getAsFloat());
			case 6:
				return dynamicOps.createDouble(((NumericTag)tag).getAsDouble());
			case 7:
				return dynamicOps.createByteList(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
			case 8:
				return dynamicOps.createString(tag.getAsString());
			case 9:
				return this.convertList(dynamicOps, tag);
			case 10:
				return this.convertMap(dynamicOps, tag);
			case 11:
				return dynamicOps.createIntList(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
			case 12:
				return dynamicOps.createLongList(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
			default:
				throw new IllegalStateException("Unknown tag type: " + tag);
		}
	}

	public DataResult<Number> getNumberValue(Tag tag) {
		return tag instanceof NumericTag ? DataResult.success(((NumericTag)tag).getAsNumber()) : DataResult.error("Not a number");
	}

	public Tag createNumeric(Number number) {
		return DoubleTag.valueOf(number.doubleValue());
	}

	public Tag createByte(byte b) {
		return ByteTag.valueOf(b);
	}

	public Tag createShort(short s) {
		return ShortTag.valueOf(s);
	}

	public Tag createInt(int i) {
		return IntTag.valueOf(i);
	}

	public Tag createLong(long l) {
		return LongTag.valueOf(l);
	}

	public Tag createFloat(float f) {
		return FloatTag.valueOf(f);
	}

	public Tag createDouble(double d) {
		return DoubleTag.valueOf(d);
	}

	public Tag createBoolean(boolean bl) {
		return ByteTag.valueOf(bl);
	}

	public DataResult<String> getStringValue(Tag tag) {
		return tag instanceof StringTag ? DataResult.success(tag.getAsString()) : DataResult.error("Not a string");
	}

	public Tag createString(String string) {
		return StringTag.valueOf(string);
	}

	private static CollectionTag<?> createGenericList(byte b, byte c) {
		if (typesMatch(b, c, (byte)4)) {
			return new LongArrayTag(new long[0]);
		} else if (typesMatch(b, c, (byte)1)) {
			return new ByteArrayTag(new byte[0]);
		} else {
			return (CollectionTag<?>)(typesMatch(b, c, (byte)3) ? new IntArrayTag(new int[0]) : new ListTag());
		}
	}

	private static boolean typesMatch(byte b, byte c, byte d) {
		return b == d && (c == d || c == 0);
	}

	private static <T extends Tag> void fillOne(CollectionTag<T> collectionTag, Tag tag, Tag tag2) {
		if (tag instanceof CollectionTag<?> collectionTag2) {
			collectionTag2.forEach(tagx -> collectionTag.add(tagx));
		}

		collectionTag.add(tag2);
	}

	private static <T extends Tag> void fillMany(CollectionTag<T> collectionTag, Tag tag, List<Tag> list) {
		if (tag instanceof CollectionTag<?> collectionTag2) {
			collectionTag2.forEach(tagx -> collectionTag.add(tagx));
		}

		list.forEach(tagx -> collectionTag.add(tagx));
	}

	public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
		if (!(tag instanceof CollectionTag) && !(tag instanceof EndTag)) {
			return DataResult.error("mergeToList called with not a list: " + tag, tag);
		} else {
			CollectionTag<?> collectionTag = createGenericList(tag instanceof CollectionTag ? ((CollectionTag)tag).getElementType() : 0, tag2.getId());
			fillOne(collectionTag, tag, tag2);
			return DataResult.success(collectionTag);
		}
	}

	public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
		if (!(tag instanceof CollectionTag) && !(tag instanceof EndTag)) {
			return DataResult.error("mergeToList called with not a list: " + tag, tag);
		} else {
			CollectionTag<?> collectionTag = createGenericList(
				tag instanceof CollectionTag ? ((CollectionTag)tag).getElementType() : 0, (Byte)list.stream().findFirst().map(Tag::getId).orElse((byte)0)
			);
			fillMany(collectionTag, tag, list);
			return DataResult.success(collectionTag);
		}
	}

	public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error("mergeToMap called with not a map: " + tag, tag);
		} else if (!(tag2 instanceof StringTag)) {
			return DataResult.error("key is not a string: " + tag2, tag);
		} else {
			CompoundTag compoundTag = new CompoundTag();
			if (tag instanceof CompoundTag compoundTag2) {
				compoundTag2.getAllKeys().forEach(string -> compoundTag.put(string, compoundTag2.get(string)));
			}

			compoundTag.put(tag2.getAsString(), tag3);
			return DataResult.success(compoundTag);
		}
	}

	public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error("mergeToMap called with not a map: " + tag, tag);
		} else {
			CompoundTag compoundTag = new CompoundTag();
			if (tag instanceof CompoundTag compoundTag2) {
				compoundTag2.getAllKeys().forEach(string -> compoundTag.put(string, compoundTag2.get(string)));
			}

			List<Tag> list = Lists.<Tag>newArrayList();
			mapLike.entries().forEach(pair -> {
				Tag tagx = (Tag)pair.getFirst();
				if (!(tagx instanceof StringTag)) {
					list.add(tagx);
				} else {
					compoundTag.put(tagx.getAsString(), (Tag)pair.getSecond());
				}
			});
			return !list.isEmpty() ? DataResult.error("some keys are not strings: " + list, compoundTag) : DataResult.success(compoundTag);
		}
	}

	public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
		return !(tag instanceof CompoundTag compoundTag)
			? DataResult.error("Not a map: " + tag)
			: DataResult.success(compoundTag.getAllKeys().stream().map(string -> Pair.of(this.createString(string), compoundTag.get(string))));
	}

	public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
		return !(tag instanceof CompoundTag compoundTag)
			? DataResult.error("Not a map: " + tag)
			: DataResult.success(biConsumer -> compoundTag.getAllKeys().forEach(string -> biConsumer.accept(this.createString(string), compoundTag.get(string))));
	}

	public DataResult<MapLike<Tag>> getMap(Tag tag) {
		return !(tag instanceof CompoundTag compoundTag) ? DataResult.error("Not a map: " + tag) : DataResult.success(new MapLike<Tag>() {
			@Nullable
			public Tag get(Tag tag) {
				return compoundTag.get(tag.getAsString());
			}

			@Nullable
			public Tag get(String string) {
				return compoundTag.get(string);
			}

			@Override
			public Stream<Pair<Tag, Tag>> entries() {
				return compoundTag.getAllKeys().stream().map(string -> Pair.of(NbtOps.this.createString(string), compoundTag.get(string)));
			}

			public String toString() {
				return "MapLike[" + compoundTag + "]";
			}
		});
	}

	public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
		CompoundTag compoundTag = new CompoundTag();
		stream.forEach(pair -> compoundTag.put(((Tag)pair.getFirst()).getAsString(), (Tag)pair.getSecond()));
		return compoundTag;
	}

	public DataResult<Stream<Tag>> getStream(Tag tag) {
		return tag instanceof CollectionTag ? DataResult.success(((CollectionTag)tag).stream().map(tagx -> tagx)) : DataResult.error("Not a list");
	}

	public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
		return tag instanceof CollectionTag<?> collectionTag ? DataResult.success(collectionTag::forEach) : DataResult.error("Not a list: " + tag);
	}

	public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
		return tag instanceof ByteArrayTag ? DataResult.success(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray())) : DynamicOps.super.getByteBuffer(tag);
	}

	public Tag createByteList(ByteBuffer byteBuffer) {
		return new ByteArrayTag(DataFixUtils.toArray(byteBuffer));
	}

	public DataResult<IntStream> getIntStream(Tag tag) {
		return tag instanceof IntArrayTag ? DataResult.success(Arrays.stream(((IntArrayTag)tag).getAsIntArray())) : DynamicOps.super.getIntStream(tag);
	}

	public Tag createIntList(IntStream intStream) {
		return new IntArrayTag(intStream.toArray());
	}

	public DataResult<LongStream> getLongStream(Tag tag) {
		return tag instanceof LongArrayTag ? DataResult.success(Arrays.stream(((LongArrayTag)tag).getAsLongArray())) : DynamicOps.super.getLongStream(tag);
	}

	public Tag createLongList(LongStream longStream) {
		return new LongArrayTag(longStream.toArray());
	}

	public Tag createList(Stream<Tag> stream) {
		PeekingIterator<Tag> peekingIterator = Iterators.peekingIterator(stream.iterator());
		if (!peekingIterator.hasNext()) {
			return new ListTag();
		} else {
			Tag tag = peekingIterator.peek();
			if (tag instanceof ByteTag) {
				List<Byte> list = Lists.<Byte>newArrayList(Iterators.transform(peekingIterator, tagx -> ((ByteTag)tagx).getAsByte()));
				return new ByteArrayTag(list);
			} else if (tag instanceof IntTag) {
				List<Integer> list = Lists.<Integer>newArrayList(Iterators.transform(peekingIterator, tagx -> ((IntTag)tagx).getAsInt()));
				return new IntArrayTag(list);
			} else if (tag instanceof LongTag) {
				List<Long> list = Lists.<Long>newArrayList(Iterators.transform(peekingIterator, tagx -> ((LongTag)tagx).getAsLong()));
				return new LongArrayTag(list);
			} else {
				ListTag listTag = new ListTag();

				while (peekingIterator.hasNext()) {
					Tag tag2 = peekingIterator.next();
					if (!(tag2 instanceof EndTag)) {
						listTag.add(tag2);
					}
				}

				return listTag;
			}
		}
	}

	public Tag remove(Tag tag, String string) {
		if (tag instanceof CompoundTag compoundTag) {
			CompoundTag compoundTag2 = new CompoundTag();
			compoundTag.getAllKeys()
				.stream()
				.filter(string2 -> !Objects.equals(string2, string))
				.forEach(stringx -> compoundTag2.put(stringx, compoundTag.get(stringx)));
			return compoundTag2;
		} else {
			return tag;
		}
	}

	public String toString() {
		return "NBT";
	}

	@Override
	public RecordBuilder<Tag> mapBuilder() {
		return new NbtOps.NbtRecordBuilder();
	}

	class NbtRecordBuilder extends AbstractStringBuilder<Tag, CompoundTag> {
		protected NbtRecordBuilder() {
			super(NbtOps.this);
		}

		protected CompoundTag initBuilder() {
			return new CompoundTag();
		}

		protected CompoundTag append(String string, Tag tag, CompoundTag compoundTag) {
			compoundTag.put(string, tag);
			return compoundTag;
		}

		protected DataResult<Tag> build(CompoundTag compoundTag, Tag tag) {
			if (tag == null || tag == EndTag.INSTANCE) {
				return DataResult.success(compoundTag);
			} else if (!(tag instanceof CompoundTag)) {
				return DataResult.error("mergeToMap called with not a map: " + tag, tag);
			} else {
				CompoundTag compoundTag2 = new CompoundTag(Maps.<String, Tag>newHashMap(((CompoundTag)tag).entries()));

				for (Entry<String, Tag> entry : compoundTag.entries().entrySet()) {
					compoundTag2.put((String)entry.getKey(), (Tag)entry.getValue());
				}

				return DataResult.success(compoundTag2);
			}
		}
	}
}
