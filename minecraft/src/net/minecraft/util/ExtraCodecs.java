package net.minecraft.util;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.Codec.ResultFunction;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import org.apache.commons.lang3.mutable.MutableObject;

public class ExtraCodecs {
	public static final Codec<Integer> NON_NEGATIVE_INT = intRangeWithMessage(0, Integer.MAX_VALUE, integer -> "Value must be non-negative: " + integer);
	public static final Codec<Integer> POSITIVE_INT = intRangeWithMessage(1, Integer.MAX_VALUE, integer -> "Value must be positive: " + integer);
	public static final Codec<Float> POSITIVE_FLOAT = floatRangeMinExclusiveWithMessage(0.0F, Float.MAX_VALUE, float_ -> "Value must be positive: " + float_);

	public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec2) {
		return new ExtraCodecs.XorCodec<>(codec, codec2);
	}

	public static <P, I> Codec<I> intervalCodec(
		Codec<P> codec, String string, String string2, BiFunction<P, P, DataResult<I>> biFunction, Function<I, P> function, Function<I, P> function2
	) {
		Codec<I> codec2 = Codec.list(codec).comapFlatMap(list -> Util.fixedSize(list, 2).flatMap(listx -> {
				P object = (P)listx.get(0);
				P object2 = (P)listx.get(1);
				return (DataResult)biFunction.apply(object, object2);
			}), object -> ImmutableList.of(function.apply(object), function2.apply(object)));
		Codec<I> codec3 = RecordCodecBuilder.create(
				instance -> instance.group(codec.fieldOf(string).forGetter(Pair::getFirst), codec.fieldOf(string2).forGetter(Pair::getSecond)).apply(instance, Pair::of)
			)
			.comapFlatMap(pair -> (DataResult)biFunction.apply(pair.getFirst(), pair.getSecond()), object -> Pair.of(function.apply(object), function2.apply(object)));
		Codec<I> codec4 = new ExtraCodecs.EitherCodec<>(codec2, codec3).xmap(either -> either.map(object -> object, object -> object), Either::left);
		return Codec.either(codec, codec4)
			.comapFlatMap(either -> either.map(object -> (DataResult)biFunction.apply(object, object), DataResult::success), object -> {
				P object2 = (P)function.apply(object);
				P object3 = (P)function2.apply(object);
				return Objects.equals(object2, object3) ? Either.left(object2) : Either.right(object);
			});
	}

	public static <A> ResultFunction<A> orElsePartial(A object) {
		return new ResultFunction<A>() {
			@Override
			public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> dynamicOps, T object, DataResult<Pair<A, T>> dataResult) {
				MutableObject<String> mutableObject = new MutableObject<>();
				Optional<Pair<A, T>> optional = dataResult.resultOrPartial(mutableObject::setValue);
				return optional.isPresent() ? dataResult : DataResult.error("(" + mutableObject.getValue() + " -> using default)", Pair.of(object, object));
			}

			@Override
			public <T> DataResult<T> coApply(DynamicOps<T> dynamicOps, A object, DataResult<T> dataResult) {
				return dataResult;
			}

			public String toString() {
				return "OrElsePartial[" + object + "]";
			}
		};
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

	private static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRangeMinExclusiveWithMessage(
		N number, N number2, Function<N, String> function
	) {
		return number3 -> ((Comparable)number3).compareTo(number) > 0 && ((Comparable)number3).compareTo(number2) <= 0
				? DataResult.success(number3)
				: DataResult.error((String)function.apply(number3));
	}

	private static Codec<Float> floatRangeMinExclusiveWithMessage(float f, float g, Function<Float, String> function) {
		Function<Float, DataResult<Float>> function2 = checkRangeMinExclusiveWithMessage(f, g, function);
		return Codec.FLOAT.flatXmap(function2, function2);
	}

	public static <T> Function<List<T>, DataResult<List<T>>> nonEmptyListCheck() {
		return list -> list.isEmpty() ? DataResult.error("List must have contents") : DataResult.success(list);
	}

	public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> codec) {
		return codec.flatXmap(nonEmptyListCheck(), nonEmptyListCheck());
	}

	public static <T> Function<List<Supplier<T>>, DataResult<List<Supplier<T>>>> nonNullSupplierListCheck() {
		return list -> {
			List<String> list2 = Lists.<String>newArrayList();

			for (int i = 0; i < list.size(); i++) {
				Supplier<T> supplier = (Supplier<T>)list.get(i);

				try {
					if (supplier.get() == null) {
						list2.add("Missing value [" + i + "] : " + supplier);
					}
				} catch (Exception var5) {
					list2.add("Invalid value [" + i + "]: " + supplier + ", message: " + var5.getMessage());
				}
			}

			return !list2.isEmpty() ? DataResult.error(String.join("; ", list2)) : DataResult.success(list, Lifecycle.stable());
		};
	}

	public static <T> Function<Supplier<T>, DataResult<Supplier<T>>> nonNullSupplierCheck() {
		return supplier -> {
			try {
				if (supplier.get() == null) {
					return DataResult.error("Missing value: " + supplier);
				}
			} catch (Exception var2) {
				return DataResult.error("Invalid value: " + supplier + ", message: " + var2.getMessage());
			}

			return DataResult.success(supplier, Lifecycle.stable());
		};
	}

	public static <A> Codec<A> lazyInitializedCodec(Supplier<Codec<A>> supplier) {
		return new ExtraCodecs.LazyInitializedCodec(supplier);
	}

	static final class EitherCodec<F, S> implements Codec<Either<F, S>> {
		private final Codec<F> first;
		private final Codec<S> second;

		public EitherCodec(Codec<F> codec, Codec<S> codec2) {
			this.first = codec;
			this.second = codec2;
		}

		@Override
		public <T> DataResult<Pair<Either<F, S>, T>> decode(DynamicOps<T> dynamicOps, T object) {
			DataResult<Pair<Either<F, S>, T>> dataResult = this.first.decode(dynamicOps, object).map(pair -> pair.mapFirst(Either::left));
			if (!dataResult.error().isPresent()) {
				return dataResult;
			} else {
				DataResult<Pair<Either<F, S>, T>> dataResult2 = this.second.decode(dynamicOps, object).map(pair -> pair.mapFirst(Either::right));
				return !dataResult2.error().isPresent() ? dataResult2 : dataResult.apply2((pair, pair2) -> pair2, dataResult2);
			}
		}

		public <T> DataResult<T> encode(Either<F, S> either, DynamicOps<T> dynamicOps, T object) {
			return either.map(object2 -> this.first.encode((F)object2, dynamicOps, object), object2 -> this.second.encode((S)object2, dynamicOps, object));
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else if (object != null && this.getClass() == object.getClass()) {
				ExtraCodecs.EitherCodec<?, ?> eitherCodec = (ExtraCodecs.EitherCodec<?, ?>)object;
				return Objects.equals(this.first, eitherCodec.first) && Objects.equals(this.second, eitherCodec.second);
			} else {
				return false;
			}
		}

		public int hashCode() {
			return Objects.hash(new Object[]{this.first, this.second});
		}

		public String toString() {
			return "EitherCodec[" + this.first + ", " + this.second + "]";
		}
	}

	static record LazyInitializedCodec() implements Codec {
		private final Supplier<Codec<A>> delegate;

		LazyInitializedCodec(Supplier<Codec<A>> supplier) {
			Supplier<Codec<A>> var2 = Suppliers.memoize(supplier::get);
			this.delegate = var2;
		}

		@Override
		public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> dynamicOps, T object) {
			return ((Codec)this.delegate.get()).decode(dynamicOps, object);
		}

		@Override
		public <T> DataResult<T> encode(A object, DynamicOps<T> dynamicOps, T object2) {
			return ((Codec)this.delegate.get()).encode(object, dynamicOps, object2);
		}
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
