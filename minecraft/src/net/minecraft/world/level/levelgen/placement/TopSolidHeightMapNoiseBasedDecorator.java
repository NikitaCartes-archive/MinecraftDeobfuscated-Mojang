package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class TopSolidHeightMapNoiseBasedDecorator extends FeatureDecorator<DecoratorNoiseCountFactor> {
	public TopSolidHeightMapNoiseBasedDecorator(Function<Dynamic<?>, ? extends DecoratorNoiseCountFactor> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorNoiseCountFactor decoratorNoiseCountFactor,
		BlockPos blockPos
	) {
		double d = Biome.BIOME_INFO_NOISE
			.getValue((double)blockPos.getX() / decoratorNoiseCountFactor.noiseFactor, (double)blockPos.getZ() / decoratorNoiseCountFactor.noiseFactor);
		int i = (int)Math.ceil((d + decoratorNoiseCountFactor.noiseOffset) * (double)decoratorNoiseCountFactor.noiseToCountRatio);
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16);
			int k = random.nextInt(16);
			int l = levelAccessor.getHeight(decoratorNoiseCountFactor.heightmap, blockPos.getX() + j, blockPos.getZ() + k);
			return new BlockPos(blockPos.getX() + j, l, blockPos.getZ() + k);
		});
	}
}
