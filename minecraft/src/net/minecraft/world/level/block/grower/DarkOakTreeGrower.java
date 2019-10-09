package net.minecraft.world.level.block.grower;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.world.level.biome.BiomeDefaultFeatures;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;

public class DarkOakTreeGrower extends AbstractMegaTreeGrower {
	@Nullable
	@Override
	protected ConfiguredFeature<SmallTreeConfiguration, ?> getConfiguredFeature(Random random) {
		return null;
	}

	@Nullable
	@Override
	protected ConfiguredFeature<MegaTreeConfiguration, ?> getConfiguredMegaFeature(Random random) {
		return Feature.DARK_OAK_TREE.configured(BiomeDefaultFeatures.DARK_OAK_TREE_CONFIG);
	}
}
