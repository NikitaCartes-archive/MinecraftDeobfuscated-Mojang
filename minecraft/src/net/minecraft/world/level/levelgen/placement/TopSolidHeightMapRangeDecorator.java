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

public class TopSolidHeightMapRangeDecorator extends FeatureDecorator<DecoratorRange> {
	public TopSolidHeightMapRangeDecorator(Function<Dynamic<?>, ? extends DecoratorRange> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DecoratorRange decoratorRange, BlockPos blockPos
	) {
		int i = random.nextInt(decoratorRange.max - decoratorRange.min) + decoratorRange.min;
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16);
			int k = random.nextInt(16);
			int l = levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, blockPos.getX() + j, blockPos.getZ() + k);
			return new BlockPos(blockPos.getX() + j, l, blockPos.getZ() + k);
		});
	}
}
