package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountWithExtraChanceHeightmapDecorator extends FeatureDecorator<FrequencyWithExtraChanceDecoratorConfiguration> {
	public CountWithExtraChanceHeightmapDecorator(Function<Dynamic<?>, ? extends FrequencyWithExtraChanceDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator chunkGenerator,
		Random random,
		FrequencyWithExtraChanceDecoratorConfiguration frequencyWithExtraChanceDecoratorConfiguration,
		BlockPos blockPos
	) {
		int i = frequencyWithExtraChanceDecoratorConfiguration.count;
		if (random.nextFloat() < frequencyWithExtraChanceDecoratorConfiguration.extraChance) {
			i += frequencyWithExtraChanceDecoratorConfiguration.extraCount;
		}

		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k);
			return new BlockPos(j, l, k);
		});
	}
}
