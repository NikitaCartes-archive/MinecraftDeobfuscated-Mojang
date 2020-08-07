package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public class BlockPileConfiguration implements FeatureConfiguration {
	public static final Codec<BlockPileConfiguration> CODEC = BlockStateProvider.CODEC
		.fieldOf("state_provider")
		.<BlockPileConfiguration>xmap(BlockPileConfiguration::new, blockPileConfiguration -> blockPileConfiguration.stateProvider)
		.codec();
	public final BlockStateProvider stateProvider;

	public BlockPileConfiguration(BlockStateProvider blockStateProvider) {
		this.stateProvider = blockStateProvider;
	}
}
