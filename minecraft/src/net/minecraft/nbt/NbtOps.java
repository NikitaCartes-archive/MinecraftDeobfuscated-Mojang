package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractStringBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class NbtOps implements DynamicOps<Tag> {
	public static final NbtOps INSTANCE = new NbtOps();
	private static final String WRAPPER_MARKER = "";

	protected NbtOps() {
	}

	public Tag empty() {
		return EndTag.INSTANCE;
	}

	public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
		return (U)(switch (tag.getId()) {
			case 0 -> (Object)dynamicOps.empty();
			case 1 -> (Object)dynamicOps.createByte(((NumericTag)tag).getAsByte());
			case 2 -> (Object)dynamicOps.createShort(((NumericTag)tag).getAsShort());
			case 3 -> (Object)dynamicOps.createInt(((NumericTag)tag).getAsInt());
			case 4 -> (Object)dynamicOps.createLong(((NumericTag)tag).getAsLong());
			case 5 -> (Object)dynamicOps.createFloat(((NumericTag)tag).getAsFloat());
			case 6 -> (Object)dynamicOps.createDouble(((NumericTag)tag).getAsDouble());
			case 7 -> (Object)dynamicOps.createByteList(ByteBuffer.wrap(((ByteArrayTag)tag).getAsByteArray()));
			case 8 -> (Object)dynamicOps.createString(tag.getAsString());
			case 9 -> (Object)this.convertList(dynamicOps, tag);
			case 10 -> (Object)this.convertMap(dynamicOps, tag);
			case 11 -> (Object)dynamicOps.createIntList(Arrays.stream(((IntArrayTag)tag).getAsIntArray()));
			case 12 -> (Object)dynamicOps.createLongList(Arrays.stream(((LongArrayTag)tag).getAsLongArray()));
			default -> throw new IllegalStateException("Unknown tag type: " + tag);
		});
	}

	public DataResult<Number> getNumberValue(Tag tag) {
		return tag instanceof NumericTag numericTag ? DataResult.success(numericTag.getAsNumber()) : DataResult.error(() -> "Not a number");
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
		return tag instanceof StringTag stringTag ? DataResult.success(stringTag.getAsString()) : DataResult.error(() -> "Not a string");
	}

	public Tag createString(String string) {
		return StringTag.valueOf(string);
	}

	public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
		return (DataResult<Tag>)createCollector(tag)
			.map(listCollector -> DataResult.success(listCollector.accept(tag2).result()))
			.orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
	}

	public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
		return (DataResult<Tag>)createCollector(tag)
			.map(listCollector -> DataResult.success(listCollector.acceptAll(list).result()))
			.orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + tag, tag));
	}

	public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
		} else if (!(tag2 instanceof StringTag)) {
			return DataResult.error(() -> "key is not a string: " + tag2, tag);
		} else {
			CompoundTag compoundTag2 = tag instanceof CompoundTag compoundTag ? compoundTag.shallowCopy() : new CompoundTag();
			compoundTag2.put(tag2.getAsString(), tag3);
			return DataResult.success(compoundTag2);
		}
	}

	public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
		} else {
			CompoundTag compoundTag2 = tag instanceof CompoundTag compoundTag ? compoundTag.shallowCopy() : new CompoundTag();
			List<Tag> list = new ArrayList();
			mapLike.entries().forEach(pair -> {
				Tag tagx = (Tag)pair.getFirst();
				if (!(tagx instanceof StringTag)) {
					list.add(tagx);
				} else {
					compoundTag2.put(tagx.getAsString(), (Tag)pair.getSecond());
				}
			});
			return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundTag2) : DataResult.success(compoundTag2);
		}
	}

	public DataResult<Tag> mergeToMap(Tag tag, Map<Tag, Tag> map) {
		if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
			return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
		} else {
			CompoundTag compoundTag2 = tag instanceof CompoundTag compoundTag ? compoundTag.shallowCopy() : new CompoundTag();
			List<Tag> list = new ArrayList();

			for (Entry<Tag, Tag> entry : map.entrySet()) {
				Tag tag2 = (Tag)entry.getKey();
				if (tag2 instanceof StringTag) {
					compoundTag2.put(tag2.getAsString(), (Tag)entry.getValue());
				} else {
					list.add(tag2);
				}
			}

			return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundTag2) : DataResult.success(compoundTag2);
		}
	}

	public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
		return tag instanceof CompoundTag compoundTag
			? DataResult.success(compoundTag.entrySet().stream().map(entry -> Pair.of(this.createString((String)entry.getKey()), (Tag)entry.getValue())))
			: DataResult.error(() -> "Not a map: " + tag);
	}

	public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
		return tag instanceof CompoundTag compoundTag ? DataResult.success(biConsumer -> {
			for (Entry<String, Tag> entry : compoundTag.entrySet()) {
				biConsumer.accept(this.createString((String)entry.getKey()), (Tag)entry.getValue());
			}
		}) : DataResult.error(() -> "Not a map: " + tag);
	}

	public DataResult<MapLike<Tag>> getMap(Tag tag) {
		return tag instanceof CompoundTag compoundTag ? DataResult.success(new MapLike<Tag>() {
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
				return compoundTag.entrySet().stream().map(entry -> Pair.of(NbtOps.this.createString((String)entry.getKey()), (Tag)entry.getValue()));
			}

			public String toString() {
				return "MapLike[" + compoundTag + "]";
			}
		}) : DataResult.error(() -> "Not a map: " + tag);
	}

	public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
		CompoundTag compoundTag = new CompoundTag();
		stream.forEach(pair -> compoundTag.put(((Tag)pair.getFirst()).getAsString(), (Tag)pair.getSecond()));
		return compoundTag;
	}

	private static Tag tryUnwrap(CompoundTag compoundTag) {
		if (compoundTag.size() == 1) {
			Tag tag = compoundTag.get("");
			if (tag != null) {
				return tag;
			}
		}

		return compoundTag;
	}

	public DataResult<Stream<Tag>> getStream(Tag tag) {
		if (tag instanceof ListTag listTag) {
			return listTag.getElementType() == 10
				? DataResult.success(listTag.stream().map(tagx -> tryUnwrap((CompoundTag)tagx)))
				: DataResult.success(listTag.stream());
		} else {
			return tag instanceof CollectionTag<?> collectionTag ? DataResult.success(collectionTag.stream().map(tagx -> tagx)) : DataResult.error(() -> "Not a list");
		}
	}

	public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
		if (tag instanceof ListTag listTag) {
			return listTag.getElementType() == 10 ? DataResult.success(consumer -> {
				for (Tag tagx : listTag) {
					consumer.accept(tryUnwrap((CompoundTag)tagx));
				}
			}) : DataResult.success(listTag::forEach);
		} else {
			return tag instanceof CollectionTag<?> collectionTag ? DataResult.success(collectionTag::forEach) : DataResult.error(() -> "Not a list: " + tag);
		}
	}

	public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
		return tag instanceof ByteArrayTag byteArrayTag ? DataResult.success(ByteBuffer.wrap(byteArrayTag.getAsByteArray())) : DynamicOps.super.getByteBuffer(tag);
	}

	public Tag createByteList(ByteBuffer byteBuffer) {
		ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
		byte[] bs = new byte[byteBuffer.capacity()];
		byteBuffer2.get(0, bs, 0, bs.length);
		return new ByteArrayTag(bs);
	}

	public DataResult<IntStream> getIntStream(Tag tag) {
		return tag instanceof IntArrayTag intArrayTag ? DataResult.success(Arrays.stream(intArrayTag.getAsIntArray())) : DynamicOps.super.getIntStream(tag);
	}

	public Tag createIntList(IntStream intStream) {
		return new IntArrayTag(intStream.toArray());
	}

	public DataResult<LongStream> getLongStream(Tag tag) {
		return tag instanceof LongArrayTag longArrayTag ? DataResult.success(Arrays.stream(longArrayTag.getAsLongArray())) : DynamicOps.super.getLongStream(tag);
	}

	public Tag createLongList(LongStream longStream) {
		return new LongArrayTag(longStream.toArray());
	}

	public Tag createList(Stream<Tag> stream) {
		return NbtOps.InitialListCollector.INSTANCE.acceptAll(stream).result();
	}

	public Tag remove(Tag tag, String string) {
		if (tag instanceof CompoundTag compoundTag) {
			CompoundTag compoundTag2 = compoundTag.shallowCopy();
			compoundTag2.remove(string);
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

	private static Optional<NbtOps.ListCollector> createCollector(Tag tag) {
		if (tag instanceof EndTag) {
			return Optional.of(NbtOps.InitialListCollector.INSTANCE);
		} else {
			if (tag instanceof CollectionTag<?> collectionTag) {
				if (collectionTag.isEmpty()) {
					return Optional.of(NbtOps.InitialListCollector.INSTANCE);
				}

				if (collectionTag instanceof ListTag listTag) {
					return switch (listTag.getElementType()) {
						case 0 -> Optional.of(NbtOps.InitialListCollector.INSTANCE);
						case 10 -> Optional.of(new NbtOps.HeterogenousListCollector(listTag));
						default -> Optional.of(new NbtOps.HomogenousListCollector(listTag));
					};
				}

				if (collectionTag instanceof ByteArrayTag byteArrayTag) {
					return Optional.of(new NbtOps.ByteListCollector(byteArrayTag.getAsByteArray()));
				}

				if (collectionTag instanceof IntArrayTag intArrayTag) {
					return Optional.of(new NbtOps.IntListCollector(intArrayTag.getAsIntArray()));
				}

				if (collectionTag instanceof LongArrayTag longArrayTag) {
					return Optional.of(new NbtOps.LongListCollector(longArrayTag.getAsLongArray()));
				}
			}

			return Optional.empty();
		}
	}

	static class ByteListCollector implements NbtOps.ListCollector {
		private final ByteArrayList values = new ByteArrayList();

		public ByteListCollector(byte b) {
			this.values.add(b);
		}

		public ByteListCollector(byte[] bs) {
			this.values.addElements(0, bs);
		}

		@Override
		public NbtOps.ListCollector accept(Tag tag) {
			if (tag instanceof ByteTag byteTag) {
				this.values.add(byteTag.getAsByte());
				return this;
			} else {
				return new NbtOps.HeterogenousListCollector(this.values).accept(tag);
			}
		}

		@Override
		public Tag result() {
			return new ByteArrayTag(this.values.toByteArray());
		}
	}

	static class HeterogenousListCollector implements NbtOps.ListCollector {
		private final ListTag result = new ListTag();

		public HeterogenousListCollector() {
		}

		public HeterogenousListCollector(Collection<Tag> collection) {
			this.result.addAll(collection);
		}

		public HeterogenousListCollector(IntArrayList intArrayList) {
			intArrayList.forEach(i -> this.result.add(wrapElement(IntTag.valueOf(i))));
		}

		public HeterogenousListCollector(ByteArrayList byteArrayList) {
			byteArrayList.forEach(b -> this.result.add(wrapElement(ByteTag.valueOf(b))));
		}

		public HeterogenousListCollector(LongArrayList longArrayList) {
			longArrayList.forEach(l -> this.result.add(wrapElement(LongTag.valueOf(l))));
		}

		private static boolean isWrapper(CompoundTag compoundTag) {
			return compoundTag.size() == 1 && compoundTag.contains("");
		}

		private static Tag wrapIfNeeded(Tag tag) {
			if (tag instanceof CompoundTag compoundTag && !isWrapper(compoundTag)) {
				return compoundTag;
			}

			return wrapElement(tag);
		}

		private static CompoundTag wrapElement(Tag tag) {
			CompoundTag compoundTag = new CompoundTag();
			compoundTag.put("", tag);
			return compoundTag;
		}

		@Override
		public NbtOps.ListCollector accept(Tag tag) {
			this.result.add(wrapIfNeeded(tag));
			return this;
		}

		@Override
		public Tag result() {
			return this.result;
		}
	}

	static class HomogenousListCollector implements NbtOps.ListCollector {
		private final ListTag result = new ListTag();

		HomogenousListCollector(Tag tag) {
			this.result.add(tag);
		}

		HomogenousListCollector(ListTag listTag) {
			this.result.addAll(listTag);
		}

		@Override
		public NbtOps.ListCollector accept(Tag tag) {
			if (tag.getId() != this.result.getElementType()) {
				return new NbtOps.HeterogenousListCollector().acceptAll(this.result).accept(tag);
			} else {
				this.result.add(tag);
				return this;
			}
		}

		@Override
		public Tag result() {
			return this.result;
		}
	}

	static class InitialListCollector implements NbtOps.ListCollector {
		public static final NbtOps.InitialListCollector INSTANCE = new NbtOps.InitialListCollector();

		private InitialListCollector() {
		}

		@Override
		public NbtOps.ListCollector accept(Tag tag) {
			if (tag instanceof CompoundTag compoundTag) {
				return new NbtOps.HeterogenousListCollector().accept(compoundTag);
			} else if (tag instanceof ByteTag byteTag) {
				return new NbtOps.ByteListCollector(byteTag.getAsByte());
			} else if (tag instanceof IntTag intTag) {
				return new NbtOps.IntListCollector(intTag.getAsInt());
			} else {
				return (NbtOps.ListCollector)(tag instanceof LongTag longTag ? new NbtOps.LongListCollector(longTag.getAsLong()) : new NbtOps.HomogenousListCollector(tag));
			}
		}

		@Override
		public Tag result() {
			return new ListTag();
		}
	}

	static class IntListCollector implements NbtOps.ListCollector {
		private final IntArrayList values = new IntArrayList();

		public IntListCollector(int i) {
			this.values.add(i);
		}

		public IntListCollector(int[] is) {
			this.values.addElements(0, is);
		}

		@Override
		public NbtOps.ListCollector accept(Tag tag) {
			if (tag instanceof IntTag intTag) {
				this.values.add(intTag.getAsInt());
				return this;
			} else {
				return new NbtOps.HeterogenousListCollector(this.values).accept(tag);
			}
		}

		@Override
		public Tag result() {
			return new IntArrayTag(this.values.toIntArray());
		}
	}

	interface ListCollector {
		NbtOps.ListCollector accept(Tag tag);

		default NbtOps.ListCollector acceptAll(Iterable<Tag> iterable) {
			NbtOps.ListCollector listCollector = this;

			for (Tag tag : iterable) {
				listCollector = listCollector.accept(tag);
			}

			return listCollector;
		}

		default NbtOps.ListCollector acceptAll(Stream<Tag> stream) {
			return this.acceptAll(stream::iterator);
		}

		Tag result();
	}

	static class LongListCollector implements NbtOps.ListCollector {
		private final LongArrayList values = new LongArrayList();

		public LongListCollector(long l) {
			this.values.add(l);
		}

		public LongListCollector(long[] ls) {
			this.values.addElements(0, ls);
		}

		@Override
		public NbtOps.ListCollector accept(Tag tag) {
			if (tag instanceof LongTag longTag) {
				this.values.add(longTag.getAsLong());
				return this;
			} else {
				return new NbtOps.HeterogenousListCollector(this.values).accept(tag);
			}
		}

		@Override
		public Tag result() {
			return new LongArrayTag(this.values.toLongArray());
		}
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
			} else if (!(tag instanceof CompoundTag compoundTag2)) {
				return DataResult.error(() -> "mergeToMap called with not a map: " + tag, tag);
			} else {
				CompoundTag compoundTag3 = compoundTag2.shallowCopy();

				for (Entry<String, Tag> entry : compoundTag.entrySet()) {
					compoundTag3.put((String)entry.getKey(), (Tag)entry.getValue());
				}

				return DataResult.success(compoundTag3);
			}
		}
	}
}
