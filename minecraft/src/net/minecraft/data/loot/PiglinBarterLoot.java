package net.minecraft.data.loot;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;

public class PiglinBarterLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
	public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
		biConsumer.accept(
			BuiltInLootTables.PIGLIN_BARTERING,
			LootTable.lootTable()
				.withPool(
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(Items.WARPED_NYLIUM).setWeight(1))
						.add(LootItem.lootTableItem(Items.QUARTZ).setWeight(1).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.OBSIDIAN).setWeight(1))
						.add(LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 3.0F))))
						.add(LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(2).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.SHROOMLIGHT).setWeight(5))
						.add(LootItem.lootTableItem(Items.FIRE_CHARGE).setWeight(5))
						.add(LootItem.lootTableItem(Items.GRAVEL).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 12.0F))))
						.add(LootItem.lootTableItem(Items.PORKCHOP).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F))))
						.add(LootItem.lootTableItem(Items.LEATHER).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 7.0F))))
						.add(LootItem.lootTableItem(Items.WARPED_FUNGI).setWeight(5).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F))))
						.add(LootItem.lootTableItem(Items.SOUL_SAND).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.RED_MUSHROOM).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.BROWN_MUSHROOM).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.FLINT).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 8.0F))))
						.add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 12.0F))))
						.add(LootItem.lootTableItem(Items.CRIMSON_FUNGI).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
						.add(LootItem.lootTableItem(Items.NETHER_BRICK).setWeight(10).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 4.0F))))
				)
		);
	}
}
