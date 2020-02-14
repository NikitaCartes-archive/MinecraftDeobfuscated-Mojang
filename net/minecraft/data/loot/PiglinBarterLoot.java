/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;

public class PiglinBarterLoot
implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        biConsumer.accept(BuiltInLootTables.PIGLIN_BARTERING, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.WARPED_NYLIUM).setWeight(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.QUARTZ).setWeight(1)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.OBSIDIAN).setWeight(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.GLOWSTONE_DUST).setWeight(2)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(2)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 3.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(2)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.SHROOMLIGHT).setWeight(5)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.FIRE_CHARGE).setWeight(5)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.GRAVEL).setWeight(5)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0f, 12.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.PORKCHOP).setWeight(5)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 5.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.LEATHER).setWeight(5)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 7.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.WARPED_FUNGI).setWeight(5)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 2.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SOUL_SAND).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.RED_MUSHROOM).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.BROWN_MUSHROOM).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.FLINT).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0f, 8.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0f, 12.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.CRIMSON_FUNGI).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 4.0f)))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.NETHER_BRICK).setWeight(10)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 4.0f))))));
    }

    @Override
    public /* synthetic */ void accept(Object object) {
        this.accept((BiConsumer)object);
    }
}

