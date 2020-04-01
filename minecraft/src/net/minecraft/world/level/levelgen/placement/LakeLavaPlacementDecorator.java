package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class LakeLavaPlacementDecorator extends FeatureDecorator<ChanceDecoratorConfiguration> {
	public LakeLavaPlacementDecorator(
		Function<Dynamic<?>, ? extends ChanceDecoratorConfiguration> function, Function<Random, ? extends ChanceDecoratorConfiguration> function2
	) {
		super(function, function2);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		ChanceDecoratorConfiguration chanceDecoratorConfiguration,
		BlockPos blockPos
	) {
		if (random.nextInt(chanceDecoratorConfiguration.chance / 10) == 0) {
			int i = random.nextInt(16) + blockPos.getX();
			int j = random.nextInt(16) + blockPos.getZ();
			int k = random.nextInt(random.nextInt(chunkGenerator.getGenDepth() - 8) + 8);
			if (k < levelAccessor.getSeaLevel() || random.nextInt(chanceDecoratorConfiguration.chance / 8) == 0) {
				return Stream.of(new BlockPos(i, k, j));
			}
		}

		return Stream.empty();
	}
}
