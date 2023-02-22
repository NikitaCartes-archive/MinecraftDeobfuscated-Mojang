package net.minecraft.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;

public interface StringRepresentable {
	int PRE_BUILT_MAP_THRESHOLD = 16;

	String getSerializedName();

	static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnum(Supplier<E[]> supplier) {
		return fromEnumWithMapping(supplier, string -> string);
	}

	static <E extends Enum<E> & StringRepresentable> StringRepresentable.EnumCodec<E> fromEnumWithMapping(
		Supplier<E[]> supplier, Function<String, String> function
	) {
		E[] enums = (E[])supplier.get();
		if (enums.length > 16) {
			Map<String, E> map = (Map<String, E>)Arrays.stream(enums)
				.collect(Collectors.toMap(enum_ -> (String)function.apply(((StringRepresentable)enum_).getSerializedName()), enum_ -> enum_));
			return new StringRepresentable.EnumCodec<>(enums, string -> string == null ? null : (Enum)map.get(string));
		} else {
			return new StringRepresentable.EnumCodec<>(enums, string -> {
				for (E enum_ : enums) {
					if (((String)function.apply(enum_.getSerializedName())).equals(string)) {
						return enum_;
					}
				}

				return null;
			});
		}
	}

	static Keyable keys(StringRepresentable[] stringRepresentables) {
		return new Keyable() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(dynamicOps::createString);
			}
		};
	}

	@Deprecated
	public static class EnumCodec<E extends Enum<E> & StringRepresentable> implements Codec<E> {
		private final Codec<E> codec;
		private final Function<String, E> resolver;

		public EnumCodec(E[] enums, Function<String, E> function) {
			this.codec = ExtraCodecs.orCompressed(
				ExtraCodecs.stringResolverCodec(object -> ((StringRepresentable)object).getSerializedName(), function),
				ExtraCodecs.idResolverCodec(object -> ((Enum)object).ordinal(), i -> i >= 0 && i < enums.length ? enums[i] : null, -1)
			);
			this.resolver = function;
		}

		@Override
		public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> dynamicOps, T object) {
			return this.codec.decode(dynamicOps, object);
		}

		public <T> DataResult<T> encode(E enum_, DynamicOps<T> dynamicOps, T object) {
			return this.codec.encode(enum_, dynamicOps, object);
		}

		@Nullable
		public E byName(@Nullable String string) {
			return (E)this.resolver.apply(string);
		}

		public E byName(@Nullable String string, E enum_) {
			return (E)Objects.requireNonNullElse(this.byName(string), enum_);
		}
	}
}
