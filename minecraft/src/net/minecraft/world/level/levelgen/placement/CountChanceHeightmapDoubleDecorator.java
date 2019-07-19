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
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public class CountChanceHeightmapDoubleDecorator extends FeatureDecorator<DecoratorFrequencyChance> {
	public CountChanceHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends DecoratorFrequencyChance> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorFrequencyChance decoratorFrequencyChance,
		BlockPos blockPos
	) {
		return IntStream.range(0, decoratorFrequencyChance.count).filter(i -> random.nextFloat() < decoratorFrequencyChance.chance).mapToObj(i -> {
			int j = random.nextInt(16);
			int k = random.nextInt(16);
			int l = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(j, 0, k)).getY() * 2;
			if (l <= 0) {
				return null;
			} else {
				int m = random.nextInt(l);
				return blockPos.offset(j, m, k);
			}
		}).filter(Objects::nonNull);
	}
}
