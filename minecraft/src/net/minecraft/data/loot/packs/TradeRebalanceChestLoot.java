package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetInstrumentFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public record TradeRebalanceChestLoot(HolderLookup.Provider registries) implements LootTableSubProvider {
	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> biConsumer) {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
		biConsumer.accept(
			BuiltInLootTables.ABANDONED_MINESHAFT,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(20))
						.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE))
						.add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(30))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries)))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE).setWeight(5))
						.add(EmptyLootItem.emptyItem().setWeight(5))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(UniformGenerator.between(2.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.GLOW_BERRIES).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(3.0F))
						.add(LootItem.lootTableItem(Blocks.RAIL).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.POWERED_RAIL).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.DETECTOR_RAIL).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.ACTIVATOR_RAIL).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.TORCH).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 16.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(EmptyLootItem.emptyItem().setWeight(4))
						.add(
							LootItem.lootTableItem(Items.BOOK)
								.setWeight(1)
								.apply(new EnchantRandomlyFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.EFFICIENCY)))
						)
				)
		);
		biConsumer.accept(BuiltInLootTables.ANCIENT_CITY, this.ancientCityLootTable());
		biConsumer.accept(BuiltInLootTables.DESERT_PYRAMID, this.desertPyramidLootTable());
		biConsumer.accept(BuiltInLootTables.JUNGLE_TEMPLE, this.jungleTempleLootTable());
		biConsumer.accept(BuiltInLootTables.PILLAGER_OUTPOST, this.pillagerOutpostLootTable());
	}

	public LootTable.Builder pillagerOutpostLootTable() {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().setRolls(UniformGenerator.between(0.0F, 1.0F)).add(LootItem.lootTableItem(Items.CROSSBOW)))
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(2.0F, 3.0F))
					.add(LootItem.lootTableItem(Items.WHEAT).setWeight(7).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 5.0F))))
					.add(LootItem.lootTableItem(Items.POTATO).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
					.add(LootItem.lootTableItem(Items.CARROT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 5.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(1.0F, 3.0F))
					.add(LootItem.lootTableItem(Blocks.DARK_OAK_LOG).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 3.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(2.0F, 3.0F))
					.add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(7))
					.add(LootItem.lootTableItem(Items.STRING).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 6.0F))))
					.add(LootItem.lootTableItem(Items.ARROW).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 7.0F))))
					.add(LootItem.lootTableItem(Items.TRIPWIRE_HOOK).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.BOOK).setWeight(1).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries)))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(0.0F, 1.0F))
					.add(LootItem.lootTableItem(Items.GOAT_HORN))
					.apply(SetInstrumentFunction.setInstrumentOptions(InstrumentTags.REGULAR_GOAT_HORNS))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(EmptyLootItem.emptyItem().setWeight(3))
					.add(LootItem.lootTableItem(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(1).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(EmptyLootItem.emptyItem().setWeight(1))
					.add(
						LootItem.lootTableItem(Items.BOOK)
							.setWeight(2)
							.apply(new EnchantRandomlyFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.QUICK_CHARGE)))
					)
			);
	}

	public LootTable.Builder desertPyramidLootTable() {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(2.0F, 4.0F))
					.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 5.0F))))
					.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 7.0F))))
					.add(LootItem.lootTableItem(Items.EMERALD).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.BONE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 6.0F))))
					.add(LootItem.lootTableItem(Items.SPIDER_EYE).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(25).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 7.0F))))
					.add(LootItem.lootTableItem(Items.SADDLE).setWeight(20))
					.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR).setWeight(15))
					.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR).setWeight(10))
					.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR).setWeight(5))
					.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries)))
					.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(20))
					.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(2))
					.add(EmptyLootItem.emptyItem().setWeight(15))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(4.0F))
					.add(LootItem.lootTableItem(Items.BONE).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
					.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
					.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
					.add(LootItem.lootTableItem(Items.STRING).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
					.add(LootItem.lootTableItem(Blocks.SAND).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 8.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(EmptyLootItem.emptyItem().setWeight(4))
					.add(LootItem.lootTableItem(Items.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(1).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
					.add(
						LootItem.lootTableItem(Items.BOOK)
							.setWeight(2)
							.apply(new EnchantRandomlyFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.UNBREAKING)))
					)
			);
	}

	public LootTable.Builder ancientCityLootTable() {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(5.0F, 10.0F))
					.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(1).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
					.add(LootItem.lootTableItem(Items.MUSIC_DISC_OTHERSIDE).setWeight(1))
					.add(LootItem.lootTableItem(Items.COMPASS).setWeight(2).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
					.add(LootItem.lootTableItem(Items.SCULK_CATALYST).setWeight(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
					.add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(2))
					.add(
						LootItem.lootTableItem(Items.DIAMOND_HOE)
							.setWeight(2)
							.apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F)))
							.apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.8F, 1.0F)))
							.apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, UniformGenerator.between(30.0F, 50.0F)))
					)
					.add(LootItem.lootTableItem(Items.LEAD).setWeight(2).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
					.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR).setWeight(2).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
					.add(LootItem.lootTableItem(Items.SADDLE).setWeight(2).apply(SetItemCountFunction.setCount(ConstantValue.exactly(1.0F))))
					.add(LootItem.lootTableItem(Items.MUSIC_DISC_13).setWeight(2))
					.add(LootItem.lootTableItem(Items.MUSIC_DISC_CAT).setWeight(2))
					.add(
						LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)
							.setWeight(2)
							.apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, UniformGenerator.between(30.0F, 50.0F)))
					)
					.add(
						LootItem.lootTableItem(Items.BOOK)
							.setWeight(3)
							.apply(new EnchantRandomlyFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.SWIFT_SNEAK)))
					)
					.add(LootItem.lootTableItem(Items.SCULK).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 10.0F))))
					.add(LootItem.lootTableItem(Items.SCULK_SENSOR).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.CANDLE).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
					.add(LootItem.lootTableItem(Items.AMETHYST_SHARD).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 15.0F))))
					.add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.GLOW_BERRIES).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 15.0F))))
					.add(
						LootItem.lootTableItem(Items.IRON_LEGGINGS)
							.setWeight(3)
							.apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, UniformGenerator.between(20.0F, 39.0F)))
					)
					.add(LootItem.lootTableItem(Items.ECHO_SHARD).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.DISC_FRAGMENT_5).setWeight(4).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(
						LootItem.lootTableItem(Items.POTION)
							.setWeight(5)
							.apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
							.apply(SetPotionFunction.setPotion(Potions.STRONG_REGENERATION))
					)
					.add(LootItem.lootTableItem(Items.BOOK).setWeight(5).apply(EnchantRandomlyFunction.randomApplicableEnchantment(this.registries)))
					.add(LootItem.lootTableItem(Items.BOOK).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 10.0F))))
					.add(LootItem.lootTableItem(Items.BONE).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 15.0F))))
					.add(LootItem.lootTableItem(Items.SOUL_TORCH).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 15.0F))))
					.add(LootItem.lootTableItem(Items.COAL).setWeight(7).apply(SetItemCountFunction.setCount(UniformGenerator.between(6.0F, 15.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(EmptyLootItem.emptyItem().setWeight(71))
					.add(
						LootItem.lootTableItem(Items.BOOK)
							.setWeight(4)
							.apply(new EnchantRandomlyFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.MENDING)))
					)
					.add(LootItem.lootTableItem(Items.WARD_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(4))
					.add(LootItem.lootTableItem(Items.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(1))
			);
	}

	public LootTable.Builder jungleTempleLootTable() {
		HolderLookup.RegistryLookup<Enchantment> registryLookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(UniformGenerator.between(2.0F, 6.0F))
					.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 5.0F))))
					.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 7.0F))))
					.add(LootItem.lootTableItem(Blocks.BAMBOO).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
					.add(LootItem.lootTableItem(Items.BONE).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 6.0F))))
					.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(16).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 7.0F))))
					.add(LootItem.lootTableItem(Items.SADDLE).setWeight(3))
					.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR))
					.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR))
					.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR))
					.add(LootItem.lootTableItem(Items.BOOK).apply(EnchantWithLevelsFunction.enchantWithLevels(this.registries, ConstantValue.exactly(30.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(EmptyLootItem.emptyItem().setWeight(2))
					.add(LootItem.lootTableItem(Items.WILD_ARMOR_TRIM_SMITHING_TEMPLATE).setWeight(1).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(EmptyLootItem.emptyItem().setWeight(1))
					.add(LootItem.lootTableItem(Items.BOOK).apply(new EnchantRandomlyFunction.Builder().withEnchantment(registryLookup.getOrThrow(Enchantments.UNBREAKING))))
			);
	}
}
