package net.minecraft.data.loot.packs;

import java.util.Set;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Blocks;

public class UpdateOneTwentyBlockLoot extends BlockLootSubProvider {
	protected UpdateOneTwentyBlockLoot() {
		super(Set.of(), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
	}

	@Override
	protected void generate() {
		this.dropSelf(Blocks.BAMBOO_PLANKS);
		this.dropSelf(Blocks.BAMBOO_MOSAIC);
		this.dropSelf(Blocks.BAMBOO_STAIRS);
		this.dropSelf(Blocks.BAMBOO_MOSAIC_STAIRS);
		this.dropSelf(Blocks.BAMBOO_SIGN);
		this.dropSelf(Blocks.OAK_HANGING_SIGN);
		this.dropSelf(Blocks.SPRUCE_HANGING_SIGN);
		this.dropSelf(Blocks.BIRCH_HANGING_SIGN);
		this.dropSelf(Blocks.ACACIA_HANGING_SIGN);
		this.dropSelf(Blocks.JUNGLE_HANGING_SIGN);
		this.dropSelf(Blocks.DARK_OAK_HANGING_SIGN);
		this.dropSelf(Blocks.MANGROVE_HANGING_SIGN);
		this.dropSelf(Blocks.CRIMSON_HANGING_SIGN);
		this.dropSelf(Blocks.WARPED_HANGING_SIGN);
		this.dropSelf(Blocks.BAMBOO_HANGING_SIGN);
		this.dropSelf(Blocks.BAMBOO_PRESSURE_PLATE);
		this.dropSelf(Blocks.BAMBOO_FENCE);
		this.dropSelf(Blocks.BAMBOO_TRAPDOOR);
		this.dropSelf(Blocks.BAMBOO_FENCE_GATE);
		this.dropSelf(Blocks.BAMBOO_BUTTON);
		this.add(Blocks.BAMBOO_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BAMBOO_MOSAIC_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.BAMBOO_DOOR, block -> this.createDoorTable(block));
		this.dropWhenSilkTouch(Blocks.CHISELED_BOOKSHELF);
	}
}
