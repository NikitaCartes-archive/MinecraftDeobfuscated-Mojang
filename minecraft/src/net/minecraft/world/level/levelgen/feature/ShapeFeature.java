package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;

public class ShapeFeature extends Feature<ShapeConfiguration> {
	public ShapeFeature(Function<Dynamic<?>, ? extends ShapeConfiguration> function, Function<Random, ? extends ShapeConfiguration> function2) {
		super(function, function2);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		ShapeConfiguration shapeConfiguration
	) {
		float f = Mth.lerp(random.nextFloat(), shapeConfiguration.radiusMin, shapeConfiguration.radiusMax);
		int i = Mth.ceil(f);
		BlockPos.betweenClosedStream(blockPos.offset(-i, -i, -i), blockPos.offset(i, i, i))
			.filter(blockPos2 -> shapeConfiguration.metric.distance(blockPos2, blockPos) < f)
			.forEach(blockPosx -> this.setBlock(levelAccessor, blockPosx, shapeConfiguration.material.getState(random, blockPosx)));
		return true;
	}
}
