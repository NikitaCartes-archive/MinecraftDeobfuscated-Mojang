package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChanceHeightmapDecorator extends FeatureDecorator<DecoratorChance> {
	public ChanceHeightmapDecorator(Function<Dynamic<?>, ? extends DecoratorChance> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorChance decoratorChance,
		BlockPos blockPos
	) {
		if (random.nextFloat() < 1.0F / (float)decoratorChance.chance) {
			int i = random.nextInt(16);
			int j = random.nextInt(16);
			BlockPos blockPos2 = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(i, 0, j));
			return Stream.of(blockPos2);
		} else {
			return Stream.empty();
		}
	}
}
