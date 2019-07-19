package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfig> {
	public SimpleRandomSelectorFeature(Function<Dynamic<?>, ? extends SimpleRandomFeatureConfig> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		SimpleRandomFeatureConfig simpleRandomFeatureConfig
	) {
		int i = random.nextInt(simpleRandomFeatureConfig.features.size());
		ConfiguredFeature<?> configuredFeature = (ConfiguredFeature<?>)simpleRandomFeatureConfig.features.get(i);
		return configuredFeature.place(levelAccessor, chunkGenerator, random, blockPos);
	}
}
