/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.loot.packs;

import java.util.function.BiConsumer;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.FishingHookPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.LootTableReference;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemDamageFunction;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class VanillaFishingLoot
implements LootTableSubProvider {
    public static final LootItemCondition.Builder IN_JUNGLE = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(Biomes.JUNGLE));
    public static final LootItemCondition.Builder IN_SPARSE_JUNGLE = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(Biomes.SPARSE_JUNGLE));
    public static final LootItemCondition.Builder IN_BAMBOO_JUNGLE = LocationCheck.checkLocation(LocationPredicate.Builder.location().setBiome(Biomes.BAMBOO_JUNGLE));

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        biConsumer.accept(BuiltInLootTables.FISHING, LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0f)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootTableReference.lootTableReference(BuiltInLootTables.FISHING_JUNK).setWeight(10)).setQuality(-2)).add((LootPoolEntryContainer.Builder<?>)((LootPoolEntryContainer.Builder)((LootPoolSingletonContainer.Builder)LootTableReference.lootTableReference(BuiltInLootTables.FISHING_TREASURE).setWeight(5)).setQuality(2)).when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().subPredicate(FishingHookPredicate.inOpenWater(true))))).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootTableReference.lootTableReference(BuiltInLootTables.FISHING_FISH).setWeight(85)).setQuality(-1))));
        biConsumer.accept(BuiltInLootTables.FISHING_FISH, LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.COD).setWeight(60)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.SALMON).setWeight(25)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.TROPICAL_FISH).setWeight(2)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.PUFFERFISH).setWeight(13))));
        biConsumer.accept(BuiltInLootTables.FISHING_JUNK, LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Blocks.LILY_PAD).setWeight(17)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.LEATHER_BOOTS).setWeight(10)).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.0f, 0.9f))))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.LEATHER).setWeight(10)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.BONE).setWeight(10)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.POTION).setWeight(10)).apply(SetPotionFunction.setPotion(Potions.WATER)))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.STRING).setWeight(5)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.FISHING_ROD).setWeight(2)).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.0f, 0.9f))))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.BOWL).setWeight(10)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.STICK).setWeight(5)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.INK_SAC).setWeight(1)).apply(SetItemCountFunction.setCount(ConstantValue.exactly(10.0f))))).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Blocks.TRIPWIRE_HOOK).setWeight(10)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(10)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Blocks.BAMBOO).when(IN_JUNGLE.or(IN_SPARSE_JUNGLE).or(IN_BAMBOO_JUNGLE))).setWeight(10))));
        biConsumer.accept(BuiltInLootTables.FISHING_TREASURE, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.NAME_TAG)).add(LootItem.lootTableItem(Items.SADDLE)).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.BOW).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.0f, 0.25f)))).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantValue.exactly(30.0f)).allowTreasure()))).add((LootPoolEntryContainer.Builder<?>)((Object)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.FISHING_ROD).apply(SetItemDamageFunction.setDamage(UniformGenerator.between(0.0f, 0.25f)))).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantValue.exactly(30.0f)).allowTreasure()))).add((LootPoolEntryContainer.Builder<?>)((Object)LootItem.lootTableItem(Items.BOOK).apply(EnchantWithLevelsFunction.enchantWithLevels(ConstantValue.exactly(30.0f)).allowTreasure()))).add(LootItem.lootTableItem(Items.NAUTILUS_SHELL))));
    }
}

