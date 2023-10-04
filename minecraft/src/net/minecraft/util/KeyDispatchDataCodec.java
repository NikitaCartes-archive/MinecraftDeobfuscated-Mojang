package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

public record KeyDispatchDataCodec<A>(Codec<A> codec) {
	@Deprecated
	public static <A> KeyDispatchDataCodec<A> of(Codec<A> codec) {
		return new KeyDispatchDataCodec<>(codec);
	}

	public static <A> KeyDispatchDataCodec<A> of(MapCodec<A> mapCodec) {
		return new KeyDispatchDataCodec<>(mapCodec.codec());
	}
}
