package net.minecraft.data.loot.packs;

import java.util.Set;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyOneBlockLoot extends BlockLootSubProvider {
	protected UpdateOneTwentyOneBlockLoot() {
		super(Set.of(), FeatureFlagSet.of(FeatureFlags.UPDATE_1_21));
	}

	@Override
	protected void generate() {
		this.dropSelf(Blocks.CRAFTER);
	}
}
