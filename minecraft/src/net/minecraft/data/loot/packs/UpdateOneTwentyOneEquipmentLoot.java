package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimMaterials;
import net.minecraft.world.item.armortrim.TrimPattern;
import net.minecraft.world.item.armortrim.TrimPatterns;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class UpdateOneTwentyOneEquipmentLoot implements LootTableSubProvider {
	@Override
	public void generate(HolderLookup.Provider provider, BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		HolderLookup.RegistryLookup<TrimPattern> registryLookup = (HolderLookup.RegistryLookup<TrimPattern>)provider.lookup(Registries.TRIM_PATTERN).orElseThrow();
		HolderLookup.RegistryLookup<TrimMaterial> registryLookup2 = (HolderLookup.RegistryLookup<TrimMaterial>)provider.lookup(Registries.TRIM_MATERIAL)
			.orElseThrow();
		ArmorTrim armorTrim = new ArmorTrim(
			(Holder<TrimMaterial>)registryLookup2.get(TrimMaterials.COPPER).orElseThrow(), (Holder<TrimPattern>)registryLookup.get(TrimPatterns.FLOW).orElseThrow()
		);
		ArmorTrim armorTrim2 = new ArmorTrim(
			(Holder<TrimMaterial>)registryLookup2.get(TrimMaterials.COPPER).orElseThrow(), (Holder<TrimPattern>)registryLookup.get(TrimPatterns.BOLT).orElseThrow()
		);
		biConsumer.accept(
			BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(NestedLootTable.inlineLootTable(trialChamberEquipment(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE, armorTrim2).build()).setWeight(4))
						.add(NestedLootTable.inlineLootTable(trialChamberEquipment(Items.IRON_HELMET, Items.IRON_CHESTPLATE, armorTrim).build()).setWeight(2))
						.add(NestedLootTable.inlineLootTable(trialChamberEquipment(Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, armorTrim).build()).setWeight(1))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_MELEE,
			LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(NestedLootTable.lootTableReference(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER)))
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.IRON_SWORD).setWeight(4))
						.add(
							LootItem.lootTableItem(Items.IRON_SWORD)
								.apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.SHARPNESS, ConstantValue.exactly(1.0F)))
						)
						.add(
							LootItem.lootTableItem(Items.IRON_SWORD)
								.apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.KNOCKBACK, ConstantValue.exactly(1.0F)))
						)
						.add(LootItem.lootTableItem(Items.DIAMOND_SWORD))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER_RANGED,
			LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(NestedLootTable.lootTableReference(BuiltInLootTables.EQUIPMENT_TRIAL_CHAMBER)))
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.BOW).setWeight(2))
						.add(LootItem.lootTableItem(Items.BOW).apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.POWER, ConstantValue.exactly(1.0F))))
						.add(LootItem.lootTableItem(Items.BOW).apply(new SetEnchantmentsFunction.Builder().withEnchantment(Enchantments.PUNCH, ConstantValue.exactly(1.0F))))
				)
		);
	}

	public static LootTable.Builder trialChamberEquipment(Item item, Item item2, ArmorTrim armorTrim) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.when(LootItemRandomChanceCondition.randomChance(0.5F))
					.add(
						LootItem.lootTableItem(item)
							.apply(SetComponentsFunction.setComponent(DataComponents.TRIM, armorTrim))
							.apply(
								new SetEnchantmentsFunction.Builder()
									.withEnchantment(Enchantments.PROTECTION, ConstantValue.exactly(4.0F))
									.withEnchantment(Enchantments.PROJECTILE_PROTECTION, ConstantValue.exactly(4.0F))
									.withEnchantment(Enchantments.FIRE_PROTECTION, ConstantValue.exactly(4.0F))
							)
					)
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.when(LootItemRandomChanceCondition.randomChance(0.5F))
					.add(
						LootItem.lootTableItem(item2)
							.apply(SetComponentsFunction.setComponent(DataComponents.TRIM, armorTrim))
							.apply(
								new SetEnchantmentsFunction.Builder()
									.withEnchantment(Enchantments.PROTECTION, ConstantValue.exactly(4.0F))
									.withEnchantment(Enchantments.PROJECTILE_PROTECTION, ConstantValue.exactly(4.0F))
									.withEnchantment(Enchantments.FIRE_PROTECTION, ConstantValue.exactly(4.0F))
							)
					)
			);
	}
}
