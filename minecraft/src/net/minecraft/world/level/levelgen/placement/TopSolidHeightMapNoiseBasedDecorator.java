package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class TopSolidHeightMapNoiseBasedDecorator extends FeatureDecorator<NoiseCountFactorDecoratorConfiguration> {
	public TopSolidHeightMapNoiseBasedDecorator(Codec<NoiseCountFactorDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator chunkGenerator,
		Random random,
		NoiseCountFactorDecoratorConfiguration noiseCountFactorDecoratorConfiguration,
		BlockPos blockPos
	) {
		double d = Biome.BIOME_INFO_NOISE
			.getValue(
				(double)blockPos.getX() / noiseCountFactorDecoratorConfiguration.noiseFactor,
				(double)blockPos.getZ() / noiseCountFactorDecoratorConfiguration.noiseFactor,
				false
			);
		int i = (int)Math.ceil((d + noiseCountFactorDecoratorConfiguration.noiseOffset) * (double)noiseCountFactorDecoratorConfiguration.noiseToCountRatio);
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = levelAccessor.getHeight(noiseCountFactorDecoratorConfiguration.heightmap, j, k);
			return new BlockPos(j, l, k);
		});
	}
}
