package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public class JavaOps implements DynamicOps<Object> {
	public static final JavaOps INSTANCE = new JavaOps();

	private JavaOps() {
	}

	@Override
	public Object empty() {
		return null;
	}

	@Override
	public Object emptyMap() {
		return Map.of();
	}

	@Override
	public Object emptyList() {
		return List.of();
	}

	@Override
	public <U> U convertTo(DynamicOps<U> dynamicOps, Object object) {
		if (object == null) {
			return dynamicOps.empty();
		} else if (object instanceof Map) {
			return this.convertMap(dynamicOps, object);
		} else if (object instanceof ByteList byteList) {
			return dynamicOps.createByteList(ByteBuffer.wrap(byteList.toByteArray()));
		} else if (object instanceof IntList intList) {
			return dynamicOps.createIntList(intList.intStream());
		} else if (object instanceof LongList longList) {
			return dynamicOps.createLongList(longList.longStream());
		} else if (object instanceof List) {
			return this.convertList(dynamicOps, object);
		} else if (object instanceof String string) {
			return dynamicOps.createString(string);
		} else if (object instanceof Boolean boolean_) {
			return dynamicOps.createBoolean(boolean_);
		} else if (object instanceof Byte byte_) {
			return dynamicOps.createByte(byte_);
		} else if (object instanceof Short short_) {
			return dynamicOps.createShort(short_);
		} else if (object instanceof Integer integer) {
			return dynamicOps.createInt(integer);
		} else if (object instanceof Long long_) {
			return dynamicOps.createLong(long_);
		} else if (object instanceof Float float_) {
			return dynamicOps.createFloat(float_);
		} else if (object instanceof Double double_) {
			return dynamicOps.createDouble(double_);
		} else if (object instanceof Number number) {
			return dynamicOps.createNumeric(number);
		} else {
			throw new IllegalStateException("Don't know how to convert " + object);
		}
	}

	@Override
	public DataResult<Number> getNumberValue(Object object) {
		return object instanceof Number number ? DataResult.success(number) : DataResult.error(() -> "Not a number: " + object);
	}

	@Override
	public Object createNumeric(Number number) {
		return number;
	}

	@Override
	public Object createByte(byte b) {
		return b;
	}

	@Override
	public Object createShort(short s) {
		return s;
	}

	@Override
	public Object createInt(int i) {
		return i;
	}

	@Override
	public Object createLong(long l) {
		return l;
	}

	@Override
	public Object createFloat(float f) {
		return f;
	}

	@Override
	public Object createDouble(double d) {
		return d;
	}

	@Override
	public DataResult<Boolean> getBooleanValue(Object object) {
		return object instanceof Boolean boolean_ ? DataResult.success(boolean_) : DataResult.error(() -> "Not a boolean: " + object);
	}

	@Override
	public Object createBoolean(boolean bl) {
		return bl;
	}

	@Override
	public DataResult<String> getStringValue(Object object) {
		return object instanceof String string ? DataResult.success(string) : DataResult.error(() -> "Not a string: " + object);
	}

	@Override
	public Object createString(String string) {
		return string;
	}

	@Override
	public DataResult<Object> mergeToList(Object object, Object object2) {
		if (object == this.empty()) {
			return DataResult.success(List.of(object2));
		} else if (object instanceof List<?> list) {
			return list.isEmpty()
				? DataResult.success(List.of(object2))
				: DataResult.success(ImmutableList.<Object>builder().addAll((Iterable<? extends Object>)list).add(object2).build());
		} else {
			return DataResult.error(() -> "Not a list: " + object);
		}
	}

	@Override
	public DataResult<Object> mergeToList(Object object, List<Object> list) {
		if (object == this.empty()) {
			return DataResult.success(list);
		} else if (object instanceof List<?> list2) {
			return list2.isEmpty() ? DataResult.success(list) : DataResult.success(ImmutableList.builder().addAll(list2).addAll(list).build());
		} else {
			return DataResult.error(() -> "Not a list: " + object);
		}
	}

	@Override
	public DataResult<Object> mergeToMap(Object object, Object object2, Object object3) {
		if (object == this.empty()) {
			return DataResult.success(Map.of(object2, object3));
		} else if (object instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				return DataResult.success(Map.of(object2, object3));
			} else {
				Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size() + 1);
				builder.putAll((Map<? extends Object, ? extends Object>)map);
				builder.put(object2, object3);
				return DataResult.success(builder.buildKeepingLast());
			}
		} else {
			return DataResult.error(() -> "Not a map: " + object);
		}
	}

	@Override
	public DataResult<Object> mergeToMap(Object object, Map<Object, Object> map) {
		if (object == this.empty()) {
			return DataResult.success(map);
		} else if (object instanceof Map<?, ?> map2) {
			if (map2.isEmpty()) {
				return DataResult.success(map);
			} else {
				Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map2.size() + map.size());
				builder.putAll((Map<? extends Object, ? extends Object>)map2);
				builder.putAll(map);
				return DataResult.success(builder.buildKeepingLast());
			}
		} else {
			return DataResult.error(() -> "Not a map: " + object);
		}
	}

	private static Map<Object, Object> mapLikeToMap(MapLike<Object> mapLike) {
		return (Map<Object, Object>)mapLike.entries().collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
	}

	@Override
	public DataResult<Object> mergeToMap(Object object, MapLike<Object> mapLike) {
		if (object == this.empty()) {
			return DataResult.success(mapLikeToMap(mapLike));
		} else if (object instanceof Map<?, ?> map) {
			if (map.isEmpty()) {
				return DataResult.success(mapLikeToMap(mapLike));
			} else {
				Builder<Object, Object> builder = ImmutableMap.builderWithExpectedSize(map.size());
				builder.putAll((Map<? extends Object, ? extends Object>)map);
				mapLike.entries().forEach(pair -> builder.put(pair.getFirst(), pair.getSecond()));
				return DataResult.success(builder.buildKeepingLast());
			}
		} else {
			return DataResult.error(() -> "Not a map: " + object);
		}
	}

	static Stream<Pair<Object, Object>> getMapEntries(Map<?, ?> map) {
		return map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
	}

	@Override
	public DataResult<Stream<Pair<Object, Object>>> getMapValues(Object object) {
		return object instanceof Map<?, ?> map ? DataResult.success(getMapEntries(map)) : DataResult.error(() -> "Not a map: " + object);
	}

	@Override
	public DataResult<Consumer<BiConsumer<Object, Object>>> getMapEntries(Object object) {
		return object instanceof Map<?, ?> map ? DataResult.success(map::forEach) : DataResult.error(() -> "Not a map: " + object);
	}

	@Override
	public Object createMap(Stream<Pair<Object, Object>> stream) {
		return stream.collect(ImmutableMap.toImmutableMap(Pair::getFirst, Pair::getSecond));
	}

	@Override
	public DataResult<MapLike<Object>> getMap(Object object) {
		return object instanceof Map<?, ?> map ? DataResult.success(new MapLike<Object>() {
			@Nullable
			@Override
			public Object get(Object object) {
				return map.get(object);
			}

			@Nullable
			@Override
			public Object get(String string) {
				return map.get(string);
			}

			@Override
			public Stream<Pair<Object, Object>> entries() {
				return JavaOps.getMapEntries(map);
			}

			public String toString() {
				return "MapLike[" + map + "]";
			}
		}) : DataResult.error(() -> "Not a map: " + object);
	}

	@Override
	public Object createMap(Map<Object, Object> map) {
		return map;
	}

	@Override
	public DataResult<Stream<Object>> getStream(Object object) {
		return object instanceof List<?> list ? DataResult.success(list.stream().map(objectx -> objectx)) : DataResult.error(() -> "Not an list: " + object);
	}

	@Override
	public DataResult<Consumer<Consumer<Object>>> getList(Object object) {
		return object instanceof List<?> list ? DataResult.success(list::forEach) : DataResult.error(() -> "Not an list: " + object);
	}

	@Override
	public Object createList(Stream<Object> stream) {
		return stream.toList();
	}

	@Override
	public DataResult<ByteBuffer> getByteBuffer(Object object) {
		return object instanceof ByteList byteList
			? DataResult.success(ByteBuffer.wrap(byteList.toByteArray()))
			: DataResult.error(() -> "Not a byte list: " + object);
	}

	@Override
	public Object createByteList(ByteBuffer byteBuffer) {
		ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
		ByteArrayList byteArrayList = new ByteArrayList();
		byteArrayList.size(byteBuffer2.capacity());
		byteBuffer2.get(0, byteArrayList.elements(), 0, byteArrayList.size());
		return byteArrayList;
	}

	@Override
	public DataResult<IntStream> getIntStream(Object object) {
		return object instanceof IntList intList ? DataResult.success(intList.intStream()) : DataResult.error(() -> "Not an int list: " + object);
	}

	@Override
	public Object createIntList(IntStream intStream) {
		return IntArrayList.toList(intStream);
	}

	@Override
	public DataResult<LongStream> getLongStream(Object object) {
		return object instanceof LongList longList ? DataResult.success(longList.longStream()) : DataResult.error(() -> "Not a long list: " + object);
	}

	@Override
	public Object createLongList(LongStream longStream) {
		return LongArrayList.toList(longStream);
	}

	@Override
	public Object remove(Object object, String string) {
		if (object instanceof Map<?, ?> map) {
			Map<Object, Object> map2 = new LinkedHashMap(map);
			map2.remove(string);
			return Map.copyOf(map2);
		} else {
			return object;
		}
	}

	@Override
	public RecordBuilder<Object> mapBuilder() {
		return new JavaOps.FixedMapBuilder<>(this);
	}

	public String toString() {
		return "Java";
	}

	static final class FixedMapBuilder<T> extends AbstractUniversalBuilder<T, Builder<T, T>> {
		public FixedMapBuilder(DynamicOps<T> dynamicOps) {
			super(dynamicOps);
		}

		protected Builder<T, T> initBuilder() {
			return ImmutableMap.builder();
		}

		protected Builder<T, T> append(T object, T object2, Builder<T, T> builder) {
			return builder.put(object, object2);
		}

		protected DataResult<T> build(Builder<T, T> builder, T object) {
			ImmutableMap<T, T> immutableMap;
			try {
				immutableMap = builder.buildOrThrow();
			} catch (IllegalArgumentException var5) {
				return DataResult.error(() -> "Can't build map: " + var5.getMessage());
			}

			return this.ops().mergeToMap(object, immutableMap);
		}
	}
}
