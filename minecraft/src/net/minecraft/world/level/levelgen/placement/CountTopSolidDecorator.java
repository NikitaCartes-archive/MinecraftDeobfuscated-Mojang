package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountTopSolidDecorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
	public CountTopSolidDecorator(Codec<FrequencyDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos
	) {
		return IntStream.range(0, frequencyDecoratorConfiguration.count).mapToObj(i -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, j, k);
			return new BlockPos(j, l, k);
		});
	}
}
