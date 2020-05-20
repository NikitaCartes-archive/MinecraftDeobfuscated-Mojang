package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.serialization.Codec;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;
import net.minecraft.world.level.levelgen.placement.FrequencyDecoratorConfiguration;

public class MagmaDecorator extends FeatureDecorator<FrequencyDecoratorConfiguration> {
	public MagmaDecorator(Codec<FrequencyDecoratorConfiguration> codec) {
		super(codec);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator chunkGenerator, Random random, FrequencyDecoratorConfiguration frequencyDecoratorConfiguration, BlockPos blockPos
	) {
		int i = levelAccessor.getSeaLevel() / 2 + 1;
		return IntStream.range(0, frequencyDecoratorConfiguration.count).mapToObj(j -> {
			int k = random.nextInt(16) + blockPos.getX();
			int l = random.nextInt(16) + blockPos.getZ();
			int m = i - 5 + random.nextInt(10);
			return new BlockPos(k, m, l);
		});
	}
}
