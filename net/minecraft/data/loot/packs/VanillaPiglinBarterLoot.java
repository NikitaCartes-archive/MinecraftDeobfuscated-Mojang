/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class VanillaPiglinBarterLoot
implements LootTableSubProvider {
    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        biConsumer.accept(BuiltInLootTables.PIGLIN_BARTERING, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.BOOK).setWeight(5)).apply(new EnchantRandomlyFunction.Builder().withEnchantment(Enchantments.SOUL_SPEED)))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.IRON_BOOTS).setWeight(8)).apply(new EnchantRandomlyFunction.Builder().withEnchantment(Enchantments.SOUL_SPEED)))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.POTION).setWeight(8)).apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SPLASH_POTION).setWeight(8)).apply(SetPotionFunction.setPotion(Potions.FIRE_RESISTANCE)))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.POTION).setWeight(10)).apply(SetPotionFunction.setPotion(Potions.WATER)))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(10)).apply(SetItemCountFunction.setCount(UniformGenerator.between(10.0f, 36.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.ENDER_PEARL).setWeight(10)).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.STRING).setWeight(20)).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0f, 9.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.QUARTZ).setWeight(20)).apply(SetItemCountFunction.setCount(UniformGenerator.between(5.0f, 12.0f))))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.OBSIDIAN).setWeight(40)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.CRYING_OBSIDIAN).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0f, 3.0f))))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.FIRE_CHARGE).setWeight(40)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.LEATHER).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 4.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SOUL_SAND).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 8.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.NETHER_BRICK).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0f, 8.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SPECTRAL_ARROW).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(6.0f, 12.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.GRAVEL).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(8.0f, 16.0f))))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.BLACKSTONE).setWeight(40)).apply(SetItemCountFunction.setCount(UniformGenerator.between(8.0f, 16.0f)))))));
    }
}

