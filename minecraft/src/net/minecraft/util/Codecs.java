package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class Codecs {
	private static Function<Integer, DataResult<Integer>> checkRange(int i, int j) {
		return integer -> integer >= i && integer <= j
				? DataResult.success(integer)
				: DataResult.error("Value " + integer + " outside of range [" + i + ":" + j + "]", integer);
	}

	public static Codec<Integer> intRange(int i, int j) {
		Function<Integer, DataResult<Integer>> function = checkRange(i, j);
		return Codec.INT.flatXmap(function, function);
	}

	private static Function<Double, DataResult<Double>> checkRange(double d, double e) {
		return double_ -> double_ >= d && double_ <= e
				? DataResult.success(double_)
				: DataResult.error("Value " + double_ + " outside of range [" + d + ":" + e + "]", double_);
	}

	public static Codec<Double> doubleRange(double d, double e) {
		Function<Double, DataResult<Double>> function = checkRange(d, e);
		return Codec.DOUBLE.flatXmap(function, function);
	}

	public static <T> MapCodec<Pair<ResourceKey<T>, T>> withName(ResourceKey<Registry<T>> resourceKey, MapCodec<T> mapCodec) {
		return Codec.mapPair(ResourceLocation.CODEC.<ResourceKey<T>>xmap(ResourceKey.elementKey(resourceKey), ResourceKey::location).fieldOf("name"), mapCodec);
	}

	private static <A> MapCodec<A> mapResult(MapCodec<A> mapCodec, Codecs.ResultFunction<A> resultFunction) {
		return new MapCodec<A>() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return mapCodec.keys(dynamicOps);
			}

			@Override
			public <T> RecordBuilder<T> encode(A object, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
				return resultFunction.coApply(dynamicOps, object, mapCodec.encode(object, dynamicOps, recordBuilder));
			}

			@Override
			public <T> DataResult<A> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
				return resultFunction.apply(dynamicOps, mapLike, mapCodec.decode(dynamicOps, mapLike));
			}

			public String toString() {
				return mapCodec + "[mapResult " + resultFunction + "]";
			}
		};
	}

	public static <A> MapCodec<A> withDefault(MapCodec<A> mapCodec, Consumer<String> consumer, Supplier<? extends A> supplier) {
		return mapResult(mapCodec, new Codecs.ResultFunction<A>() {
			@Override
			public <T> DataResult<A> apply(DynamicOps<T> dynamicOps, MapLike<T> mapLike, DataResult<A> dataResult) {
				return DataResult.success((A)dataResult.resultOrPartial(consumer).orElseGet(supplier));
			}

			@Override
			public <T> RecordBuilder<T> coApply(DynamicOps<T> dynamicOps, A object, RecordBuilder<T> recordBuilder) {
				return recordBuilder;
			}

			public String toString() {
				return "WithDefault[" + supplier.get() + "]";
			}
		});
	}

	interface ResultFunction<A> {
		<T> DataResult<A> apply(DynamicOps<T> dynamicOps, MapLike<T> mapLike, DataResult<A> dataResult);

		<T> RecordBuilder<T> coApply(DynamicOps<T> dynamicOps, A object, RecordBuilder<T> recordBuilder);
	}
}
