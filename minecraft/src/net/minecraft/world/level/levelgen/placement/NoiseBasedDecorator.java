package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedDecorator extends RepeatingDecorator<NoiseCountFactorDecoratorConfiguration> {
	public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int count(Random random, NoiseCountFactorDecoratorConfiguration noiseCountFactorDecoratorConfiguration, BlockPos blockPos) {
		double d = Biome.BIOME_INFO_NOISE
			.getValue(
				(double)blockPos.getX() / noiseCountFactorDecoratorConfiguration.noiseFactor,
				(double)blockPos.getZ() / noiseCountFactorDecoratorConfiguration.noiseFactor,
				false
			);
		return (int)Math.ceil((d + noiseCountFactorDecoratorConfiguration.noiseOffset) * (double)noiseCountFactorDecoratorConfiguration.noiseToCountRatio);
	}
}
