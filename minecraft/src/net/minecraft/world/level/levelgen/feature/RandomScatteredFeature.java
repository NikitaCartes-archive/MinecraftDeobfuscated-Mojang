package net.minecraft.world.level.levelgen.feature;

import com.mojang.datafixers.Dynamic;
import java.util.function.Function;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public abstract class RandomScatteredFeature<C extends FeatureConfiguration> extends StructureFeature<C> {
	public RandomScatteredFeature(Function<Dynamic<?>, ? extends C> function) {
		super(function);
	}

	@Override
	protected int getSpacing(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getTemplesSpacing();
	}

	@Override
	protected int getSeparation(DimensionType dimensionType, ChunkGeneratorSettings chunkGeneratorSettings) {
		return chunkGeneratorSettings.getTemplesSeparation();
	}

	@Override
	protected abstract int getRandomSalt(ChunkGeneratorSettings chunkGeneratorSettings);
}
