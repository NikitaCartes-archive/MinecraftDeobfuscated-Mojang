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
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;

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
		Function<String, E> function2 = createNameLookup(enums, function);
		return new StringRepresentable.EnumCodec<>(enums, function2);
	}

	static <T extends StringRepresentable> Codec<T> fromValues(Supplier<T[]> supplier) {
		T[] stringRepresentables = (T[])supplier.get();
		Function<String, T> function = createNameLookup(stringRepresentables, string -> string);
		ToIntFunction<T> toIntFunction = Util.createIndexLookup(Arrays.asList(stringRepresentables));
		return new StringRepresentable.StringRepresentableCodec<>(stringRepresentables, function, toIntFunction);
	}

	static <T extends StringRepresentable> Function<String, T> createNameLookup(T[] stringRepresentables, Function<String, String> function) {
		if (stringRepresentables.length > 16) {
			Map<String, T> map = (Map<String, T>)Arrays.stream(stringRepresentables)
				.collect(
					Collectors.toMap(stringRepresentable -> (String)function.apply(stringRepresentable.getSerializedName()), stringRepresentable -> stringRepresentable)
				);
			return string -> string == null ? null : (StringRepresentable)map.get(string);
		} else {
			return string -> {
				for (T stringRepresentable : stringRepresentables) {
					if (((String)function.apply(stringRepresentable.getSerializedName())).equals(string)) {
						return stringRepresentable;
					}
				}

				return null;
			};
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
	public static class EnumCodec<E extends Enum<E> & StringRepresentable> extends StringRepresentable.StringRepresentableCodec<E> {
		private final Function<String, E> resolver;

		public EnumCodec(E[] enums, Function<String, E> function) {
			super(enums, function, object -> ((Enum)object).ordinal());
			this.resolver = function;
		}

		@Nullable
		public E byName(@Nullable String string) {
			return (E)this.resolver.apply(string);
		}

		public E byName(@Nullable String string, E enum_) {
			return (E)Objects.requireNonNullElse(this.byName(string), enum_);
		}

		public E byName(@Nullable String string, Supplier<? extends E> supplier) {
			return (E)Objects.requireNonNullElseGet(this.byName(string), supplier);
		}
	}

	public static class StringRepresentableCodec<S extends StringRepresentable> implements Codec<S> {
		private final Codec<S> codec;

		public StringRepresentableCodec(S[] stringRepresentables, Function<String, S> function, ToIntFunction<S> toIntFunction) {
			this.codec = ExtraCodecs.orCompressed(
				Codec.stringResolver(StringRepresentable::getSerializedName, function),
				ExtraCodecs.idResolverCodec(toIntFunction, i -> i >= 0 && i < stringRepresentables.length ? stringRepresentables[i] : null, -1)
			);
		}

		@Override
		public <T> DataResult<Pair<S, T>> decode(DynamicOps<T> dynamicOps, T object) {
			return this.codec.decode(dynamicOps, object);
		}

		public <T> DataResult<T> encode(S stringRepresentable, DynamicOps<T> dynamicOps, T object) {
			return this.codec.encode(stringRepresentable, dynamicOps, object);
		}
	}
}
