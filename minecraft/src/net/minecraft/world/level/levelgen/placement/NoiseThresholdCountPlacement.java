package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseThresholdCountPlacement extends RepeatingPlacement {
	public static final Codec<NoiseThresholdCountPlacement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.DOUBLE.fieldOf("noise_level").forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.noiseLevel),
					Codec.INT.fieldOf("below_noise").forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.belowNoise),
					Codec.INT.fieldOf("above_noise").forGetter(noiseThresholdCountPlacement -> noiseThresholdCountPlacement.aboveNoise)
				)
				.apply(instance, NoiseThresholdCountPlacement::new)
	);
	private final double noiseLevel;
	private final int belowNoise;
	private final int aboveNoise;

	private NoiseThresholdCountPlacement(double d, int i, int j) {
		this.noiseLevel = d;
		this.belowNoise = i;
		this.aboveNoise = j;
	}

	public static NoiseThresholdCountPlacement of(double d, int i, int j) {
		return new NoiseThresholdCountPlacement(d, i, j);
	}

	@Override
	protected int count(Random random, BlockPos blockPos) {
		double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
		return d < this.noiseLevel ? this.belowNoise : this.aboveNoise;
	}

	@Override
	public PlacementModifierType<?> type() {
		return PlacementModifierType.NOISE_THRESHOLD_COUNT;
	}
}
