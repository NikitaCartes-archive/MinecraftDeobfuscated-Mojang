package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Brightness(int block, int sky) {
	public static final Codec<Integer> LIGHT_VALUE_CODEC = ExtraCodecs.intRange(0, 15);
	public static final Codec<Brightness> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(LIGHT_VALUE_CODEC.fieldOf("block").forGetter(Brightness::block), LIGHT_VALUE_CODEC.fieldOf("sky").forGetter(Brightness::sky))
				.apply(instance, Brightness::new)
	);
	public static Brightness FULL_BRIGHT = new Brightness(15, 15);

	public int pack() {
		return this.block << 4 | this.sky << 20;
	}

	public static Brightness unpack(int i) {
		int j = i >> 4 & 65535;
		int k = i >> 20 & 65535;
		return new Brightness(j, k);
	}
}
