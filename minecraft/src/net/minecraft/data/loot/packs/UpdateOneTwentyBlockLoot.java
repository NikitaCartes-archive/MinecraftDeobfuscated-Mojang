package net.minecraft.data.loot.packs;

import java.util.Set;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TorchflowerCropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;

public class UpdateOneTwentyBlockLoot extends BlockLootSubProvider {
	protected UpdateOneTwentyBlockLoot() {
		super(Set.of(), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
	}

	@Override
	protected void generate() {
		this.dropSelf(Blocks.BAMBOO_BLOCK);
		this.dropSelf(Blocks.STRIPPED_BAMBOO_BLOCK);
		this.dropSelf(Blocks.BAMBOO_PLANKS);
		this.dropSelf(Blocks.BAMBOO_MOSAIC);
		this.dropSelf(Blocks.BAMBOO_STAIRS);
		this.dropSelf(Blocks.BAMBOO_MOSAIC_STAIRS);
		this.dropSelf(Blocks.BAMBOO_SIGN);
		this.dropSelf(Blocks.OAK_HANGING_SIGN);
		this.dropSelf(Blocks.SPRUCE_HANGING_SIGN);
		this.dropSelf(Blocks.BIRCH_HANGING_SIGN);
		this.dropSelf(Blocks.ACACIA_HANGING_SIGN);
		this.dropSelf(Blocks.CHERRY_HANGING_SIGN);
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
		this.add(Blocks.DECORATED_POT, noDrop());
		this.dropSelf(Blocks.PIGLIN_HEAD);
		this.dropSelf(Blocks.TORCHFLOWER);
		this.dropPottedContents(Blocks.POTTED_TORCHFLOWER);
		LootItemBlockStatePropertyCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TORCHFLOWER_CROP)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TorchflowerCropBlock.AGE, 2));
		this.add(
			Blocks.TORCHFLOWER_CROP,
			this.applyExplosionDecay(
				Blocks.TORCHFLOWER_CROP,
				LootTable.lootTable()
					.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.TORCHFLOWER).when(builder).otherwise(LootItem.lootTableItem(Items.TORCHFLOWER_SEEDS))))
			)
		);
		this.dropSelf(Blocks.CHERRY_PLANKS);
		this.dropSelf(Blocks.CHERRY_SAPLING);
		this.dropSelf(Blocks.CHERRY_LOG);
		this.dropSelf(Blocks.STRIPPED_CHERRY_LOG);
		this.dropSelf(Blocks.CHERRY_WOOD);
		this.dropSelf(Blocks.STRIPPED_CHERRY_WOOD);
		this.dropSelf(Blocks.CHERRY_SIGN);
		this.dropSelf(Blocks.CHERRY_PRESSURE_PLATE);
		this.dropSelf(Blocks.CHERRY_TRAPDOOR);
		this.dropSelf(Blocks.CHERRY_BUTTON);
		this.dropSelf(Blocks.CHERRY_STAIRS);
		this.dropSelf(Blocks.CHERRY_FENCE_GATE);
		this.dropSelf(Blocks.CHERRY_FENCE);
		this.dropPottedContents(Blocks.POTTED_CHERRY_SAPLING);
		this.add(Blocks.CHERRY_SLAB, block -> this.createSlabItemTable(block));
		this.add(Blocks.CHERRY_DOOR, block -> this.createDoorTable(block));
		this.add(Blocks.CHERRY_LEAVES, block -> this.createLeavesDrops(block, Blocks.CHERRY_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.PINK_PETALS, this.createPetalsDrops(Blocks.PINK_PETALS));
		this.add(Blocks.SUSPICIOUS_SAND, noDrop());
	}
}
