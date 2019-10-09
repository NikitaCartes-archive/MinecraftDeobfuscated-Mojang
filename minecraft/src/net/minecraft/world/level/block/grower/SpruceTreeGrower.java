package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class SpruceTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random) {
		return Feature.NORMAL_TREE.configured(BiomeDefaultFeatures.SPRUCE_TREE_CONFIG);
	}

	@Nullable
	@Override
	protected ConfiguredFeature<MegaTreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
		return Feature.MEGA_SPRUCE_TREE.configured(random.nextBoolean() ? BiomeDefaultFeatures.MEGA_SPRUCE_TREE_CONFIG : BiomeDefaultFeatures.MEGA_PINE_TREE_CONFIG);
	}
}
