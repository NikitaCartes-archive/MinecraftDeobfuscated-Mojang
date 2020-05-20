package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChanceHeightmapDoubleDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
	public ChanceHeightmapDoubleDecorator(Codec<ChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos
	) {
		if (random.nextFloat() < 1.0F / (float)chanceDecoratorConfiguration.chance) {
			int i = random.nextInt(16) + blockPos.getX();
			int j = random.nextInt(16) + blockPos.getZ();
			int k = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j) * 2;
			return k <= 0 ? Stream.empty() : Stream.of(new BlockPos(i, random.nextInt(k), j));
		} else {
			return Stream.empty();
		}
	}
}
