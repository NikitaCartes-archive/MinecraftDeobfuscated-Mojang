package net.minecraft.util;

import com.mojang.serialization.MapCodec;

public record KeyDispatchDataCodec<A>(MapCodec<A> codec) {
	public static <A> KeyDispatchDataCodec<A> of(MapCodec<A> mapCodec) {
		return new KeyDispatchDataCodec<>(mapCodec);
	}
}
