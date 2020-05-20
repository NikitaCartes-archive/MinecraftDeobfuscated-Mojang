package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class IcebergPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
	public IcebergPlacementDecorator(Codec<ChanceDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, ChanceDecoratorConfiguration chanceDecoratorConfiguration, BlockPos blockPos
	) {
		if (random.nextFloat() < 1.0F / (float)chanceDecoratorConfiguration.chance) {
			int i = random.nextInt(8) + 4 + blockPos.getX();
			int j = random.nextInt(8) + 4 + blockPos.getZ();
			int k = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, i, j);
			return Stream.of(new BlockPos(i, k, j));
		} else {
			return Stream.empty();
		}
	}
}
