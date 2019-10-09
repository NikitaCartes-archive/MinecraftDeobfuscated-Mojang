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

public class CountHeighmapDoubleDecorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
	public CountHeighmapDoubleDecorator(Function<Dynamic<?>, ? extends FrequencyDecoratorConfiguration> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		FrequencyDecoratorConfiguration frequencyDecoratorConfiguration,
		BlockPos blockPos
	) {
		return IntStream.range(0, frequencyDecoratorConfiguration.count).mapToObj(i -> {
			int j = random.nextInt(16) + blockPos.getX();
			int k = random.nextInt(16) + blockPos.getZ();
			int l = levelAccessor.getHeight(Heightmap.Types.MOTION_BLOCKING, j, k) * 2;
			return l <= 0 ? null : new BlockPos(j, random.nextInt(l), k);
		}).filter(Objects::nonNull);
	}
}
