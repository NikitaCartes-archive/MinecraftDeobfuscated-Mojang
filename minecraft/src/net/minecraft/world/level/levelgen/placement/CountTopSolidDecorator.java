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

public class CountTopSolidDecorator extends FeatureDecorator<DecoratorFrequency> {
	public CountTopSolidDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorFrequency decoratorFrequency,
		BlockPos blockPos
	) {
		return IntStream.range(0, decoratorFrequency.count).mapToObj(i -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			return new BlockPos(j, levelAccessor.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, j, k), k);
		});
	}
}
