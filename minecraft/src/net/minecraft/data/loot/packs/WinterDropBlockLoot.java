package net.minecraft.data.loot.packs;

import java.util.Set;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Blocks;

public class WinterDropBlockLoot extends BlockLootSubProvider {
	public WinterDropBlockLoot(HolderLookup.Provider provider) {
		super(Set.of(), FeatureFlagSet.of(FeatureFlags.WINTER_DROP), provider);
	}

	@Override
	protected void generate() {
		this.dropSelf(Blocks.PALE_OAK_PLANKS);
		this.dropSelf(Blocks.PALE_OAK_SAPLING);
		this.dropSelf(Blocks.PALE_OAK_LOG);
		this.dropSelf(Blocks.STRIPPED_PALE_OAK_LOG);
		this.dropSelf(Blocks.PALE_OAK_WOOD);
		this.dropSelf(Blocks.STRIPPED_PALE_OAK_WOOD);
		this.dropSelf(Blocks.PALE_OAK_SIGN);
		this.dropSelf(Blocks.PALE_OAK_HANGING_SIGN);
		this.dropSelf(Blocks.PALE_OAK_PRESSURE_PLATE);
		this.dropSelf(Blocks.PALE_OAK_TRAPDOOR);
		this.dropSelf(Blocks.PALE_OAK_BUTTON);
		this.dropSelf(Blocks.PALE_OAK_STAIRS);
		this.dropSelf(Blocks.PALE_OAK_FENCE_GATE);
		this.dropSelf(Blocks.PALE_OAK_FENCE);
		this.add(Blocks.PALE_MOSS_CARPET, block -> this.createMossyCarpetBlockDrops(block));
		this.dropSelf(Blocks.PALE_HANGING_MOSS);
		this.dropSelf(Blocks.PALE_MOSS_BLOCK);
		this.dropPottedContents(Blocks.POTTED_PALE_OAK_SAPLING);
		this.add(Blocks.PALE_OAK_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.PALE_OAK_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.PALE_OAK_LEAVES, block -> this.createLeavesDrops(block, Blocks.PALE_OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.dropWhenSilkTouch(Blocks.CREAKING_HEART);
	}
}
