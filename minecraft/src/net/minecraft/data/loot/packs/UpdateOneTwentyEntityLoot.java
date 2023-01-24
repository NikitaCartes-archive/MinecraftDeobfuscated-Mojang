package net.minecraft.data.loot.packs;

import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class UpdateOneTwentyEntityLoot extends EntityLootSubProvider {
	protected UpdateOneTwentyEntityLoot() {
		super(FeatureFlagSet.of(FeatureFlags.UPDATE_1_20, FeatureFlags.VANILLA), FeatureFlagSet.of(FeatureFlags.UPDATE_1_20));
	}

	@Override
	public void generate() {
		this.add(EntityType.CAMEL, LootTable.lootTable());
		this.add(
			EntityType.ELDER_GUARDIAN,
			VanillaEntityLoot.elderGuardianLootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(EmptyLootItem.emptyItem().setWeight(4))
						.add(LootItem.lootTableItem(Items.TIDE_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(1))
				)
		);
	}
}
