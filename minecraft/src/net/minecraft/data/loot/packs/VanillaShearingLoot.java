package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.EntitySubPredicates;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public record VanillaShearingLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		biConsumer.accept(
			BuiltInLootTables.BOGGED_SHEAR,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(2.0F))
						.add(LootItem.lootTableItem(Items.BROWN_MUSHROOM).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
						.add(LootItem.lootTableItem(Items.RED_MUSHROOM).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
				)
		);
		LootData.WOOL_ITEM_BY_DYE
			.forEach(
				(dyeColor, itemLike) -> biConsumer.accept(
						(ResourceKey)BuiltInLootTables.SHEAR_SHEEP_BY_DYE.get(dyeColor),
						LootTable.lootTable().withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1.0F, 3.0F)).add(LootItem.lootTableItem(itemLike)))
					)
			);
		biConsumer.accept(
			BuiltInLootTables.SHEAR_SHEEP, LootTable.lootTable().withPool(EntityLootSubProvider.createSheepDispatchPool(BuiltInLootTables.SHEAR_SHEEP_BY_DYE))
		);
		biConsumer.accept(
			BuiltInLootTables.SHEAR_MOOSHROOM,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.add(
							AlternativesEntry.alternatives(
								NestedLootTable.lootTableReference(BuiltInLootTables.SHEAR_RED_MOOSHROOM)
									.when(
										LootItemEntityPropertyCondition.hasProperties(
											LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(EntitySubPredicates.MOOSHROOM.createPredicate(MushroomCow.Variant.RED))
										)
									),
								NestedLootTable.lootTableReference(BuiltInLootTables.SHEAR_BROWN_MOOSHROOM)
									.when(
										LootItemEntityPropertyCondition.hasProperties(
											LootContext.EntityTarget.THIS,
											EntityPredicate.Builder.entity().subPredicate(EntitySubPredicates.MOOSHROOM.createPredicate(MushroomCow.Variant.BROWN))
										)
									)
							)
						)
				)
		);
		biConsumer.accept(
			BuiltInLootTables.SHEAR_RED_MOOSHROOM,
			LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(5.0F)).add(LootItem.lootTableItem(Items.RED_MUSHROOM)))
		);
		biConsumer.accept(
			BuiltInLootTables.SHEAR_BROWN_MOOSHROOM,
			LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(5.0F)).add(LootItem.lootTableItem(Items.BROWN_MUSHROOM)))
		);
		biConsumer.accept(
			BuiltInLootTables.SHEAR_SNOW_GOLEM,
			LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Items.CARVED_PUMPKIN)))
		);
	}
}
