package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountChanceHeightmapDoubleDecorator extends FeatureDecorator<FrequencyChanceDecoratorConfiguration> {
	public CountChanceHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends FrequencyChanceDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator chunkGenerator,
		Random random,
		FrequencyChanceDecoratorConfiguration frequencyChanceDecoratorConfiguration,
		BlockPos blockPos
	) {
		return IntStream.range(0, frequencyChanceDecoratorConfiguration.count)
			.filter(i -> random.nextFloat() < frequencyChanceDecoratorConfiguration.chance)
			.mapToObj(i -> {
				int j = random.nextInt(16) + blockPos.getX();
				int k = random.nextInt(16) + blockPos.getZ();
				int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k) * 2;
				return l <= 0 ? null : new BlockPos(j, random.nextInt(l), k);
			})
			.filter(Objects::nonNull);
	}
}
