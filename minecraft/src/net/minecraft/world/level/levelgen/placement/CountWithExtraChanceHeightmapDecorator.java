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

public class CountWithExtraChanceHeightmapDecorator extends FeatureDecorator<DecoratorFrequencyWithExtraChance> {
	public CountWithExtraChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorFrequencyWithExtraChance> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorFrequencyWithExtraChance decoratorFrequencyWithExtraChance,
		BlockPos blockPos
	) {
		int i = decoratorFrequencyWithExtraChance.count;
		if (random.nextFloat() < decoratorFrequencyWithExtraChance.extraChance) {
			i += decoratorFrequencyWithExtraChance.extraCount;
		}

		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16);
			int k = random.nextInt(16);
			return levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k));
		});
	}
}
