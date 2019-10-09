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
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class ChorusPlantPlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
	public ChorusPlantPlacementDecorator(Function<Dynamic<?>, ? extends NoneDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		NoneDecoratorConfiguration noneDecoratorConfiguration,
		BlockPos blockPos
	) {
		int i = random.nextInt(5);
		return IntStream.range(0, i).mapToObj(ix -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k);
			if (l > 0) {
				int m = l - 1;
				return new BlockPos(j, m, k);
			} else {
				return null;
			}
		}).filter(Objects::nonNull);
	}
}
