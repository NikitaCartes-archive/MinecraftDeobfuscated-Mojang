package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountChanceHeightmapDecorator extends FeatureDecorator<FrequencyChanceDecoratorConfiguration> {
	public CountChanceHeightmapDecorator(Function<Dynamic<?>, ? extends FrequencyChanceDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		FrequencyChanceDecoratorConfiguration frequencyChanceDecoratorConfiguration,
		BlockPos blockPos
	) {
		return IntStream.range(0, frequencyChanceDecoratorConfiguration.count)
			.filter(i -> random.nextFloat() < frequencyChanceDecoratorConfiguration.chance)
			.mapToObj(i -> {
				int j = random.nextInt(16) + blockPos.getX();
				int k = random.nextInt(16) + blockPos.getZ();
				int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k);
				return new BlockPos(j, l, k);
			});
	}
}
