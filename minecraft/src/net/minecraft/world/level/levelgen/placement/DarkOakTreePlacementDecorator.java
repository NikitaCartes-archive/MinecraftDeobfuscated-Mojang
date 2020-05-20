package net.minecraft.world.level.levelgen.placement;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneDecoratorConfiguration;

public class DarkOakTreePlacementDecorator extends FeatureDecorator<NoneDecoratorConfiguration> {
	public DarkOakTreePlacementDecorator(Codec<NoneDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, NoneDecoratorConfiguration noneDecoratorConfiguration, BlockPos blockPos
	) {
		return IntStream.range(0, 16).mapToObj(i -> {
			int j = i / 4;
			int k = i % 4;
			int l = j * 4 + 1 + random.nextInt(3) + blockPos.getX();
			int m = k * 4 + 1 + random.nextInt(3) + blockPos.getZ();
			int n = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, l, m);
			return new BlockPos(l, n, m);
		});
	}
}
