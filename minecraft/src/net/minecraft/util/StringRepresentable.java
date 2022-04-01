package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface StringRepresentable {
	String getSerializedName();

	static <E extends Enum<E> & StringRepresentable> Codec<E> fromEnum(Supplier<E[]> supplier, Function<String, E> function) {
		E[] enums = (E[])supplier.get();
		return ExtraCodecs.orCompressed(
			ExtraCodecs.stringResolverCodec(object -> ((StringRepresentable)object).getSerializedName(), function),
			ExtraCodecs.idResolverCodec(object -> ((Enum)object).ordinal(), i -> i >= 0 && i < enums.length ? enums[i] : null, -1)
		);
	}

	static Keyable keys(StringRepresentable[] stringRepresentables) {
		return new Keyable() {
			@Override
			public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
				return Arrays.stream(stringRepresentables).map(StringRepresentable::getSerializedName).map(dynamicOps::createString);
			}
		};
	}
}
