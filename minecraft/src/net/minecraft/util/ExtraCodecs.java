package net.minecraft.util;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class ExtraCodecs {
	public static final Codec<DoubleStream> DOUBLE_STREAM = new PrimitiveCodec<DoubleStream>() {
		@Override
		public <T> DataResult<DoubleStream> read(DynamicOps<T> dynamicOps, T object) {
			return ExtraCodecs.asDoubleStream(dynamicOps, object);
		}

		public <T> T write(DynamicOps<T> dynamicOps, DoubleStream doubleStream) {
			return ExtraCodecs.createDoubleList(dynamicOps, doubleStream);
		}

		public String toString() {
			return "DoubleStream";
		}
	};

	private static <T> DataResult<DoubleStream> asDoubleStream(DynamicOps<T> dynamicOps, T object) {
		return dynamicOps.getStream(object)
			.flatMap(
				stream -> {
					List<T> list = (List<T>)stream.collect(Collectors.toList());
					return list.stream().allMatch(objectxx -> dynamicOps.getNumberValue((T)objectxx).result().isPresent())
						? DataResult.success(list.stream().mapToDouble(objectxx -> ((Number)dynamicOps.getNumberValue((T)objectxx).result().get()).doubleValue()))
						: DataResult.error("Some elements are not doubles: " + object);
				}
			);
	}

	private static <T> T createDoubleList(DynamicOps<T> dynamicOps, DoubleStream doubleStream) {
		return dynamicOps.createList(doubleStream.mapToObj(dynamicOps::createDouble));
	}

	public static <F, S> Codec<Either<F, S>> xor(Codec<F> codec, Codec<S> codec2) {
		return new ExtraCodecs.XorCodec<>(codec, codec2);
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
			return "XorCodec[" + this.first + ", " + this.second + ']';
		}
	}
}
