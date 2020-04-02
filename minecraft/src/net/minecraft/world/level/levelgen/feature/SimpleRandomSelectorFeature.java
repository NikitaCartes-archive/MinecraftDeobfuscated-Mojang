package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;

public class SimpleRandomSelectorFeature extends Feature<SimpleRandomFeatureConfiguration> {
	public SimpleRandomSelectorFeature(Function<Dynamic<?>, ? extends SimpleRandomFeatureConfiguration> function) {
		super(function);
	}

	public boolean place(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator,
		Random random,
		BlockPos blockPos,
		SimpleRandomFeatureConfiguration simpleRandomFeatureConfiguration
	) {
		int i = random.nextInt(simpleRandomFeatureConfiguration.features.size());
		ConfiguredFeature<?, ?> configuredFeature = (ConfiguredFeature<?, ?>)simpleRandomFeatureConfiguration.features.get(i);
		return configuredFeature.place(levelAccessor, structureFeatureManager, chunkGenerator, random, blockPos);
	}
}
