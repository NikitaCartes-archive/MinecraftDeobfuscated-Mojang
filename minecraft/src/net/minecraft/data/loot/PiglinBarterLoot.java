package net.minecraft.data.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetNbtFunction;

public class PiglinBarterLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
	public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
		biConsumer.accept(
			BuiltInLootTables.PIGLIN_BARTERING,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.NETHERITE_HOE).setWeight(1))
						.add(LootItem.lootTableItem(Items.BOOK).setWeight(1).apply(new EnchantRandomlyFunction.Builder().withEnchantment(Enchantments.SOUL_SPEED)))
						.add(LootItem.lootTableItem(Items.IRON_BOOTS).setWeight(5).apply(new EnchantRandomlyFunction.Builder().withEnchantment(Enchantments.SOUL_SPEED)))
						.add(
							LootItem.lootTableItem(Items.POTION)
								.setWeight(10)
								.apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), compoundTag -> compoundTag.putString("Potion", "minecraft:fire_resistance"))))
						)
						.add(
							LootItem.lootTableItem(Items.SPLASH_POTION)
								.setWeight(10)
								.apply(SetNbtFunction.setTag(Util.make(new CompoundTag(), compoundTag -> compoundTag.putString("Potion", "minecraft:fire_resistance"))))
						)
						.add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(9.0F, 36.0F))))
						.add(LootItem.lootTableItem(Items.QUARTZ).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(8.0F, 16.0F))))
						.add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(5.0F, 12.0F))))
						.add(LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F))))
						.add(LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.STRING).setWeight(20).apply(SetItemCountFunction.setCount(RandomValueBounds.between(8.0F, 24.0F))))
						.add(LootItem.lootTableItem(Items.FIRE_CHARGE).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.GRAVEL).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(8.0F, 16.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 10.0F))))
						.add(LootItem.lootTableItem(Items.NETHER_BRICK).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 16.0F))))
						.add(LootItem.lootTableItem(Items.OBSIDIAN).setWeight(40))
						.add(LootItem.lootTableItem(Items.CRYING_OBSIDIAN).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.SOUL_SAND).setWeight(40).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 16.0F))))
				)
		);
	}
}
