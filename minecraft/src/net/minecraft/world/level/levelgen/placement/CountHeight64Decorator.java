package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class CountHeight64Decorator extends FeatureDecorator<DecoratorFrequency> {
	public CountHeight64Decorator(Function<Dynamic<?>, ? extends DecoratorFrequency> function) {
		super(function);
	}

	public Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		DecoratorFrequency decoratorFrequency,
		BlockPos blockPos
	) {
		return IntStream.range(0, decoratorFrequency.count).mapToObj(i -> {
			int j = random.nextInt(16);
			int k = 64;
			int l = random.nextInt(16);
			return blockPos.offset(j, 64, l);
		});
	}
}
