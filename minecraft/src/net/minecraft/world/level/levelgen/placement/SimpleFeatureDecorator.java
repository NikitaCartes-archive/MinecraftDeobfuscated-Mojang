package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.DecoratorConfiguration;

public abstract class SimpleFeatureDecorator<DC extends DecoratorConfiguration> extends FeatureDecorator<DC> {
	public SimpleFeatureDecorator(Function<Dynamic<?>, ? extends DC> function) {
		super(function);
	}

	@Override
	public final Stream<BlockPos> getPositions(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, DC decoratorConfiguration, BlockPos blockPos
	) {
		return this.place(random, decoratorConfiguration, blockPos);
	}

	protected abstract Stream<BlockPos> place(Random random, DC decoratorConfiguration, BlockPos blockPos);
}
