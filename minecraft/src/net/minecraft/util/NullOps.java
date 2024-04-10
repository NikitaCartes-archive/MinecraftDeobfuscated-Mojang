package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.RecordBuilder.AbstractUniversalBuilder;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class NullOps implements DynamicOps<Unit> {
	public static final NullOps INSTANCE = new NullOps();

	private NullOps() {
	}

	public <U> U convertTo(DynamicOps<U> dynamicOps, Unit unit) {
		return dynamicOps.empty();
	}

	public Unit empty() {
		return Unit.INSTANCE;
	}

	public Unit emptyMap() {
		return Unit.INSTANCE;
	}

	public Unit emptyList() {
		return Unit.INSTANCE;
	}

	public Unit createNumeric(Number number) {
		return Unit.INSTANCE;
	}

	public Unit createByte(byte b) {
		return Unit.INSTANCE;
	}

	public Unit createShort(short s) {
		return Unit.INSTANCE;
	}

	public Unit createInt(int i) {
		return Unit.INSTANCE;
	}

	public Unit createLong(long l) {
		return Unit.INSTANCE;
	}

	public Unit createFloat(float f) {
		return Unit.INSTANCE;
	}

	public Unit createDouble(double d) {
		return Unit.INSTANCE;
	}

	public Unit createBoolean(boolean bl) {
		return Unit.INSTANCE;
	}

	public Unit createString(String string) {
		return Unit.INSTANCE;
	}

	public DataResult<Number> getNumberValue(Unit unit) {
		return DataResult.error(() -> "Not a number");
	}

	public DataResult<Boolean> getBooleanValue(Unit unit) {
		return DataResult.error(() -> "Not a boolean");
	}

	public DataResult<String> getStringValue(Unit unit) {
		return DataResult.error(() -> "Not a string");
	}

	public DataResult<Unit> mergeToList(Unit unit, Unit unit2) {
		return DataResult.success(Unit.INSTANCE);
	}

	public DataResult<Unit> mergeToList(Unit unit, List<Unit> list) {
		return DataResult.success(Unit.INSTANCE);
	}

	public DataResult<Unit> mergeToMap(Unit unit, Unit unit2, Unit unit3) {
		return DataResult.success(Unit.INSTANCE);
	}

	public DataResult<Unit> mergeToMap(Unit unit, Map<Unit, Unit> map) {
		return DataResult.success(Unit.INSTANCE);
	}

	public DataResult<Unit> mergeToMap(Unit unit, MapLike<Unit> mapLike) {
		return DataResult.success(Unit.INSTANCE);
	}

	public DataResult<Stream<Pair<Unit, Unit>>> getMapValues(Unit unit) {
		return DataResult.error(() -> "Not a map");
	}

	public DataResult<Consumer<BiConsumer<Unit, Unit>>> getMapEntries(Unit unit) {
		return DataResult.error(() -> "Not a map");
	}

	public DataResult<MapLike<Unit>> getMap(Unit unit) {
		return DataResult.error(() -> "Not a map");
	}

	public DataResult<Stream<Unit>> getStream(Unit unit) {
		return DataResult.error(() -> "Not a list");
	}

	public DataResult<Consumer<Consumer<Unit>>> getList(Unit unit) {
		return DataResult.error(() -> "Not a list");
	}

	public DataResult<ByteBuffer> getByteBuffer(Unit unit) {
		return DataResult.error(() -> "Not a byte list");
	}

	public DataResult<IntStream> getIntStream(Unit unit) {
		return DataResult.error(() -> "Not an int list");
	}

	public DataResult<LongStream> getLongStream(Unit unit) {
		return DataResult.error(() -> "Not a long list");
	}

	public Unit createMap(Stream<Pair<Unit, Unit>> stream) {
		return Unit.INSTANCE;
	}

	public Unit createMap(Map<Unit, Unit> map) {
		return Unit.INSTANCE;
	}

	public Unit createList(Stream<Unit> stream) {
		return Unit.INSTANCE;
	}

	public Unit createByteList(ByteBuffer byteBuffer) {
		return Unit.INSTANCE;
	}

	public Unit createIntList(IntStream intStream) {
		return Unit.INSTANCE;
	}

	public Unit createLongList(LongStream longStream) {
		return Unit.INSTANCE;
	}

	public Unit remove(Unit unit, String string) {
		return unit;
	}

	@Override
	public RecordBuilder<Unit> mapBuilder() {
		return new NullOps.NullMapBuilder(this);
	}

	public String toString() {
		return "Null";
	}

	static final class NullMapBuilder extends AbstractUniversalBuilder<Unit, Unit> {
		public NullMapBuilder(DynamicOps<Unit> dynamicOps) {
			super(dynamicOps);
		}

		protected Unit initBuilder() {
			return Unit.INSTANCE;
		}

		protected Unit append(Unit unit, Unit unit2, Unit unit3) {
			return unit3;
		}

		protected DataResult<Unit> build(Unit unit, Unit unit2) {
			return DataResult.success(unit2);
		}
	}
}
