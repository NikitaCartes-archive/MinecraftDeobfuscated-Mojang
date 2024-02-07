package net.minecraft.data.loot.packs;

import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class UpdateOneTwentyOneEntityLoot extends EntityLootSubProvider {
	protected UpdateOneTwentyOneEntityLoot() {
		super(FeatureFlagSet.of(FeatureFlags.UPDATE_1_21));
	}

	@Override
	public void generate() {
		this.add(
			EntityType.BREEZE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.WIND_CHARGE).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 6.0F))))
						.when(LootItemKilledByPlayerCondition.killedByPlayer())
				)
		);
	}
}
