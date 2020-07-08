package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;

public class NoiseBasedDecorator extends SimpleFeatureDecorator<NoiseCountFactorDecoratorConfiguration> {
	public NoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> place(Random random, NoiseCountFactorDecoratorConfiguration noiseCountFactorDecoratorConfiguration, BlockPos blockPos) {
		double d = Biome.BIOME_INFO_NOISE
			.getValue(
				(double)blockPos.getX() / noiseCountFactorDecoratorConfiguration.noiseFactor,
				(double)blockPos.getZ() / noiseCountFactorDecoratorConfiguration.noiseFactor,
				false
			);
		int i = (int)Math.ceil((d + noiseCountFactorDecoratorConfiguration.noiseOffset) * (double)noiseCountFactorDecoratorConfiguration.noiseToCountRatio);
		return IntStream.range(0, i).mapToObj(ix -> blockPos);
	}
}
