package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.DecorationContext;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class DecoratedFeature extends Feature<DecoratedFeatureConfiguration> {
	public DecoratedFeature(Codec<DecoratedFeatureConfiguration> codec) {
		super(codec);
	}

	public boolean place(
		WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, Random random, BlockPos blockPos, DecoratedFeatureConfiguration decoratedFeatureConfiguration
	) {
		MutableBoolean mutableBoolean = new MutableBoolean();
		decoratedFeatureConfiguration.decorator.getPositions(new DecorationContext(worldGenLevel, chunkGenerator), random, blockPos).forEach(blockPosx -> {
			if (((ConfiguredFeature)decoratedFeatureConfiguration.feature.get()).place(worldGenLevel, chunkGenerator, random, blockPosx)) {
				mutableBoolean.setTrue();
			}
		});
		return mutableBoolean.isTrue();
	}

	public String toString() {
		return String.format("< %s [%s] >", this.getClass().getSimpleName(), Registry.FEATURE.getKey(this));
	}
}
