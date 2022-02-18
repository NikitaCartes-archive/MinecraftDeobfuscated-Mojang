package net.minecraft.resources;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.ListBuilder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.ListBuilder.Builder;
import com.mojang.serialization.RecordBuilder.MapBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public abstract class DelegatingOps<T> implements DynamicOps<T> {
	protected final DynamicOps<T> delegate;

	protected DelegatingOps(DynamicOps<T> dynamicOps) {
		this.delegate = dynamicOps;
	}

	@Override
	public T empty() {
		return this.delegate.empty();
	}

	@Override
	public <U> U convertTo(DynamicOps<U> dynamicOps, T object) {
		return this.delegate.convertTo(dynamicOps, object);
	}

	@Override
	public DataResult<Number> getNumberValue(T object) {
		return this.delegate.getNumberValue(object);
	}

	@Override
	public T createNumeric(Number number) {
		return this.delegate.createNumeric(number);
	}

	@Override
	public T createByte(byte b) {
		return this.delegate.createByte(b);
	}

	@Override
	public T createShort(short s) {
		return this.delegate.createShort(s);
	}

	@Override
	public T createInt(int i) {
		return this.delegate.createInt(i);
	}

	@Override
	public T createLong(long l) {
		return this.delegate.createLong(l);
	}

	@Override
	public T createFloat(float f) {
		return this.delegate.createFloat(f);
	}

	@Override
	public T createDouble(double d) {
		return this.delegate.createDouble(d);
	}

	@Override
	public DataResult<Boolean> getBooleanValue(T object) {
		return this.delegate.getBooleanValue(object);
	}

	@Override
	public T createBoolean(boolean bl) {
		return this.delegate.createBoolean(bl);
	}

	@Override
	public DataResult<String> getStringValue(T object) {
		return this.delegate.getStringValue(object);
	}

	@Override
	public T createString(String string) {
		return this.delegate.createString(string);
	}

	@Override
	public DataResult<T> mergeToList(T object, T object2) {
		return this.delegate.mergeToList(object, object2);
	}

	@Override
	public DataResult<T> mergeToList(T object, List<T> list) {
		return this.delegate.mergeToList(object, list);
	}

	@Override
	public DataResult<T> mergeToMap(T object, T object2, T object3) {
		return this.delegate.mergeToMap(object, object2, object3);
	}

	@Override
	public DataResult<T> mergeToMap(T object, MapLike<T> mapLike) {
		return this.delegate.mergeToMap(object, mapLike);
	}

	@Override
	public DataResult<Stream<Pair<T, T>>> getMapValues(T object) {
		return this.delegate.getMapValues(object);
	}

	@Override
	public DataResult<Consumer<BiConsumer<T, T>>> getMapEntries(T object) {
		return this.delegate.getMapEntries(object);
	}

	@Override
	public T createMap(Stream<Pair<T, T>> stream) {
		return this.delegate.createMap(stream);
	}

	@Override
	public DataResult<MapLike<T>> getMap(T object) {
		return this.delegate.getMap(object);
	}

	@Override
	public DataResult<Stream<T>> getStream(T object) {
		return this.delegate.getStream(object);
	}

	@Override
	public DataResult<Consumer<Consumer<T>>> getList(T object) {
		return this.delegate.getList(object);
	}

	@Override
	public T createList(Stream<T> stream) {
		return this.delegate.createList(stream);
	}

	@Override
	public DataResult<ByteBuffer> getByteBuffer(T object) {
		return this.delegate.getByteBuffer(object);
	}

	@Override
	public T createByteList(ByteBuffer byteBuffer) {
		return this.delegate.createByteList(byteBuffer);
	}

	@Override
	public DataResult<IntStream> getIntStream(T object) {
		return this.delegate.getIntStream(object);
	}

	@Override
	public T createIntList(IntStream intStream) {
		return this.delegate.createIntList(intStream);
	}

	@Override
	public DataResult<LongStream> getLongStream(T object) {
		return this.delegate.getLongStream(object);
	}

	@Override
	public T createLongList(LongStream longStream) {
		return this.delegate.createLongList(longStream);
	}

	@Override
	public T remove(T object, String string) {
		return this.delegate.remove(object, string);
	}

	@Override
	public boolean compressMaps() {
		return this.delegate.compressMaps();
	}

	@Override
	public ListBuilder<T> listBuilder() {
		return new Builder<>(this);
	}

	@Override
	public RecordBuilder<T> mapBuilder() {
		return new MapBuilder<>(this);
	}
}
