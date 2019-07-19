package net.minecraft.nbt;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.PeekingIterator;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NbtOps implements DynamicOps<Tag> {
	public static final NbtOps INSTANCE = new NbtOps();

	protected NbtOps() {
	}

	public Tag empty() {
		return new EndTag();
	}

	public Type<?> getType(Tag tag) {
		switch (tag.getId()) {
			case 0:
				return DSL.nilType();
			case 1:
				return DSL.byteType();
			case 2:
				return DSL.shortType();
			case 3:
				return DSL.intType();
			case 4:
				return DSL.longType();
			case 5:
				return DSL.floatType();
			case 6:
				return DSL.doubleType();
			case 7:
				return DSL.list(DSL.byteType());
			case 8:
				return DSL.string();
			case 9:
				return DSL.list(DSL.remainderType());
			case 10:
				return DSL.compoundList(DSL.remainderType(), DSL.remainderType());
			case 11:
				return DSL.list(DSL.intType());
			case 12:
				return DSL.list(DSL.longType());
			default:
				return DSL.remainderType();
		}
	}

	public Optional<Number> getNumberValue(Tag tag) {
		return tag instanceof NumericTag ? Optional.of(((NumericTag)tag).getAsNumber()) : Optional.empty();
	}

	public Tag createNumeric(Number number) {
		return new DoubleTag(number.doubleValue());
	}

	public Tag createByte(byte b) {
		return new ByteTag(b);
	}

	public Tag createShort(short s) {
		return new ShortTag(s);
	}

	public Tag createInt(int i) {
		return new IntTag(i);
	}

	public Tag createLong(long l) {
		return new LongTag(l);
	}

	public Tag createFloat(float f) {
		return new FloatTag(f);
	}

	public Tag createDouble(double d) {
		return new DoubleTag(d);
	}

	public Optional<String> getStringValue(Tag tag) {
		return tag instanceof StringTag ? Optional.of(tag.getAsString()) : Optional.empty();
	}

	public Tag createString(String string) {
		return new StringTag(string);
	}

	public Tag mergeInto(Tag tag, Tag tag2) {
		if (tag2 instanceof EndTag) {
			return tag;
		} else if (!(tag instanceof CompoundTag)) {
			if (tag instanceof EndTag) {
				throw new IllegalArgumentException("mergeInto called with a null input.");
			} else if (tag instanceof CollectionTag) {
				CollectionTag<Tag> collectionTag = new ListTag();
				CollectionTag<?> collectionTag2 = (CollectionTag<?>)tag;
				collectionTag.addAll(collectionTag2);
				collectionTag.add(tag2);
				return collectionTag;
			} else {
				return tag;
			}
		} else if (!(tag2 instanceof CompoundTag)) {
			return tag;
		} else {
			CompoundTag compoundTag = new CompoundTag();
			CompoundTag compoundTag2 = (CompoundTag)tag;

			for (String string : compoundTag2.getAllKeys()) {
				compoundTag.put(string, compoundTag2.get(string));
			}

			CompoundTag compoundTag3 = (CompoundTag)tag2;

			for (String string2 : compoundTag3.getAllKeys()) {
				compoundTag.put(string2, compoundTag3.get(string2));
			}

			return compoundTag;
		}
	}

	public Tag mergeInto(Tag tag, Tag tag2, Tag tag3) {
		CompoundTag compoundTag;
		if (tag instanceof EndTag) {
			compoundTag = new CompoundTag();
		} else {
			if (!(tag instanceof CompoundTag)) {
				return tag;
			}

			CompoundTag compoundTag2 = (CompoundTag)tag;
			compoundTag = new CompoundTag();
			compoundTag2.getAllKeys().forEach(string -> compoundTag.put(string, compoundTag2.get(string)));
		}

		compoundTag.put(tag2.getAsString(), tag3);
		return compoundTag;
	}

	public Tag merge(Tag tag, Tag tag2) {
		if (tag instanceof EndTag) {
			return tag2;
		} else if (tag2 instanceof EndTag) {
			return tag;
		} else {
			if (tag instanceof CompoundTag && tag2 instanceof CompoundTag) {
				CompoundTag compoundTag = (CompoundTag)tag;
				CompoundTag compoundTag2 = (CompoundTag)tag2;
				CompoundTag compoundTag3 = new CompoundTag();
				compoundTag.getAllKeys().forEach(string -> compoundTag3.put(string, compoundTag.get(string)));
				compoundTag2.getAllKeys().forEach(string -> compoundTag3.put(string, compoundTag2.get(string)));
			}

			if (tag instanceof CollectionTag && tag2 instanceof CollectionTag) {
				ListTag listTag = new ListTag();
				listTag.addAll((CollectionTag)tag);
				listTag.addAll((CollectionTag)tag2);
				return listTag;
			} else {
				throw new IllegalArgumentException("Could not merge " + tag + " and " + tag2);
			}
		}
	}

	public Optional<Map<Tag, Tag>> getMapValues(Tag tag) {
		if (tag instanceof CompoundTag) {
			CompoundTag compoundTag = (CompoundTag)tag;
			return Optional.of(
				compoundTag.getAllKeys()
					.stream()
					.map(string -> Pair.of(this.createString(string), compoundTag.get(string)))
					.collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
			);
		} else {
			return Optional.empty();
		}
	}

	public Tag createMap(Map<Tag, Tag> map) {
		CompoundTag compoundTag = new CompoundTag();

		for (Entry<Tag, Tag> entry : map.entrySet()) {
			compoundTag.put(((Tag)entry.getKey()).getAsString(), (Tag)entry.getValue());
		}

		return compoundTag;
	}

	public Optional<Stream<Tag>> getStream(Tag tag) {
		return tag instanceof CollectionTag ? Optional.of(((CollectionTag)tag).stream().map(tagx -> tagx)) : Optional.empty();
	}

	public Optional<ByteBuffer> getByteBuffer(Tag tag) {
		return tag instanceof ByteArrayTag ? Optional.of(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray())) : DynamicOps.super.getByteBuffer(tag);
	}

	public Tag createByteList(ByteBuffer byteBuffer) {
		return new ByteArrayTag(DataFixUtils.toArray(byteBuffer));
	}

	public Optional<IntStream> getIntStream(Tag tag) {
		return tag instanceof IntArrayTag ? Optional.of(Arrays.stream(((IntArrayTag)tag).getAsIntArray())) : DynamicOps.super.getIntStream(tag);
	}

	public Tag createIntList(IntStream intStream) {
		return new IntArrayTag(intStream.toArray());
	}

	public Optional<LongStream> getLongStream(Tag tag) {
		return tag instanceof LongArrayTag ? Optional.of(Arrays.stream(((LongArrayTag)tag).getAsLongArray())) : DynamicOps.super.getLongStream(tag);
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
		if (tag instanceof CompoundTag) {
			CompoundTag compoundTag = (CompoundTag)tag;
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
}
