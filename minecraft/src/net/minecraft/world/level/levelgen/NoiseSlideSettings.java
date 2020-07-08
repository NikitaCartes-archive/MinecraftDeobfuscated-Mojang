package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseSlideSettings {
	public static final Codec<NoiseSlideSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.INT.fieldOf("target").forGetter(NoiseSlideSettings::target),
					Codec.intRange(0, 256).fieldOf("size").forGetter(NoiseSlideSettings::size),
					Codec.INT.fieldOf("offset").forGetter(NoiseSlideSettings::offset)
				)
				.apply(instance, NoiseSlideSettings::new)
	);
	private final int target;
	private final int size;
	private final int offset;

	public NoiseSlideSettings(int i, int j, int k) {
		this.target = i;
		this.size = j;
		this.offset = k;
	}

	public int target() {
		return this.target;
	}

	public int size() {
		return this.size;
	}

	public int offset() {
		return this.offset;
	}
}
