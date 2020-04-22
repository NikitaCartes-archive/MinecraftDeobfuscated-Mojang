package net.minecraft.data.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetStewEffectFunction;

public class ChestLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
	public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
		biConsumer.accept(
			BuiltInLootTables.ABANDONED_MINESHAFT,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(20))
						.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE))
						.add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(30))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE).setWeight(5))
						.add(EmptyLootItem.emptyItem().setWeight(5))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(3))
						.add(LootItem.lootTableItem(Blocks.RAIL).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.POWERED_RAIL).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.DETECTOR_RAIL).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.ACTIVATOR_RAIL).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.TORCH).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 16.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.BASTION_BRIDGE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Blocks.LODESTONE).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 2.0F))
						.add(
							LootItem.lootTableItem(Items.CROSSBOW)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.1F, 0.5F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(LootItem.lootTableItem(Items.SPECTRAL_ARROW).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 12.0F))))
						.add(LootItem.lootTableItem(Blocks.GILDED_BLACKSTONE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.CRYING_OBSIDIAN).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.GOLD_BLOCK).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.GOLDEN_SWORD).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(
							LootItem.lootTableItem(Items.GOLDEN_CHESTPLATE)
								.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.GOLDEN_HELMET)
								.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.GOLDEN_LEGGINGS)
								.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.GOLDEN_BOOTS)
								.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.STRING).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.ARROW).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 17.0F))))
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.BASTION_HOGLIN_STABLE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.DIAMOND_SHOVEL)
								.setWeight(5)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.15F, 0.45F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(LootItem.lootTableItem(Items.NETHERITE_SCRAP).setWeight(2).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.ANCIENT_DEBRIS).setWeight(3).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(10).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Blocks.GOLD_BLOCK).setWeight(25).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(
							LootItem.lootTableItem(Items.GOLDEN_HOE)
								.setWeight(15)
								.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(EmptyLootItem.emptyItem().setWeight(45))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 4.0F))
						.add(LootItem.lootTableItem(Blocks.GLOWSTONE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Blocks.GILDED_BLACKSTONE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Blocks.SOUL_SAND).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Blocks.CRIMSON_NYLIUM).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.ARROW).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 17.0F))))
						.add(LootItem.lootTableItem(Items.STRING).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.PORKCHOP).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.COOKED_PORKCHOP).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F))))
						.add(LootItem.lootTableItem(Blocks.CRIMSON_FUNGUS).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Blocks.CRIMSON_ROOTS).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.BASTION_OTHER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.CROSSBOW)
								.setWeight(12)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.1F, 0.5F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(LootItem.lootTableItem(Items.ANCIENT_DEBRIS).setWeight(2).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.NETHERITE_SCRAP).setWeight(2).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.SPECTRAL_ARROW).setWeight(16).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 15.0F))))
						.add(LootItem.lootTableItem(Items.PIGLIN_BANNER_PATTERN).setWeight(5).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_PIGSTEP).setWeight(3).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(new EnchantRandomlyFunction.Builder().withEnchantment(Enchantments.SOUL_SPEED)))
						.add(EmptyLootItem.emptyItem().setWeight(50))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(2))
						.add(
							LootItem.lootTableItem(Items.GOLDEN_BOOTS)
								.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
								.apply(new EnchantRandomlyFunction.Builder().withEnchantment(Enchantments.SOUL_SPEED))
						)
						.add(LootItem.lootTableItem(Blocks.GOLD_BLOCK).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.CROSSBOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.GOLDEN_SWORD).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.GOLDEN_CHESTPLATE).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.GOLDEN_HELMET).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.GOLDEN_LEGGINGS).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.GOLDEN_BOOTS).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(EmptyLootItem.emptyItem().setWeight(2))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 5.0F))
						.add(LootItem.lootTableItem(Blocks.CRYING_OBSIDIAN).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Blocks.GILDED_BLACKSTONE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Blocks.CHAIN).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 10.0F))))
						.add(LootItem.lootTableItem(Items.MAGMA_CREAM).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
						.add(LootItem.lootTableItem(Blocks.BONE_BLOCK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.OBSIDIAN).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.STRING).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.ARROW).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 17.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.BASTION_TREASURE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 2.0F))
						.add(LootItem.lootTableItem(Items.NETHERITE_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Blocks.ANCIENT_DEBRIS).setWeight(14).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Items.NETHERITE_SCRAP).setWeight(10).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))
						.add(LootItem.lootTableItem(Blocks.ANCIENT_DEBRIS).setWeight(1).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2))))
						.add(
							LootItem.lootTableItem(Items.DIAMOND_SWORD)
								.setWeight(10)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE)
								.setWeight(6)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_HELMET)
								.setWeight(6)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)
								.setWeight(6)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_BOOTS)
								.setWeight(6)
								.apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F)))
								.apply(EnchantRandomlyFunction.randomApplicableEnchantment())
						)
						.add(LootItem.lootTableItem(Items.DIAMOND_SWORD).setWeight(6).apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F))))
						.add(LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE).setWeight(5).apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F))))
						.add(LootItem.lootTableItem(Items.DIAMOND_HELMET).setWeight(5).apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F))))
						.add(LootItem.lootTableItem(Items.DIAMOND_BOOTS).setWeight(5).apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F))))
						.add(LootItem.lootTableItem(Items.DIAMOND_LEGGINGS).setWeight(5).apply(SetItemDamageFunction.setDamage(RandomValueBounds.between(0.2F, 0.65F))))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.SPECTRAL_ARROW).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 21.0F))))
						.add(LootItem.lootTableItem(Blocks.GOLD_BLOCK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 9.0F))))
						.add(LootItem.lootTableItem(Blocks.CRYING_OBSIDIAN).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.QUARTZ).apply(SetItemCountFunction.setCount(RandomValueBounds.between(8.0F, 23.0F))))
						.add(LootItem.lootTableItem(Blocks.GILDED_BLACKSTONE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.MAGMA_CREAM).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(8.0F, 16.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.BURIED_TREASURE,
			LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Items.HEART_OF_THE_SEA)))
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(5.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.TNT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 3.0F))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(0.0F, 1.0F))
						.add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE))
						.add(LootItem.lootTableItem(Items.IRON_SWORD))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(2))
						.add(LootItem.lootTableItem(Items.COOKED_COD).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.COOKED_SALMON).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.DESERT_PYRAMID,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BONE).setWeight(25).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.SPIDER_EYE).setWeight(25).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(25).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(20))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR).setWeight(15))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR).setWeight(10))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR).setWeight(5))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(20).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(20))
						.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(2))
						.add(EmptyLootItem.emptyItem().setWeight(15))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(4))
						.add(LootItem.lootTableItem(Items.BONE).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.STRING).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.SAND).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.END_CITY_TREASURE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 6.0F))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 10.0F))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(3))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR))
						.add(
							LootItem.lootTableItem(Items.DIAMOND_SWORD)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_BOOTS)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_LEGGINGS)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_HELMET)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_PICKAXE)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.DIAMOND_SHOVEL)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_SWORD)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_BOOTS)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_CHESTPLATE)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_LEGGINGS)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_HELMET)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_PICKAXE)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
						.add(
							LootItem.lootTableItem(Items.IRON_SHOVEL)
								.setWeight(3)
								.apply(EnchantWithLevelsFunction.enchantWithLevels(RandomValueBounds.between(20.0F, 39.0F)).allowTreasure())
						)
				)
		);
		biConsumer.accept(
			BuiltInLootTables.IGLOO_CHEST,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.STONE_AXE).setWeight(2))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10))
						.add(LootItem.lootTableItem(Items.EMERALD))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F))))
				)
				.withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Items.GOLDEN_APPLE)))
		);
		biConsumer.accept(
			BuiltInLootTables.JUNGLE_TEMPLE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 6.0F))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Blocks.BAMBOO).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BONE).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(16).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(3))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.BOOK).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantIntValue.exactly(30)).allowTreasure()))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.JUNGLE_TEMPLE_DISPENSER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 2.0F))
						.add(LootItem.lootTableItem(Items.ARROW).setWeight(30).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.NETHER_BRIDGE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.GOLDEN_SWORD).setWeight(5))
						.add(LootItem.lootTableItem(Items.GOLDEN_CHESTPLATE).setWeight(5))
						.add(LootItem.lootTableItem(Items.FLINT_AND_STEEL).setWeight(5))
						.add(LootItem.lootTableItem(Items.NETHER_WART).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(10))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR).setWeight(8))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR).setWeight(5))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR).setWeight(3))
						.add(LootItem.lootTableItem(Blocks.OBSIDIAN).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.PILLAGER_OUTPOST,
			LootTable.lootTable()
				.withPool(LootPool.lootPool().setRolls(RandomValueBounds.between(0.0F, 1.0F)).add(LootItem.lootTableItem(Items.CROSSBOW)))
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 3.0F))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.POTATO).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.CARROT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 5.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 3.0F))
						.add(LootItem.lootTableItem(Blocks.DARK_OAK_LOG).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 3.0F))
						.add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(7))
						.add(LootItem.lootTableItem(Items.STRING).setWeight(4).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.ARROW).setWeight(4).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.TRIPWIRE_HOOK).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(1).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.SHIPWRECK_MAP,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(Items.MAP)
								.apply(
									ExplorationMapFunction.makeExplorationMap()
										.setDestination("buried_treasure")
										.setMapDecoration(MapDecoration.Type.RED_X)
										.setZoom((byte)1)
										.setSkipKnownStructures(false)
								)
						)
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(3))
						.add(LootItem.lootTableItem(Items.COMPASS))
						.add(LootItem.lootTableItem(Items.MAP))
						.add(LootItem.lootTableItem(Items.CLOCK))
						.add(LootItem.lootTableItem(Items.PAPER).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 10.0F))))
						.add(LootItem.lootTableItem(Items.FEATHER).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.SHIPWRECK_SUPPLY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 10.0F))
						.add(LootItem.lootTableItem(Items.PAPER).setWeight(8).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 12.0F))))
						.add(LootItem.lootTableItem(Items.POTATO).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.POISONOUS_POTATO).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.CARROT).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(8.0F, 21.0F))))
						.add(
							LootItem.lootTableItem(Items.SUSPICIOUS_STEW)
								.setWeight(10)
								.apply(
									SetStewEffectFunction.stewEffect()
										.withEffect(MobEffects.NIGHT_VISION, RandomValueBounds.between(7.0F, 10.0F))
										.withEffect(MobEffects.JUMP, RandomValueBounds.between(7.0F, 10.0F))
										.withEffect(MobEffects.WEAKNESS, RandomValueBounds.between(6.0F, 8.0F))
										.withEffect(MobEffects.BLINDNESS, RandomValueBounds.between(5.0F, 7.0F))
										.withEffect(MobEffects.POISON, RandomValueBounds.between(10.0F, 20.0F))
										.withEffect(MobEffects.SATURATION, RandomValueBounds.between(7.0F, 10.0F))
								)
						)
						.add(LootItem.lootTableItem(Items.COAL).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 24.0F))))
						.add(LootItem.lootTableItem(Blocks.PUMPKIN).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.BAMBOO).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Blocks.TNT).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER_HELMET).setWeight(3).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE).setWeight(3).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.LEATHER_LEGGINGS).setWeight(3).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.LEATHER_BOOTS).setWeight(3).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.SHIPWRECK_TREASURE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 6.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(90).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(5))
						.add(LootItem.lootTableItem(Items.EXPERIENCE_BOTTLE).setWeight(5))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(50).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 10.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 10.0F))))
						.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 10.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.SIMPLE_DUNGEON,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 3.0F))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(20))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(15))
						.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(2))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_13).setWeight(15))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_CAT).setWeight(15))
						.add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(20))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR).setWeight(10))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR).setWeight(15))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR).setWeight(5))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(20))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BUCKET).setWeight(10))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(3))
						.add(LootItem.lootTableItem(Items.BONE).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.STRING).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.SPAWN_BONUS_CHEST,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.STONE_AXE))
						.add(LootItem.lootTableItem(Items.WOODEN_AXE).setWeight(3))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.STONE_PICKAXE))
						.add(LootItem.lootTableItem(Items.WOODEN_PICKAXE).setWeight(3))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(3))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.SALMON).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(4))
						.add(LootItem.lootTableItem(Items.STICK).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 12.0F))))
						.add(LootItem.lootTableItem(Blocks.OAK_PLANKS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 12.0F))))
						.add(LootItem.lootTableItem(Blocks.OAK_LOG).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.SPRUCE_LOG).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.BIRCH_LOG).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.JUNGLE_LOG).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.ACACIA_LOG).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.DARK_OAK_LOG).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.STRONGHOLD_CORRIDOR,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 3.0F))
						.add(LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(10))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_SWORD).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_CHESTPLATE).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_HELMET).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_LEGGINGS).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_BOOTS).setWeight(5))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE))
						.add(LootItem.lootTableItem(Items.SADDLE))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.BOOK).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantIntValue.exactly(30)).allowTreasure()))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.STRONGHOLD_CROSSING,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 9.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE))
						.add(LootItem.lootTableItem(Items.BOOK).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantIntValue.exactly(30)).allowTreasure()))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.STRONGHOLD_LIBRARY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 10.0F))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.PAPER).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.MAP))
						.add(LootItem.lootTableItem(Items.COMPASS))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantIntValue.exactly(30)).allowTreasure()))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.UNDERWATER_RUIN_BIG,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(5).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE))
						.add(LootItem.lootTableItem(Items.GOLDEN_HELMET))
						.add(LootItem.lootTableItem(Items.FISHING_ROD).setWeight(5).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(
							LootItem.lootTableItem(Items.MAP)
								.setWeight(10)
								.apply(
									ExplorationMapFunction.makeExplorationMap()
										.setDestination("buried_treasure")
										.setMapDecoration(MapDecoration.Type.RED_X)
										.setZoom((byte)1)
										.setSkipKnownStructures(false)
								)
						)
				)
		);
		biConsumer.accept(
			BuiltInLootTables.UNDERWATER_RUIN_SMALL,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(2.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.STONE_AXE).setWeight(2))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(5))
						.add(LootItem.lootTableItem(Items.EMERALD))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE))
						.add(LootItem.lootTableItem(Items.GOLDEN_HELMET))
						.add(LootItem.lootTableItem(Items.FISHING_ROD).setWeight(5).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(
							LootItem.lootTableItem(Items.MAP)
								.setWeight(5)
								.apply(
									ExplorationMapFunction.makeExplorationMap()
										.setDestination("buried_treasure")
										.setMapDecoration(MapDecoration.Type.RED_X)
										.setZoom((byte)1)
										.setSkipKnownStructures(false)
								)
						)
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_WEAPONSMITH,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_SWORD).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_CHESTPLATE).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_HELMET).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_LEGGINGS).setWeight(5))
						.add(LootItem.lootTableItem(Items.IRON_BOOTS).setWeight(5))
						.add(LootItem.lootTableItem(Blocks.OBSIDIAN).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F))))
						.add(LootItem.lootTableItem(Blocks.OAK_SAPLING).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(3))
						.add(LootItem.lootTableItem(Items.IRON_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR))
						.add(LootItem.lootTableItem(Items.DIAMOND_HORSE_ARMOR))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_TOOLSMITH,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_PICKAXE).setWeight(5))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.STICK).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.IRON_SHOVEL).setWeight(5))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_CARTOGRAPHER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.MAP).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.PAPER).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.COMPASS).setWeight(5))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.STICK).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_MASON,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.CLAY_BALL).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.FLOWER_POT).setWeight(1))
						.add(LootItem.lootTableItem(Blocks.STONE).setWeight(2))
						.add(LootItem.lootTableItem(Blocks.STONE_BRICKS).setWeight(2))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(4).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.YELLOW_DYE).setWeight(1))
						.add(LootItem.lootTableItem(Blocks.SMOOTH_STONE).setWeight(1))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_ARMORER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(4).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.IRON_HELMET).setWeight(1))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_SHEPHERD,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Blocks.WHITE_WOOL).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Blocks.BLACK_WOOL).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.GRAY_WOOL).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.BROWN_WOOL).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Blocks.LIGHT_GRAY_WOOL).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
						.add(LootItem.lootTableItem(Items.SHEARS).setWeight(1))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 6.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_BUTCHER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
						.add(LootItem.lootTableItem(Items.PORKCHOP).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BEEF).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.MUTTON).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_FLETCHER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
						.add(LootItem.lootTableItem(Items.ARROW).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.FEATHER).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.EGG).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.FLINT).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.STICK).setWeight(6).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_FISHER,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1))
						.add(LootItem.lootTableItem(Items.COD).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.SALMON).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.WATER_BUCKET).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.BARREL).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(3).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_TANNERY,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 5.0F))
						.add(LootItem.lootTableItem(Items.LEATHER).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER_CHESTPLATE).setWeight(2))
						.add(LootItem.lootTableItem(Items.LEATHER_BOOTS).setWeight(2))
						.add(LootItem.lootTableItem(Items.LEATHER_HELMET).setWeight(2))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER_LEGGINGS).setWeight(2))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(1))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_TEMPLE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(7).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.LAPIS_LAZULI).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_PLAINS_HOUSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.DANDELION).setWeight(2))
						.add(LootItem.lootTableItem(Items.POPPY).setWeight(1))
						.add(LootItem.lootTableItem(Items.POTATO).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.APPLE).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(1))
						.add(LootItem.lootTableItem(Items.FEATHER).setWeight(1))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.OAK_SAPLING).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_TAIGA_HOUSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.FERN).setWeight(2))
						.add(LootItem.lootTableItem(Items.LARGE_FERN).setWeight(2))
						.add(LootItem.lootTableItem(Items.POTATO).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.SWEET_BERRIES).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.PUMPKIN_PIE).setWeight(1))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.SPRUCE_SAPLING).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.SPRUCE_SIGN).setWeight(1))
						.add(LootItem.lootTableItem(Items.SPRUCE_LOG).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_SAVANNA_HOUSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.GRASS).setWeight(5))
						.add(LootItem.lootTableItem(Items.TALL_GRASS).setWeight(5))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.WHEAT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Blocks.ACACIA_SAPLING).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.SADDLE).setWeight(1))
						.add(LootItem.lootTableItem(Blocks.TORCH).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.BUCKET).setWeight(1))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_SNOWY_HOUSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Blocks.BLUE_ICE).setWeight(1))
						.add(LootItem.lootTableItem(Blocks.SNOW_BLOCK).setWeight(4))
						.add(LootItem.lootTableItem(Items.POTATO).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SOUP).setWeight(1))
						.add(LootItem.lootTableItem(Items.FURNACE).setWeight(1))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.SNOWBALL).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.VILLAGE_DESERT_HOUSE,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(3.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.CLAY_BALL).setWeight(1))
						.add(LootItem.lootTableItem(Items.GREEN_DYE).setWeight(1))
						.add(LootItem.lootTableItem(Blocks.CACTUS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(1))
						.add(LootItem.lootTableItem(Blocks.DEAD_BUSH).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.EMERALD).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.WOODLAND_MANSION,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 3.0F))
						.add(LootItem.lootTableItem(Items.LEAD).setWeight(20))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(15))
						.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(2))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_13).setWeight(15))
						.add(LootItem.lootTableItem(Items.MUSIC_DISC_CAT).setWeight(15))
						.add(LootItem.lootTableItem(Items.NAME_TAG).setWeight(20))
						.add(LootItem.lootTableItem(Items.CHAINMAIL_CHESTPLATE).setWeight(10))
						.add(LootItem.lootTableItem(Items.DIAMOND_HOE).setWeight(15))
						.add(LootItem.lootTableItem(Items.DIAMOND_CHESTPLATE).setWeight(5))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(10).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(1.0F, 4.0F))
						.add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BREAD).setWeight(20))
						.add(LootItem.lootTableItem(Items.WHEAT).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BUCKET).setWeight(10))
						.add(LootItem.lootTableItem(Items.REDSTONE).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.COAL).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.MELON_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.PUMPKIN_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BEETROOT_SEEDS).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
				)
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(3))
						.add(LootItem.lootTableItem(Items.BONE).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.GUNPOWDER).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.STRING).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 8.0F))))
				)
		);
		biConsumer.accept(
			BuiltInLootTables.RUINED_PORTAL,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(RandomValueBounds.between(4.0F, 8.0F))
						.add(LootItem.lootTableItem(Items.OBSIDIAN).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.FLINT).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(9.0F, 18.0F))))
						.add(LootItem.lootTableItem(Items.FLINT_AND_STEEL).setWeight(40))
						.add(LootItem.lootTableItem(Items.FIRE_CHARGE).setWeight(40))
						.add(LootItem.lootTableItem(Items.GOLDEN_APPLE).setWeight(40))
						.add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(15).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 24.0F))))
						.add(LootItem.lootTableItem(Items.GOLDEN_SWORD).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_AXE).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_HOE).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_SHOVEL).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_PICKAXE).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_BOOTS).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_CHESTPLATE).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_HELMET).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GOLDEN_LEGGINGS).setWeight(15).apply(EnchantRandomlyFunction.randomApplicableEnchantment()))
						.add(LootItem.lootTableItem(Items.GLISTERING_MELON_SLICE).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 12.0F))))
						.add(LootItem.lootTableItem(Items.GOLDEN_HORSE_ARMOR).setWeight(5))
						.add(LootItem.lootTableItem(Items.LIGHT_WEIGHTED_PRESSURE_PLATE).setWeight(5))
						.add(LootItem.lootTableItem(Items.GOLDEN_CARROT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 12.0F))))
						.add(LootItem.lootTableItem(Items.CLOCK).setWeight(5))
						.add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.BELL).setWeight(1))
						.add(LootItem.lootTableItem(Items.ENCHANTED_GOLDEN_APPLE).setWeight(1))
						.add(LootItem.lootTableItem(Items.GOLD_BLOCK).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
				)
		);
	}
}
