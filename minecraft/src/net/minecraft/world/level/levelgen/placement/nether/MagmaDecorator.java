package net.minecraft.world.level.levelgen.placement.nether;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.DecoratorFrequency;
import net.minecraft.world.level.levelgen.placement.FeatureDecorator;

public class MagmaDecorator extends FeatureDecorator<DecoratorFrequency> {
	public MagmaDecorator(Function<Dynamic<?>, ? extends DecoratorFrequency> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorFrequency decoratorFrequency,
		BlockPos blockPos
	) {
		int i = levelAccessor.getSeaLevel() / 2 + 1;
		return IntStream.range(0, decoratorFrequency.count).mapToObj(j -> {
			int k = random.nextInt(16);
			int l = i - 5 + random.nextInt(10);
			int m = random.nextInt(16);
			return blockPos.offset(k, l, m);
		});
	}
}
