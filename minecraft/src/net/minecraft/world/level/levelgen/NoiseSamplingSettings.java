package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class NoiseSamplingSettings {
	public static final Codec<NoiseSamplingSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.DOUBLE.fieldOf("xz_scale").forGetter(NoiseSamplingSettings::xzScale),
					Codec.DOUBLE.fieldOf("y_scale").forGetter(NoiseSamplingSettings::yScale),
					Codec.DOUBLE.fieldOf("xz_factor").forGetter(NoiseSamplingSettings::xzFactor),
					Codec.DOUBLE.fieldOf("y_factor").forGetter(NoiseSamplingSettings::yFactor)
				)
				.apply(instance, NoiseSamplingSettings::new)
	);
	private final double xzScale;
	private final double yScale;
	private final double xzFactor;
	private final double yFactor;

	public NoiseSamplingSettings(double d, double e, double f, double g) {
		this.xzScale = d;
		this.yScale = e;
		this.xzFactor = f;
		this.yFactor = g;
	}

	public double xzScale() {
		return this.xzScale;
	}

	public double yScale() {
		return this.yScale;
	}

	public double xzFactor() {
		return this.xzFactor;
	}

	public double yFactor() {
		return this.yFactor;
	}
}
