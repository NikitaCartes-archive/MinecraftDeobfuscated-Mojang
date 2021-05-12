package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class ExtraCodecs {
	public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, integer -> "Value must be non-negative: " + integer);
	public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, integer -> "Value must be positive: " + integer);

	public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec2) {
		return new ExtraCodecs.XorCodec<>(codec, codec2);
	}

	private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRangeWithMessage(N number, N number2, Function<N, String> function) {
		return number3 -> ((Comparable)number3).compareTo(number) >= 0 && ((Comparable)number3).compareTo(number2) <= 0
				? DataResult.success(number3)
				: DataResult.error((String)function.apply(number3));
	}

	private static Codec<Integer> intRangeWithMessage(int i, int j, Function<Integer, String> function) {
		Function<Integer, DataResult<Integer>> function2 = checkRangeWithMessage(i, j, function);
		return Codec.INT.flatXmap(function2, function2);
	}

	public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
		return list -> list.isEmpty() ? DataResult.error("List must have contents") : DataResult.success(list);
	}

	static final class XorCodec<F, S> implements Codec<Either<F, S>> {
		private final Codec<F> first;
		private final Codec<S> second;

		public XorCodec(Codec<F> codec, Codec<S> codec2) {
			this.first = codec;
			this.second = codec2;
		}

		@Override
		public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicOps, T object) {
			DataResult<Pair<Either<F, S>, T>> dataResult = this.first.decode(dynamicOps, object).map(pair -> pair.mapFirst(Either::left));
			DataResult<Pair<Either<F, S>, T>> dataResult2 = this.second.decode(dynamicOps, object).map(pair -> pair.mapFirst(Either::right));
			Optional<Pair<Either<F, S>, T>> optional = dataResult.result();
			Optional<Pair<Either<F, S>, T>> optional2 = dataResult2.result();
			if (optional.isPresent() && optional2.isPresent()) {
				return DataResult.error(
					"Both alternatives read successfully, can not pick the correct one; first: " + optional.get() + " second: " + optional2.get(),
					(Pair<Either<F, S>, T>)optional.get()
				);
			} else {
				return optional.isPresent() ? dataResult : dataResult2;
			}
		}

		public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicOps, T object) {
			return either.map(object2 -> this.first.encode((F)object2, dynamicOps, object), object2 -> this.second.encode((S)object2, dynamicOps, object));
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				ExtraCodecs.XorCodec<?, ?> xorCodec = (ExtraCodecs.XorCodec<?, ?>)object;
				return Objects.equals(this.first, xorCodec.first) && Objects.equals(this.second, xorCodec.second);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.first, this.second});
		}

		public String toString() {
			return "XorCodec[" + this.first + ", " + this.second + "]";
		}
	}
}
