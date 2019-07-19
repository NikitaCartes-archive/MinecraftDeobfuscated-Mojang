package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class LakeWaterPlacementDecorator extends FeatureDecorator<LakeChanceDecoratorConfig> {
	public LakeWaterPlacementDecorator(Function<Dynamic<?>, ? extends LakeChanceDecoratorConfig> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		LakeChanceDecoratorConfig lakeChanceDecoratorConfig,
		BlockPos blockPos
	) {
		if (random.nextInt(lakeChanceDecoratorConfig.chance) == 0) {
			int i = random.nextInt(16);
			int j = random.nextInt(chunkGenerator.getGenDepth());
			int k = random.nextInt(16);
			return Stream.of(blockPos.offset(i, j, k));
		} else {
			return Stream.empty();
		}
	}
}
