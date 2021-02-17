package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.configurations.NoiseDependantDecoratorConfiguration;

public class CountNoiseDecorator extends RepeatingDecorator<NoiseDependantDecoratorConfiguration> {
	public CountNoiseDecorator(Codec<NoiseDependantDecoratorConfiguration> codec) {
		super(codec);
	}

	protected int count(Random random, NoiseDependantDecoratorConfiguration noiseDependantDecoratorConfiguration, BlockPos blockPos) {
		double d = Biome.BIOME_INFO_NOISE.getValue((double)blockPos.getX() / 200.0, (double)blockPos.getZ() / 200.0, false);
		return d < noiseDependantDecoratorConfiguration.noiseLevel
			? noiseDependantDecoratorConfiguration.belowNoise
			: noiseDependantDecoratorConfiguration.aboveNoise;
	}
}
