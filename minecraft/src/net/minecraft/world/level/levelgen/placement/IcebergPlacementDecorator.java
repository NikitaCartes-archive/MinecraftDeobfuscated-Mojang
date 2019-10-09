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

public class IcebergPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
	public IcebergPlacementDecorator(Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		ChanceDecoratorConfiguration chanceDecoratorConfiguration,
		BlockPos blockPos
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
