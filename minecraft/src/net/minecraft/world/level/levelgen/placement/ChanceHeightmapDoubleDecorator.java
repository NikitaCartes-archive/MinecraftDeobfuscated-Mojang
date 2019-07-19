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

public class ChanceHeightmapDoubleDecorator extends FeatureDecorator<DecoratorChance> {
	public ChanceHeightmapDoubleDecorator(Function<Dynamic<?>, ? extends DecoratorChance> function) {
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
			int k = levelAccessor.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos.offset(i, 0, j)).getY() * 2;
			if (k <= 0) {
				return Stream.empty();
			} else {
				int l = random.nextInt(k);
				return Stream.of(blockPos.offset(i, l, j));
			}
		} else {
			return Stream.empty();
		}
	}
}
