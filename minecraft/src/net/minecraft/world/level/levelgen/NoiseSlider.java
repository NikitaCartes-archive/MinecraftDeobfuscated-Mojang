package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class NoiseSlider {
	public static final Codec<NoiseSlider> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.DOUBLE.fieldOf("target").forGetter(noiseSlider -> noiseSlider.target),
					ExtraCodecs.NON_NEGATIVE_INT.fieldOf("size").forGetter(noiseSlider -> noiseSlider.size),
					Codec.INT.fieldOf("offset").forGetter(noiseSlider -> noiseSlider.offset)
				)
				.apply(instance, NoiseSlider::new)
	);
	private final double target;
	private final int size;
	private final int offset;

	public NoiseSlider(double d, int i, int j) {
		this.target = d;
		this.size = i;
		this.offset = j;
	}

	public double applySlide(double d, int i) {
		if (this.size <= 0) {
			return d;
		} else {
			double e = (double)(i - this.offset) / (double)this.size;
			return Mth.clampedLerp(this.target, d, e);
		}
	}
}
