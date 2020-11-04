/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CarrotBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PotatoBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.ConstantIntValue;
import net.minecraft.world.level.storage.loot.IntLimiter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.DynamicLoot;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.CopyBlockState;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LimitCount;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;

public class BlockLoot
implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
    private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1))));
    private static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    private static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
    private static final Set<Item> EXPLOSION_RESISTANT = Stream.of(Blocks.DRAGON_EGG, Blocks.BEACON, Blocks.CONDUIT, Blocks.SKELETON_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.PLAYER_HEAD, Blocks.ZOMBIE_HEAD, Blocks.CREEPER_HEAD, Blocks.DRAGON_HEAD, Blocks.SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.LIGHT_GRAY_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.WHITE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX).map(ItemLike::asItem).collect(ImmutableSet.toImmutableSet());
    private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05f, 0.0625f, 0.083333336f, 0.1f};
    private static final float[] JUNGLE_LEAVES_SAPLING_CHANGES = new float[]{0.025f, 0.027777778f, 0.03125f, 0.041666668f, 0.1f};
    private final Map<ResourceLocation, LootTable.Builder> map = Maps.newHashMap();

    private static <T> T applyExplosionDecay(ItemLike itemLike, FunctionUserBuilder<T> functionUserBuilder) {
        if (!EXPLOSION_RESISTANT.contains(itemLike.asItem())) {
            return functionUserBuilder.apply(ApplyExplosionDecay.explosionDecay());
        }
        return functionUserBuilder.unwrap();
    }

    private static <T> T applyExplosionCondition(ItemLike itemLike, ConditionUserBuilder<T> conditionUserBuilder) {
        if (!EXPLOSION_RESISTANT.contains(itemLike.asItem())) {
            return conditionUserBuilder.when(ExplosionCondition.survivesExplosion());
        }
        return conditionUserBuilder.unwrap();
    }

    private static LootTable.Builder createSingleItemTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike))));
    }

    private static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder builder, LootPoolEntryContainer.Builder<?> builder2) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).when(builder)).otherwise(builder2)));
    }

    private static LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLoot.createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
    }

    private static LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLoot.createSelfDropDispatchTable(block, HAS_SHEARS, builder);
    }

    private static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
        return BlockLoot.createSelfDropDispatchTable(block, HAS_SHEARS_OR_SILK_TOUCH, builder);
    }

    private static LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike) {
        return BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(itemLike)));
    }

    private static LootTable.Builder createSingleItemTable(ItemLike itemLike, RandomIntGenerator randomIntGenerator) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(itemLike, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(randomIntGenerator)))));
    }

    private static LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike, RandomIntGenerator randomIntGenerator) {
        return BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(randomIntGenerator))));
    }

    private static LootTable.Builder createSilkTouchOnlyTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike)));
    }

    private static LootTable.Builder createPotFlowerItemTable(ItemLike itemLike) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))).withPool(BlockLoot.applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike))));
    }

    private static LootTable.Builder createSlabItemTable(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(block).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE)))))));
    }

    private static <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T comparable) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(block).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, comparable))))));
    }

    private static LootTable.Builder createNameableBlockEntityTable(Block block) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(block).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))));
    }

    private static LootTable.Builder createShulkerBoxDrop(Block block) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Lock", "BlockEntityTag.Lock").copy("LootTable", "BlockEntityTag.LootTable").copy("LootTableSeed", "BlockEntityTag.LootTableSeed"))).apply(SetContainerContents.setContents().withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS))))));
    }

    private static LootTable.Builder createBannerDrop(Block block) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns")))));
    }

    private static LootTable.Builder createBeeNestDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))));
    }

    private static LootTable.Builder createBeeHiveDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(((LootPoolEntryContainer.Builder)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).when(HAS_SILK_TOUCH)).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))).apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))).otherwise(LootItem.lootTableItem(block))));
    }

    private static LootTable.Builder createOreDrop(Block block, Item item) {
        return BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(item).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))));
    }

    private static LootTable.Builder createMushroomBlockDrop(Block block, ItemLike itemLike) {
        return BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(RandomValueBounds.between(-6.0f, 2.0f)))).apply(LimitCount.limitCount(IntLimiter.lowerBound(0)))));
    }

    private static LootTable.Builder createGrassDrops(Block block) {
        return BlockLoot.createShearsDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.WHEAT_SEEDS).when(LootItemRandomChanceCondition.randomChance(0.125f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))));
    }

    private static LootTable.Builder createStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(item).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.06666667f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, false))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.13333334f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, true))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.2f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 2))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.26666668f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 3))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.33333334f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 4))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.4f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 5))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.46666667f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 6))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 7)))))));
    }

    private static LootTable.Builder createAttachedStemDrops(Block block, Item item) {
        return LootTable.lootTable().withPool(BlockLoot.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336f))))));
    }

    private static LootTable.Builder createShearsOnlyDrop(ItemLike itemLike) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_SHEARS).add(LootItem.lootTableItem(itemLike)));
    }

    private static LootTable.Builder createLeavesDrops(Block block, Block block2, float ... fs) {
        return BlockLoot.createSilkTouchOrShearsDispatchTable(block, ((LootPoolSingletonContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(block2))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, fs))).withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 2.0f))))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f))));
    }

    private static LootTable.Builder createOakLeavesDrops(Block block, Block block2, float ... fs) {
        return BlockLoot.createLeavesDrops(block, block2, fs).withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(Items.APPLE))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005f, 0.0055555557f, 0.00625f, 0.008333334f, 0.025f))));
    }

    private static LootTable.Builder createCropDrops(Block block, Item item, Item item2, LootItemCondition.Builder builder) {
        return BlockLoot.applyExplosionDecay(block, LootTable.lootTable().withPool(LootPool.lootPool().add(((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(item).when(builder)).otherwise(LootItem.lootTableItem(item2)))).withPool(LootPool.lootPool().when(builder).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(item2).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286f, 3)))));
    }

    private static LootTable.Builder createDoublePlantShearsDrop(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SHEARS).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(block).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))));
    }

    private static LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block block2) {
        AlternativesEntry.Builder builder = ((LootPoolSingletonContainer.Builder)((LootPoolEntryContainer.Builder)LootItem.lootTableItem(block2).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))).when(HAS_SHEARS)).otherwise((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(Items.WHEAT_SEEDS))).when(LootItemRandomChanceCondition.randomChance(0.125f)));
        return LootTable.lootTable().withPool(LootPool.lootPool().add(builder).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER).build()).build()), new BlockPos(0, 1, 0)))).withPool(LootPool.lootPool().add(builder).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))).when(LocationCheck.checkLocation(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER).build()).build()), new BlockPos(0, -1, 0))));
    }

    private static LootTable.Builder createCandleDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, 2))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(3)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, 3))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(4)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, 4)))))));
    }

    private static LootTable.Builder createCandleCakeDrops(Block block) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block)));
    }

    public static LootTable.Builder noDrop() {
        return LootTable.lootTable();
    }

    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        this.dropSelf(Blocks.GRANITE);
        this.dropSelf(Blocks.POLISHED_GRANITE);
        this.dropSelf(Blocks.DIORITE);
        this.dropSelf(Blocks.POLISHED_DIORITE);
        this.dropSelf(Blocks.ANDESITE);
        this.dropSelf(Blocks.POLISHED_ANDESITE);
        this.dropSelf(Blocks.DIRT);
        this.dropSelf(Blocks.COARSE_DIRT);
        this.dropSelf(Blocks.COBBLESTONE);
        this.dropSelf(Blocks.OAK_PLANKS);
        this.dropSelf(Blocks.SPRUCE_PLANKS);
        this.dropSelf(Blocks.BIRCH_PLANKS);
        this.dropSelf(Blocks.JUNGLE_PLANKS);
        this.dropSelf(Blocks.ACACIA_PLANKS);
        this.dropSelf(Blocks.DARK_OAK_PLANKS);
        this.dropSelf(Blocks.OAK_SAPLING);
        this.dropSelf(Blocks.SPRUCE_SAPLING);
        this.dropSelf(Blocks.BIRCH_SAPLING);
        this.dropSelf(Blocks.JUNGLE_SAPLING);
        this.dropSelf(Blocks.ACACIA_SAPLING);
        this.dropSelf(Blocks.DARK_OAK_SAPLING);
        this.dropSelf(Blocks.SAND);
        this.dropSelf(Blocks.RED_SAND);
        this.dropSelf(Blocks.GOLD_ORE);
        this.dropSelf(Blocks.IRON_ORE);
        this.dropSelf(Blocks.OAK_LOG);
        this.dropSelf(Blocks.SPRUCE_LOG);
        this.dropSelf(Blocks.BIRCH_LOG);
        this.dropSelf(Blocks.JUNGLE_LOG);
        this.dropSelf(Blocks.ACACIA_LOG);
        this.dropSelf(Blocks.DARK_OAK_LOG);
        this.dropSelf(Blocks.STRIPPED_SPRUCE_LOG);
        this.dropSelf(Blocks.STRIPPED_BIRCH_LOG);
        this.dropSelf(Blocks.STRIPPED_JUNGLE_LOG);
        this.dropSelf(Blocks.STRIPPED_ACACIA_LOG);
        this.dropSelf(Blocks.STRIPPED_DARK_OAK_LOG);
        this.dropSelf(Blocks.STRIPPED_OAK_LOG);
        this.dropSelf(Blocks.STRIPPED_WARPED_STEM);
        this.dropSelf(Blocks.STRIPPED_CRIMSON_STEM);
        this.dropSelf(Blocks.OAK_WOOD);
        this.dropSelf(Blocks.SPRUCE_WOOD);
        this.dropSelf(Blocks.BIRCH_WOOD);
        this.dropSelf(Blocks.JUNGLE_WOOD);
        this.dropSelf(Blocks.ACACIA_WOOD);
        this.dropSelf(Blocks.DARK_OAK_WOOD);
        this.dropSelf(Blocks.STRIPPED_OAK_WOOD);
        this.dropSelf(Blocks.STRIPPED_SPRUCE_WOOD);
        this.dropSelf(Blocks.STRIPPED_BIRCH_WOOD);
        this.dropSelf(Blocks.STRIPPED_JUNGLE_WOOD);
        this.dropSelf(Blocks.STRIPPED_ACACIA_WOOD);
        this.dropSelf(Blocks.STRIPPED_DARK_OAK_WOOD);
        this.dropSelf(Blocks.STRIPPED_CRIMSON_HYPHAE);
        this.dropSelf(Blocks.STRIPPED_WARPED_HYPHAE);
        this.dropSelf(Blocks.SPONGE);
        this.dropSelf(Blocks.WET_SPONGE);
        this.dropSelf(Blocks.LAPIS_BLOCK);
        this.dropSelf(Blocks.SANDSTONE);
        this.dropSelf(Blocks.CHISELED_SANDSTONE);
        this.dropSelf(Blocks.CUT_SANDSTONE);
        this.dropSelf(Blocks.NOTE_BLOCK);
        this.dropSelf(Blocks.POWERED_RAIL);
        this.dropSelf(Blocks.DETECTOR_RAIL);
        this.dropSelf(Blocks.STICKY_PISTON);
        this.dropSelf(Blocks.PISTON);
        this.dropSelf(Blocks.WHITE_WOOL);
        this.dropSelf(Blocks.ORANGE_WOOL);
        this.dropSelf(Blocks.MAGENTA_WOOL);
        this.dropSelf(Blocks.LIGHT_BLUE_WOOL);
        this.dropSelf(Blocks.YELLOW_WOOL);
        this.dropSelf(Blocks.LIME_WOOL);
        this.dropSelf(Blocks.PINK_WOOL);
        this.dropSelf(Blocks.GRAY_WOOL);
        this.dropSelf(Blocks.LIGHT_GRAY_WOOL);
        this.dropSelf(Blocks.CYAN_WOOL);
        this.dropSelf(Blocks.PURPLE_WOOL);
        this.dropSelf(Blocks.BLUE_WOOL);
        this.dropSelf(Blocks.BROWN_WOOL);
        this.dropSelf(Blocks.GREEN_WOOL);
        this.dropSelf(Blocks.RED_WOOL);
        this.dropSelf(Blocks.BLACK_WOOL);
        this.dropSelf(Blocks.DANDELION);
        this.dropSelf(Blocks.POPPY);
        this.dropSelf(Blocks.BLUE_ORCHID);
        this.dropSelf(Blocks.ALLIUM);
        this.dropSelf(Blocks.AZURE_BLUET);
        this.dropSelf(Blocks.RED_TULIP);
        this.dropSelf(Blocks.ORANGE_TULIP);
        this.dropSelf(Blocks.WHITE_TULIP);
        this.dropSelf(Blocks.PINK_TULIP);
        this.dropSelf(Blocks.OXEYE_DAISY);
        this.dropSelf(Blocks.CORNFLOWER);
        this.dropSelf(Blocks.WITHER_ROSE);
        this.dropSelf(Blocks.LILY_OF_THE_VALLEY);
        this.dropSelf(Blocks.BROWN_MUSHROOM);
        this.dropSelf(Blocks.RED_MUSHROOM);
        this.dropSelf(Blocks.GOLD_BLOCK);
        this.dropSelf(Blocks.IRON_BLOCK);
        this.dropSelf(Blocks.BRICKS);
        this.dropSelf(Blocks.MOSSY_COBBLESTONE);
        this.dropSelf(Blocks.OBSIDIAN);
        this.dropSelf(Blocks.CRYING_OBSIDIAN);
        this.dropSelf(Blocks.TORCH);
        this.dropSelf(Blocks.OAK_STAIRS);
        this.dropSelf(Blocks.REDSTONE_WIRE);
        this.dropSelf(Blocks.DIAMOND_BLOCK);
        this.dropSelf(Blocks.CRAFTING_TABLE);
        this.dropSelf(Blocks.OAK_SIGN);
        this.dropSelf(Blocks.SPRUCE_SIGN);
        this.dropSelf(Blocks.BIRCH_SIGN);
        this.dropSelf(Blocks.ACACIA_SIGN);
        this.dropSelf(Blocks.JUNGLE_SIGN);
        this.dropSelf(Blocks.DARK_OAK_SIGN);
        this.dropSelf(Blocks.LADDER);
        this.dropSelf(Blocks.RAIL);
        this.dropSelf(Blocks.COBBLESTONE_STAIRS);
        this.dropSelf(Blocks.LEVER);
        this.dropSelf(Blocks.STONE_PRESSURE_PLATE);
        this.dropSelf(Blocks.OAK_PRESSURE_PLATE);
        this.dropSelf(Blocks.SPRUCE_PRESSURE_PLATE);
        this.dropSelf(Blocks.BIRCH_PRESSURE_PLATE);
        this.dropSelf(Blocks.JUNGLE_PRESSURE_PLATE);
        this.dropSelf(Blocks.ACACIA_PRESSURE_PLATE);
        this.dropSelf(Blocks.DARK_OAK_PRESSURE_PLATE);
        this.dropSelf(Blocks.REDSTONE_TORCH);
        this.dropSelf(Blocks.STONE_BUTTON);
        this.dropSelf(Blocks.CACTUS);
        this.dropSelf(Blocks.SUGAR_CANE);
        this.dropSelf(Blocks.JUKEBOX);
        this.dropSelf(Blocks.OAK_FENCE);
        this.dropSelf(Blocks.PUMPKIN);
        this.dropSelf(Blocks.NETHERRACK);
        this.dropSelf(Blocks.SOUL_SAND);
        this.dropSelf(Blocks.SOUL_SOIL);
        this.dropSelf(Blocks.BASALT);
        this.dropSelf(Blocks.POLISHED_BASALT);
        this.dropSelf(Blocks.SOUL_TORCH);
        this.dropSelf(Blocks.CARVED_PUMPKIN);
        this.dropSelf(Blocks.JACK_O_LANTERN);
        this.dropSelf(Blocks.REPEATER);
        this.dropSelf(Blocks.OAK_TRAPDOOR);
        this.dropSelf(Blocks.SPRUCE_TRAPDOOR);
        this.dropSelf(Blocks.BIRCH_TRAPDOOR);
        this.dropSelf(Blocks.JUNGLE_TRAPDOOR);
        this.dropSelf(Blocks.ACACIA_TRAPDOOR);
        this.dropSelf(Blocks.DARK_OAK_TRAPDOOR);
        this.dropSelf(Blocks.STONE_BRICKS);
        this.dropSelf(Blocks.MOSSY_STONE_BRICKS);
        this.dropSelf(Blocks.CRACKED_STONE_BRICKS);
        this.dropSelf(Blocks.CHISELED_STONE_BRICKS);
        this.dropSelf(Blocks.IRON_BARS);
        this.dropSelf(Blocks.OAK_FENCE_GATE);
        this.dropSelf(Blocks.BRICK_STAIRS);
        this.dropSelf(Blocks.STONE_BRICK_STAIRS);
        this.dropSelf(Blocks.LILY_PAD);
        this.dropSelf(Blocks.NETHER_BRICKS);
        this.dropSelf(Blocks.NETHER_BRICK_FENCE);
        this.dropSelf(Blocks.NETHER_BRICK_STAIRS);
        this.dropSelf(Blocks.CAULDRON);
        this.dropSelf(Blocks.END_STONE);
        this.dropSelf(Blocks.REDSTONE_LAMP);
        this.dropSelf(Blocks.SANDSTONE_STAIRS);
        this.dropSelf(Blocks.TRIPWIRE_HOOK);
        this.dropSelf(Blocks.EMERALD_BLOCK);
        this.dropSelf(Blocks.SPRUCE_STAIRS);
        this.dropSelf(Blocks.BIRCH_STAIRS);
        this.dropSelf(Blocks.JUNGLE_STAIRS);
        this.dropSelf(Blocks.COBBLESTONE_WALL);
        this.dropSelf(Blocks.MOSSY_COBBLESTONE_WALL);
        this.dropSelf(Blocks.FLOWER_POT);
        this.dropSelf(Blocks.OAK_BUTTON);
        this.dropSelf(Blocks.SPRUCE_BUTTON);
        this.dropSelf(Blocks.BIRCH_BUTTON);
        this.dropSelf(Blocks.JUNGLE_BUTTON);
        this.dropSelf(Blocks.ACACIA_BUTTON);
        this.dropSelf(Blocks.DARK_OAK_BUTTON);
        this.dropSelf(Blocks.SKELETON_SKULL);
        this.dropSelf(Blocks.WITHER_SKELETON_SKULL);
        this.dropSelf(Blocks.ZOMBIE_HEAD);
        this.dropSelf(Blocks.CREEPER_HEAD);
        this.dropSelf(Blocks.DRAGON_HEAD);
        this.dropSelf(Blocks.ANVIL);
        this.dropSelf(Blocks.CHIPPED_ANVIL);
        this.dropSelf(Blocks.DAMAGED_ANVIL);
        this.dropSelf(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE);
        this.dropSelf(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE);
        this.dropSelf(Blocks.COMPARATOR);
        this.dropSelf(Blocks.DAYLIGHT_DETECTOR);
        this.dropSelf(Blocks.REDSTONE_BLOCK);
        this.dropSelf(Blocks.QUARTZ_BLOCK);
        this.dropSelf(Blocks.CHISELED_QUARTZ_BLOCK);
        this.dropSelf(Blocks.QUARTZ_PILLAR);
        this.dropSelf(Blocks.QUARTZ_STAIRS);
        this.dropSelf(Blocks.ACTIVATOR_RAIL);
        this.dropSelf(Blocks.WHITE_TERRACOTTA);
        this.dropSelf(Blocks.ORANGE_TERRACOTTA);
        this.dropSelf(Blocks.MAGENTA_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_BLUE_TERRACOTTA);
        this.dropSelf(Blocks.YELLOW_TERRACOTTA);
        this.dropSelf(Blocks.LIME_TERRACOTTA);
        this.dropSelf(Blocks.PINK_TERRACOTTA);
        this.dropSelf(Blocks.GRAY_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_GRAY_TERRACOTTA);
        this.dropSelf(Blocks.CYAN_TERRACOTTA);
        this.dropSelf(Blocks.PURPLE_TERRACOTTA);
        this.dropSelf(Blocks.BLUE_TERRACOTTA);
        this.dropSelf(Blocks.BROWN_TERRACOTTA);
        this.dropSelf(Blocks.GREEN_TERRACOTTA);
        this.dropSelf(Blocks.RED_TERRACOTTA);
        this.dropSelf(Blocks.BLACK_TERRACOTTA);
        this.dropSelf(Blocks.ACACIA_STAIRS);
        this.dropSelf(Blocks.DARK_OAK_STAIRS);
        this.dropSelf(Blocks.SLIME_BLOCK);
        this.dropSelf(Blocks.IRON_TRAPDOOR);
        this.dropSelf(Blocks.PRISMARINE);
        this.dropSelf(Blocks.PRISMARINE_BRICKS);
        this.dropSelf(Blocks.DARK_PRISMARINE);
        this.dropSelf(Blocks.PRISMARINE_STAIRS);
        this.dropSelf(Blocks.PRISMARINE_BRICK_STAIRS);
        this.dropSelf(Blocks.DARK_PRISMARINE_STAIRS);
        this.dropSelf(Blocks.HAY_BLOCK);
        this.dropSelf(Blocks.WHITE_CARPET);
        this.dropSelf(Blocks.ORANGE_CARPET);
        this.dropSelf(Blocks.MAGENTA_CARPET);
        this.dropSelf(Blocks.LIGHT_BLUE_CARPET);
        this.dropSelf(Blocks.YELLOW_CARPET);
        this.dropSelf(Blocks.LIME_CARPET);
        this.dropSelf(Blocks.PINK_CARPET);
        this.dropSelf(Blocks.GRAY_CARPET);
        this.dropSelf(Blocks.LIGHT_GRAY_CARPET);
        this.dropSelf(Blocks.CYAN_CARPET);
        this.dropSelf(Blocks.PURPLE_CARPET);
        this.dropSelf(Blocks.BLUE_CARPET);
        this.dropSelf(Blocks.BROWN_CARPET);
        this.dropSelf(Blocks.GREEN_CARPET);
        this.dropSelf(Blocks.RED_CARPET);
        this.dropSelf(Blocks.BLACK_CARPET);
        this.dropSelf(Blocks.TERRACOTTA);
        this.dropSelf(Blocks.COAL_BLOCK);
        this.dropSelf(Blocks.RED_SANDSTONE);
        this.dropSelf(Blocks.CHISELED_RED_SANDSTONE);
        this.dropSelf(Blocks.CUT_RED_SANDSTONE);
        this.dropSelf(Blocks.RED_SANDSTONE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_STONE);
        this.dropSelf(Blocks.SMOOTH_SANDSTONE);
        this.dropSelf(Blocks.SMOOTH_QUARTZ);
        this.dropSelf(Blocks.SMOOTH_RED_SANDSTONE);
        this.dropSelf(Blocks.SPRUCE_FENCE_GATE);
        this.dropSelf(Blocks.BIRCH_FENCE_GATE);
        this.dropSelf(Blocks.JUNGLE_FENCE_GATE);
        this.dropSelf(Blocks.ACACIA_FENCE_GATE);
        this.dropSelf(Blocks.DARK_OAK_FENCE_GATE);
        this.dropSelf(Blocks.SPRUCE_FENCE);
        this.dropSelf(Blocks.BIRCH_FENCE);
        this.dropSelf(Blocks.JUNGLE_FENCE);
        this.dropSelf(Blocks.ACACIA_FENCE);
        this.dropSelf(Blocks.DARK_OAK_FENCE);
        this.dropSelf(Blocks.END_ROD);
        this.dropSelf(Blocks.PURPUR_BLOCK);
        this.dropSelf(Blocks.PURPUR_PILLAR);
        this.dropSelf(Blocks.PURPUR_STAIRS);
        this.dropSelf(Blocks.END_STONE_BRICKS);
        this.dropSelf(Blocks.MAGMA_BLOCK);
        this.dropSelf(Blocks.NETHER_WART_BLOCK);
        this.dropSelf(Blocks.RED_NETHER_BRICKS);
        this.dropSelf(Blocks.BONE_BLOCK);
        this.dropSelf(Blocks.OBSERVER);
        this.dropSelf(Blocks.TARGET);
        this.dropSelf(Blocks.WHITE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.ORANGE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.MAGENTA_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.YELLOW_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.LIME_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.PINK_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.GRAY_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.CYAN_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.PURPLE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.BLUE_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.BROWN_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.GREEN_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.RED_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.BLACK_GLAZED_TERRACOTTA);
        this.dropSelf(Blocks.WHITE_CONCRETE);
        this.dropSelf(Blocks.ORANGE_CONCRETE);
        this.dropSelf(Blocks.MAGENTA_CONCRETE);
        this.dropSelf(Blocks.LIGHT_BLUE_CONCRETE);
        this.dropSelf(Blocks.YELLOW_CONCRETE);
        this.dropSelf(Blocks.LIME_CONCRETE);
        this.dropSelf(Blocks.PINK_CONCRETE);
        this.dropSelf(Blocks.GRAY_CONCRETE);
        this.dropSelf(Blocks.LIGHT_GRAY_CONCRETE);
        this.dropSelf(Blocks.CYAN_CONCRETE);
        this.dropSelf(Blocks.PURPLE_CONCRETE);
        this.dropSelf(Blocks.BLUE_CONCRETE);
        this.dropSelf(Blocks.BROWN_CONCRETE);
        this.dropSelf(Blocks.GREEN_CONCRETE);
        this.dropSelf(Blocks.RED_CONCRETE);
        this.dropSelf(Blocks.BLACK_CONCRETE);
        this.dropSelf(Blocks.WHITE_CONCRETE_POWDER);
        this.dropSelf(Blocks.ORANGE_CONCRETE_POWDER);
        this.dropSelf(Blocks.MAGENTA_CONCRETE_POWDER);
        this.dropSelf(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
        this.dropSelf(Blocks.YELLOW_CONCRETE_POWDER);
        this.dropSelf(Blocks.LIME_CONCRETE_POWDER);
        this.dropSelf(Blocks.PINK_CONCRETE_POWDER);
        this.dropSelf(Blocks.GRAY_CONCRETE_POWDER);
        this.dropSelf(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
        this.dropSelf(Blocks.CYAN_CONCRETE_POWDER);
        this.dropSelf(Blocks.PURPLE_CONCRETE_POWDER);
        this.dropSelf(Blocks.BLUE_CONCRETE_POWDER);
        this.dropSelf(Blocks.BROWN_CONCRETE_POWDER);
        this.dropSelf(Blocks.GREEN_CONCRETE_POWDER);
        this.dropSelf(Blocks.RED_CONCRETE_POWDER);
        this.dropSelf(Blocks.BLACK_CONCRETE_POWDER);
        this.dropSelf(Blocks.KELP);
        this.dropSelf(Blocks.DRIED_KELP_BLOCK);
        this.dropSelf(Blocks.DEAD_TUBE_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_BRAIN_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_BUBBLE_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_FIRE_CORAL_BLOCK);
        this.dropSelf(Blocks.DEAD_HORN_CORAL_BLOCK);
        this.dropSelf(Blocks.CONDUIT);
        this.dropSelf(Blocks.DRAGON_EGG);
        this.dropSelf(Blocks.BAMBOO);
        this.dropSelf(Blocks.POLISHED_GRANITE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        this.dropSelf(Blocks.MOSSY_STONE_BRICK_STAIRS);
        this.dropSelf(Blocks.POLISHED_DIORITE_STAIRS);
        this.dropSelf(Blocks.MOSSY_COBBLESTONE_STAIRS);
        this.dropSelf(Blocks.END_STONE_BRICK_STAIRS);
        this.dropSelf(Blocks.STONE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_SANDSTONE_STAIRS);
        this.dropSelf(Blocks.SMOOTH_QUARTZ_STAIRS);
        this.dropSelf(Blocks.GRANITE_STAIRS);
        this.dropSelf(Blocks.ANDESITE_STAIRS);
        this.dropSelf(Blocks.RED_NETHER_BRICK_STAIRS);
        this.dropSelf(Blocks.POLISHED_ANDESITE_STAIRS);
        this.dropSelf(Blocks.DIORITE_STAIRS);
        this.dropSelf(Blocks.BRICK_WALL);
        this.dropSelf(Blocks.PRISMARINE_WALL);
        this.dropSelf(Blocks.RED_SANDSTONE_WALL);
        this.dropSelf(Blocks.MOSSY_STONE_BRICK_WALL);
        this.dropSelf(Blocks.GRANITE_WALL);
        this.dropSelf(Blocks.STONE_BRICK_WALL);
        this.dropSelf(Blocks.NETHER_BRICK_WALL);
        this.dropSelf(Blocks.ANDESITE_WALL);
        this.dropSelf(Blocks.RED_NETHER_BRICK_WALL);
        this.dropSelf(Blocks.SANDSTONE_WALL);
        this.dropSelf(Blocks.END_STONE_BRICK_WALL);
        this.dropSelf(Blocks.DIORITE_WALL);
        this.dropSelf(Blocks.LOOM);
        this.dropSelf(Blocks.SCAFFOLDING);
        this.dropSelf(Blocks.HONEY_BLOCK);
        this.dropSelf(Blocks.HONEYCOMB_BLOCK);
        this.dropSelf(Blocks.RESPAWN_ANCHOR);
        this.dropSelf(Blocks.LODESTONE);
        this.dropSelf(Blocks.WARPED_STEM);
        this.dropSelf(Blocks.WARPED_HYPHAE);
        this.dropSelf(Blocks.WARPED_FUNGUS);
        this.dropSelf(Blocks.WARPED_WART_BLOCK);
        this.dropSelf(Blocks.CRIMSON_STEM);
        this.dropSelf(Blocks.CRIMSON_HYPHAE);
        this.dropSelf(Blocks.CRIMSON_FUNGUS);
        this.dropSelf(Blocks.SHROOMLIGHT);
        this.dropSelf(Blocks.CRIMSON_PLANKS);
        this.dropSelf(Blocks.WARPED_PLANKS);
        this.dropSelf(Blocks.WARPED_PRESSURE_PLATE);
        this.dropSelf(Blocks.WARPED_FENCE);
        this.dropSelf(Blocks.WARPED_TRAPDOOR);
        this.dropSelf(Blocks.WARPED_FENCE_GATE);
        this.dropSelf(Blocks.WARPED_STAIRS);
        this.dropSelf(Blocks.WARPED_BUTTON);
        this.dropSelf(Blocks.WARPED_SIGN);
        this.dropSelf(Blocks.CRIMSON_PRESSURE_PLATE);
        this.dropSelf(Blocks.CRIMSON_FENCE);
        this.dropSelf(Blocks.CRIMSON_TRAPDOOR);
        this.dropSelf(Blocks.CRIMSON_FENCE_GATE);
        this.dropSelf(Blocks.CRIMSON_STAIRS);
        this.dropSelf(Blocks.CRIMSON_BUTTON);
        this.dropSelf(Blocks.CRIMSON_SIGN);
        this.dropSelf(Blocks.NETHERITE_BLOCK);
        this.dropSelf(Blocks.ANCIENT_DEBRIS);
        this.dropSelf(Blocks.BLACKSTONE);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_BRICKS);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
        this.dropSelf(Blocks.BLACKSTONE_STAIRS);
        this.dropSelf(Blocks.BLACKSTONE_WALL);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
        this.dropSelf(Blocks.CHISELED_POLISHED_BLACKSTONE);
        this.dropSelf(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_STAIRS);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_BUTTON);
        this.dropSelf(Blocks.POLISHED_BLACKSTONE_WALL);
        this.dropSelf(Blocks.CHISELED_NETHER_BRICKS);
        this.dropSelf(Blocks.CRACKED_NETHER_BRICKS);
        this.dropSelf(Blocks.QUARTZ_BRICKS);
        this.dropSelf(Blocks.CHAIN);
        this.dropSelf(Blocks.WARPED_ROOTS);
        this.dropSelf(Blocks.CRIMSON_ROOTS);
        this.dropSelf(Blocks.AMETHYST_BLOCK);
        this.dropSelf(Blocks.CALCITE);
        this.dropSelf(Blocks.TUFF);
        this.dropSelf(Blocks.TINTED_GLASS);
        this.dropSelf(Blocks.COPPER_BLOCK);
        this.dropSelf(Blocks.LIGHTLY_WEATHERED_COPPER_BLOCK);
        this.dropSelf(Blocks.SEMI_WEATHERED_COPPER_BLOCK);
        this.dropSelf(Blocks.WEATHERED_COPPER_BLOCK);
        this.dropSelf(Blocks.COPPER_ORE);
        this.dropSelf(Blocks.CUT_COPPER);
        this.dropSelf(Blocks.LIGHTLY_WEATHERED_CUT_COPPER);
        this.dropSelf(Blocks.SEMI_WEATHERED_CUT_COPPER);
        this.dropSelf(Blocks.WEATHERED_CUT_COPPER);
        this.dropSelf(Blocks.WAXED_COPPER);
        this.dropSelf(Blocks.WAXED_SEMI_WEATHERED_COPPER);
        this.dropSelf(Blocks.WAXED_LIGHTLY_WEATHERED_COPPER);
        this.dropSelf(Blocks.WAXED_CUT_COPPER);
        this.dropSelf(Blocks.WAXED_SEMI_WEATHERED_CUT_COPPER);
        this.dropSelf(Blocks.WAXED_LIGHTLY_WEATHERED_CUT_COPPER);
        this.dropSelf(Blocks.WAXED_CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.WAXED_LIGHTLY_WEATHERED_CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.WAXED_SEMI_WEATHERED_CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.WAXED_CUT_COPPER_SLAB);
        this.dropSelf(Blocks.WAXED_LIGHTLY_WEATHERED_CUT_COPPER_SLAB);
        this.dropSelf(Blocks.WAXED_SEMI_WEATHERED_CUT_COPPER_SLAB);
        this.dropSelf(Blocks.CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.LIGHTLY_WEATHERED_CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.SEMI_WEATHERED_CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.WEATHERED_CUT_COPPER_STAIRS);
        this.dropSelf(Blocks.CUT_COPPER_SLAB);
        this.dropSelf(Blocks.LIGHTLY_WEATHERED_CUT_COPPER_SLAB);
        this.dropSelf(Blocks.SEMI_WEATHERED_CUT_COPPER_SLAB);
        this.dropSelf(Blocks.WEATHERED_CUT_COPPER_SLAB);
        this.dropSelf(Blocks.LIGHTNING_ROD);
        this.dropOther(Blocks.FARMLAND, Blocks.DIRT);
        this.dropOther(Blocks.TRIPWIRE, Items.STRING);
        this.dropOther(Blocks.DIRT_PATH, Blocks.DIRT);
        this.dropOther(Blocks.KELP_PLANT, Blocks.KELP);
        this.dropOther(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO);
        this.dropOther(Blocks.WATER_CAULDRON, Blocks.CAULDRON);
        this.dropOther(Blocks.LAVA_CAULDRON, Blocks.CAULDRON);
        this.add(Blocks.STONE, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.COBBLESTONE));
        this.add(Blocks.GRASS_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        this.add(Blocks.PODZOL, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        this.add(Blocks.MYCELIUM, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DIRT));
        this.add(Blocks.TUBE_CORAL_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_TUBE_CORAL_BLOCK));
        this.add(Blocks.BRAIN_CORAL_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_BRAIN_CORAL_BLOCK));
        this.add(Blocks.BUBBLE_CORAL_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_BUBBLE_CORAL_BLOCK));
        this.add(Blocks.FIRE_CORAL_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_FIRE_CORAL_BLOCK));
        this.add(Blocks.HORN_CORAL_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.DEAD_HORN_CORAL_BLOCK));
        this.add(Blocks.CRIMSON_NYLIUM, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.NETHERRACK));
        this.add(Blocks.WARPED_NYLIUM, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.NETHERRACK));
        this.add(Blocks.BOOKSHELF, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Items.BOOK, ConstantIntValue.exactly(3)));
        this.add(Blocks.CLAY, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Items.CLAY_BALL, ConstantIntValue.exactly(4)));
        this.add(Blocks.ENDER_CHEST, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Blocks.OBSIDIAN, ConstantIntValue.exactly(8)));
        this.add(Blocks.SNOW_BLOCK, (Block block) -> BlockLoot.createSingleItemTableWithSilkTouch(block, Items.SNOWBALL, ConstantIntValue.exactly(4)));
        this.add(Blocks.CHORUS_PLANT, BlockLoot.createSingleItemTable(Items.CHORUS_FRUIT, RandomValueBounds.between(0.0f, 1.0f)));
        this.dropPottedContents(Blocks.POTTED_OAK_SAPLING);
        this.dropPottedContents(Blocks.POTTED_SPRUCE_SAPLING);
        this.dropPottedContents(Blocks.POTTED_BIRCH_SAPLING);
        this.dropPottedContents(Blocks.POTTED_JUNGLE_SAPLING);
        this.dropPottedContents(Blocks.POTTED_ACACIA_SAPLING);
        this.dropPottedContents(Blocks.POTTED_DARK_OAK_SAPLING);
        this.dropPottedContents(Blocks.POTTED_FERN);
        this.dropPottedContents(Blocks.POTTED_DANDELION);
        this.dropPottedContents(Blocks.POTTED_POPPY);
        this.dropPottedContents(Blocks.POTTED_BLUE_ORCHID);
        this.dropPottedContents(Blocks.POTTED_ALLIUM);
        this.dropPottedContents(Blocks.POTTED_AZURE_BLUET);
        this.dropPottedContents(Blocks.POTTED_RED_TULIP);
        this.dropPottedContents(Blocks.POTTED_ORANGE_TULIP);
        this.dropPottedContents(Blocks.POTTED_WHITE_TULIP);
        this.dropPottedContents(Blocks.POTTED_PINK_TULIP);
        this.dropPottedContents(Blocks.POTTED_OXEYE_DAISY);
        this.dropPottedContents(Blocks.POTTED_CORNFLOWER);
        this.dropPottedContents(Blocks.POTTED_LILY_OF_THE_VALLEY);
        this.dropPottedContents(Blocks.POTTED_WITHER_ROSE);
        this.dropPottedContents(Blocks.POTTED_RED_MUSHROOM);
        this.dropPottedContents(Blocks.POTTED_BROWN_MUSHROOM);
        this.dropPottedContents(Blocks.POTTED_DEAD_BUSH);
        this.dropPottedContents(Blocks.POTTED_CACTUS);
        this.dropPottedContents(Blocks.POTTED_BAMBOO);
        this.dropPottedContents(Blocks.POTTED_CRIMSON_FUNGUS);
        this.dropPottedContents(Blocks.POTTED_WARPED_FUNGUS);
        this.dropPottedContents(Blocks.POTTED_CRIMSON_ROOTS);
        this.dropPottedContents(Blocks.POTTED_WARPED_ROOTS);
        this.add(Blocks.ACACIA_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.BIRCH_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.COBBLESTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.DARK_OAK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.DARK_PRISMARINE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.JUNGLE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.NETHER_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.OAK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PETRIFIED_OAK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PRISMARINE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PRISMARINE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.PURPUR_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.QUARTZ_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.CUT_RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.CUT_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SPRUCE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.STONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_STONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_GRANITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_RED_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.MOSSY_STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_DIORITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.MOSSY_COBBLESTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.END_STONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_SANDSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.SMOOTH_QUARTZ_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.GRANITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.ANDESITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.RED_NETHER_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_ANDESITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.DIORITE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.CRIMSON_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.WARPED_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.BLACKSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.POLISHED_BLACKSTONE_SLAB, BlockLoot::createSlabItemTable);
        this.add(Blocks.ACACIA_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.BIRCH_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.DARK_OAK_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.IRON_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.JUNGLE_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.OAK_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.SPRUCE_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.WARPED_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.CRIMSON_DOOR, BlockLoot::createDoorTable);
        this.add(Blocks.BLACK_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.BLUE_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.BROWN_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.CYAN_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.GRAY_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.GREEN_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LIGHT_BLUE_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LIGHT_GRAY_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LIME_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.MAGENTA_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.PURPLE_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.ORANGE_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.PINK_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.RED_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.WHITE_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.YELLOW_BED, (Block block) -> BlockLoot.createSinglePropConditionTable(block, BedBlock.PART, BedPart.HEAD));
        this.add(Blocks.LILAC, (Block block) -> BlockLoot.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.SUNFLOWER, (Block block) -> BlockLoot.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.PEONY, (Block block) -> BlockLoot.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.ROSE_BUSH, (Block block) -> BlockLoot.createSinglePropConditionTable(block, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
        this.add(Blocks.TNT, LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(Blocks.TNT, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Blocks.TNT).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TNT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TntBlock.UNSTABLE, false)))))));
        this.add(Blocks.COCOA, (Block block) -> LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(Items.COCOA_BEANS).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(3)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CocoaBlock.AGE, 2))))))));
        this.add(Blocks.SEA_PICKLE, (Block block) -> LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(Blocks.SEA_PICKLE, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(block).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(2)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 2))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(3)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 3))))).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(ConstantIntValue.exactly(4)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 4))))))));
        this.add(Blocks.COMPOSTER, (Block block) -> LootTable.lootTable().withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(Items.COMPOSTER)))).withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.BONE_MEAL)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 8)))));
        this.add(Blocks.CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.WHITE_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.ORANGE_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.MAGENTA_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.LIGHT_BLUE_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.YELLOW_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.LIME_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.PINK_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.GRAY_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.LIGHT_GRAY_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.CYAN_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.PURPLE_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.BLUE_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.BROWN_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.GREEN_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.RED_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.BLACK_CANDLE, BlockLoot::createCandleDrops);
        this.add(Blocks.BEACON, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BREWING_STAND, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.DISPENSER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.DROPPER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.ENCHANTING_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.FURNACE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.HOPPER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.TRAPPED_CHEST, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.SMOKER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BLAST_FURNACE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BARREL, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.CARTOGRAPHY_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.FLETCHING_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.GRINDSTONE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.LECTERN, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.SMITHING_TABLE, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.STONECUTTER, BlockLoot::createNameableBlockEntityTable);
        this.add(Blocks.BELL, BlockLoot::createSingleItemTable);
        this.add(Blocks.LANTERN, BlockLoot::createSingleItemTable);
        this.add(Blocks.SOUL_LANTERN, BlockLoot::createSingleItemTable);
        this.add(Blocks.SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BLACK_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BLUE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BROWN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.CYAN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.GRAY_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.GREEN_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.LIGHT_BLUE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.LIGHT_GRAY_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.LIME_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.MAGENTA_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.ORANGE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.PINK_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.PURPLE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.RED_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.WHITE_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.YELLOW_SHULKER_BOX, BlockLoot::createShulkerBoxDrop);
        this.add(Blocks.BLACK_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.BLUE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.BROWN_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.CYAN_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.GRAY_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.GREEN_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.LIGHT_BLUE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.LIGHT_GRAY_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.LIME_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.MAGENTA_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.ORANGE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.PINK_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.PURPLE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.RED_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.WHITE_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.YELLOW_BANNER, BlockLoot::createBannerDrop);
        this.add(Blocks.PLAYER_HEAD, (Block block) -> LootTable.lootTable().withPool(BlockLoot.applyExplosionCondition(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(block).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("SkullOwner", "SkullOwner"))))));
        this.add(Blocks.BEE_NEST, BlockLoot::createBeeNestDrop);
        this.add(Blocks.BEEHIVE, BlockLoot::createBeeHiveDrop);
        this.add(Blocks.BIRCH_LEAVES, (Block block) -> BlockLoot.createLeavesDrops(block, Blocks.BIRCH_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.ACACIA_LEAVES, (Block block) -> BlockLoot.createLeavesDrops(block, Blocks.ACACIA_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.JUNGLE_LEAVES, (Block block) -> BlockLoot.createLeavesDrops(block, Blocks.JUNGLE_SAPLING, JUNGLE_LEAVES_SAPLING_CHANGES));
        this.add(Blocks.SPRUCE_LEAVES, (Block block) -> BlockLoot.createLeavesDrops(block, Blocks.SPRUCE_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.OAK_LEAVES, (Block block) -> BlockLoot.createOakLeavesDrops(block, Blocks.OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        this.add(Blocks.DARK_OAK_LEAVES, (Block block) -> BlockLoot.createOakLeavesDrops(block, Blocks.DARK_OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
        LootItemBlockStatePropertyCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BEETROOTS).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BeetrootBlock.AGE, 3));
        this.add(Blocks.BEETROOTS, BlockLoot.createCropDrops(Blocks.BEETROOTS, Items.BEETROOT, Items.BEETROOT_SEEDS, builder));
        LootItemBlockStatePropertyCondition.Builder builder2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7));
        this.add(Blocks.WHEAT, BlockLoot.createCropDrops(Blocks.WHEAT, Items.WHEAT, Items.WHEAT_SEEDS, builder2));
        LootItemBlockStatePropertyCondition.Builder builder3 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CARROTS).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CarrotBlock.AGE, 7));
        this.add(Blocks.CARROTS, BlockLoot.applyExplosionDecay(Blocks.CARROTS, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.CARROT))).withPool(LootPool.lootPool().when(builder3).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.CARROT).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286f, 3))))));
        LootItemBlockStatePropertyCondition.Builder builder4 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTATOES).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PotatoBlock.AGE, 7));
        this.add(Blocks.POTATOES, BlockLoot.applyExplosionDecay(Blocks.POTATOES, LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.POTATO))).withPool(LootPool.lootPool().when(builder4).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.POTATO).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286f, 3)))).withPool(LootPool.lootPool().when(builder4).add((LootPoolEntryContainer.Builder<?>)LootItem.lootTableItem(Items.POISONOUS_POTATO).when(LootItemRandomChanceCondition.randomChance(0.02f))))));
        this.add(Blocks.SWEET_BERRY_BUSH, (Block block) -> BlockLoot.applyExplosionDecay(block, LootTable.lootTable().withPool(LootPool.lootPool().when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 3))).add(LootItem.lootTableItem(Items.SWEET_BERRIES)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 3.0f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))).withPool(LootPool.lootPool().when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 2))).add(LootItem.lootTableItem(Items.SWEET_BERRIES)).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0f, 2.0f))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))));
        this.add(Blocks.BROWN_MUSHROOM_BLOCK, (Block block) -> BlockLoot.createMushroomBlockDrop(block, Blocks.BROWN_MUSHROOM));
        this.add(Blocks.RED_MUSHROOM_BLOCK, (Block block) -> BlockLoot.createMushroomBlockDrop(block, Blocks.RED_MUSHROOM));
        this.add(Blocks.COAL_ORE, (Block block) -> BlockLoot.createOreDrop(block, Items.COAL));
        this.add(Blocks.EMERALD_ORE, (Block block) -> BlockLoot.createOreDrop(block, Items.EMERALD));
        this.add(Blocks.NETHER_QUARTZ_ORE, (Block block) -> BlockLoot.createOreDrop(block, Items.QUARTZ));
        this.add(Blocks.DIAMOND_ORE, (Block block) -> BlockLoot.createOreDrop(block, Items.DIAMOND));
        this.add(Blocks.NETHER_GOLD_ORE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.GOLD_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 6.0f)))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))));
        this.add(Blocks.LAPIS_ORE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.LAPIS_LAZULI).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0f, 9.0f)))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))));
        this.add(Blocks.COBWEB, (Block block) -> BlockLoot.createSilkTouchOrShearsDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(Items.STRING))));
        this.add(Blocks.DEAD_BUSH, (Block block) -> BlockLoot.createShearsDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0f, 2.0f))))));
        this.add(Blocks.NETHER_SPROUTS, BlockLoot::createShearsOnlyDrop);
        this.add(Blocks.SEAGRASS, BlockLoot::createShearsOnlyDrop);
        this.add(Blocks.VINE, BlockLoot::createShearsOnlyDrop);
        this.add(Blocks.TALL_SEAGRASS, BlockLoot.createDoublePlantShearsDrop(Blocks.SEAGRASS));
        this.add(Blocks.LARGE_FERN, (Block block) -> BlockLoot.createDoublePlantWithSeedDrops(block, Blocks.FERN));
        this.add(Blocks.TALL_GRASS, (Block block) -> BlockLoot.createDoublePlantWithSeedDrops(block, Blocks.GRASS));
        this.add(Blocks.MELON_STEM, (Block block) -> BlockLoot.createStemDrops(block, Items.MELON_SEEDS));
        this.add(Blocks.ATTACHED_MELON_STEM, (Block block) -> BlockLoot.createAttachedStemDrops(block, Items.MELON_SEEDS));
        this.add(Blocks.PUMPKIN_STEM, (Block block) -> BlockLoot.createStemDrops(block, Items.PUMPKIN_SEEDS));
        this.add(Blocks.ATTACHED_PUMPKIN_STEM, (Block block) -> BlockLoot.createAttachedStemDrops(block, Items.PUMPKIN_SEEDS));
        this.add(Blocks.CHORUS_FLOWER, (Block block) -> LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(block))).when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS)))));
        this.add(Blocks.FERN, BlockLoot::createGrassDrops);
        this.add(Blocks.GRASS, BlockLoot::createGrassDrops);
        this.add(Blocks.GLOWSTONE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.GLOWSTONE_DUST).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 4.0f)))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))).apply(LimitCount.limitCount(IntLimiter.clamp(1, 4))))));
        this.add(Blocks.MELON, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.MELON_SLICE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0f, 7.0f)))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))).apply(LimitCount.limitCount(IntLimiter.upperBound(9))))));
        this.add(Blocks.REDSTONE_ORE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0f, 5.0f)))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)))));
        this.add(Blocks.SEA_LANTERN, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 3.0f)))).apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))).apply(LimitCount.limitCount(IntLimiter.clamp(1, 5))))));
        this.add(Blocks.NETHER_WART, (Block block) -> LootTable.lootTable().withPool(BlockLoot.applyExplosionDecay(block, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add((LootPoolEntryContainer.Builder<?>)((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.NETHER_WART).apply((LootItemFunction.Builder)SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 4.0f)).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))))).apply((LootItemFunction.Builder)ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))))))));
        this.add(Blocks.SNOW, (Block block) -> LootTable.lootTable().withPool(LootPool.lootPool().when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS)).add(AlternativesEntry.alternatives(new LootPoolEntryContainer.Builder[]{AlternativesEntry.alternatives(new LootPoolEntryContainer.Builder[]{LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, true))), ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2))), ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3))), ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4))), ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(5))), ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(6))), ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.SNOWBALL).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7)))).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(7))), LootItem.lootTableItem(Items.SNOWBALL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(8)))}).when(HAS_NO_SILK_TOUCH), AlternativesEntry.alternatives(new LootPoolEntryContainer.Builder[]{LootItem.lootTableItem(Blocks.SNOW).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, true))), ((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Blocks.SNOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2))), ((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Blocks.SNOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3)))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3))), ((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Blocks.SNOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4)))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4))), ((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Blocks.SNOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(5)))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5))), ((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Blocks.SNOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(6)))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6))), ((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Blocks.SNOW).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(7)))).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7))), LootItem.lootTableItem(Blocks.SNOW_BLOCK)})}))));
        this.add(Blocks.GRAVEL, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, BlockLoot.applyExplosionCondition(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.FLINT).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1f, 0.14285715f, 0.25f, 1.0f))).otherwise(LootItem.lootTableItem(block)))));
        this.add(Blocks.CAMPFIRE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2))))));
        this.add(Blocks.GILDED_BLACKSTONE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, BlockLoot.applyExplosionCondition(block, ((LootPoolSingletonContainer.Builder)((LootPoolEntryContainer.Builder)LootItem.lootTableItem(Items.GOLD_NUGGET).apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0f, 5.0f)))).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1f, 0.14285715f, 0.25f, 1.0f))).otherwise(LootItem.lootTableItem(block)))));
        this.add(Blocks.SOUL_CAMPFIRE, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionCondition(block, LootItem.lootTableItem(Items.SOUL_SOIL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1))))));
        this.add(Blocks.AMETHYST_CLUSTER, (Block block) -> BlockLoot.createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder)BlockLoot.applyExplosionDecay(block, ((LootPoolSingletonContainer.Builder)LootItem.lootTableItem(Items.AMETHYST_SHARD).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4)))).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE)))));
        this.dropWhenSilkTouch(Blocks.SMALL_AMETHYST_BUD);
        this.dropWhenSilkTouch(Blocks.MEDIUM_AMETHYST_BUD);
        this.dropWhenSilkTouch(Blocks.LARGE_AMETHYST_BUD);
        this.dropWhenSilkTouch(Blocks.GLASS);
        this.dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.RED_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS);
        this.dropWhenSilkTouch(Blocks.GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.WHITE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.ORANGE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.MAGENTA_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.LIGHT_BLUE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.YELLOW_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.LIME_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.PINK_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.GRAY_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.LIGHT_GRAY_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.CYAN_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.PURPLE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.BLUE_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.BROWN_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.GREEN_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.RED_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.BLACK_STAINED_GLASS_PANE);
        this.dropWhenSilkTouch(Blocks.ICE);
        this.dropWhenSilkTouch(Blocks.PACKED_ICE);
        this.dropWhenSilkTouch(Blocks.BLUE_ICE);
        this.dropWhenSilkTouch(Blocks.TURTLE_EGG);
        this.dropWhenSilkTouch(Blocks.MUSHROOM_STEM);
        this.dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL);
        this.dropWhenSilkTouch(Blocks.TUBE_CORAL);
        this.dropWhenSilkTouch(Blocks.BRAIN_CORAL);
        this.dropWhenSilkTouch(Blocks.BUBBLE_CORAL);
        this.dropWhenSilkTouch(Blocks.FIRE_CORAL);
        this.dropWhenSilkTouch(Blocks.HORN_CORAL);
        this.dropWhenSilkTouch(Blocks.DEAD_TUBE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_BRAIN_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_BUBBLE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_FIRE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.DEAD_HORN_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.TUBE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.BRAIN_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.BUBBLE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.FIRE_CORAL_FAN);
        this.dropWhenSilkTouch(Blocks.HORN_CORAL_FAN);
        this.otherWhenSilkTouch(Blocks.INFESTED_STONE, Blocks.STONE);
        this.otherWhenSilkTouch(Blocks.INFESTED_COBBLESTONE, Blocks.COBBLESTONE);
        this.otherWhenSilkTouch(Blocks.INFESTED_STONE_BRICKS, Blocks.STONE_BRICKS);
        this.otherWhenSilkTouch(Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS);
        this.otherWhenSilkTouch(Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.CRACKED_STONE_BRICKS);
        this.otherWhenSilkTouch(Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS);
        this.addNetherVinesDropTable(Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT);
        this.addNetherVinesDropTable(Blocks.TWISTING_VINES, Blocks.TWISTING_VINES_PLANT);
        this.add(Blocks.CAKE, BlockLoot.noDrop());
        this.add(Blocks.CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.CANDLE));
        this.add(Blocks.WHITE_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.WHITE_CANDLE));
        this.add(Blocks.ORANGE_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.ORANGE_CANDLE));
        this.add(Blocks.MAGENTA_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.MAGENTA_CANDLE));
        this.add(Blocks.LIGHT_BLUE_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.LIGHT_BLUE_CANDLE));
        this.add(Blocks.YELLOW_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.YELLOW_CANDLE));
        this.add(Blocks.LIME_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.LIME_CANDLE));
        this.add(Blocks.PINK_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.PINK_CANDLE));
        this.add(Blocks.GRAY_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.GRAY_CANDLE));
        this.add(Blocks.LIGHT_GRAY_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.LIGHT_GRAY_CANDLE));
        this.add(Blocks.CYAN_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.CYAN_CANDLE));
        this.add(Blocks.PURPLE_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.PURPLE_CANDLE));
        this.add(Blocks.BLUE_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.BLUE_CANDLE));
        this.add(Blocks.BROWN_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.BROWN_CANDLE));
        this.add(Blocks.GREEN_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.GREEN_CANDLE));
        this.add(Blocks.RED_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.RED_CANDLE));
        this.add(Blocks.BLACK_CANDLE_CAKE, BlockLoot.createCandleCakeDrops(Blocks.BLACK_CANDLE));
        this.add(Blocks.FROSTED_ICE, BlockLoot.noDrop());
        this.add(Blocks.SPAWNER, BlockLoot.noDrop());
        this.add(Blocks.FIRE, BlockLoot.noDrop());
        this.add(Blocks.SOUL_FIRE, BlockLoot.noDrop());
        this.add(Blocks.NETHER_PORTAL, BlockLoot.noDrop());
        this.add(Blocks.BUDDING_AMETHYST, BlockLoot.noDrop());
        HashSet<ResourceLocation> set = Sets.newHashSet();
        for (Block block2 : Registry.BLOCK) {
            ResourceLocation resourceLocation = block2.getLootTable();
            if (resourceLocation == BuiltInLootTables.EMPTY || !set.add(resourceLocation)) continue;
            LootTable.Builder builder5 = this.map.remove(resourceLocation);
            if (builder5 == null) {
                throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", resourceLocation, Registry.BLOCK.getKey(block2)));
            }
            biConsumer.accept(resourceLocation, builder5);
        }
        if (!this.map.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
        }
    }

    private void addNetherVinesDropTable(Block block, Block block2) {
        LootTable.Builder builder = BlockLoot.createSilkTouchOrShearsDispatchTable(block, LootItem.lootTableItem(block).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.33f, 0.55f, 0.77f, 1.0f)));
        this.add(block, builder);
        this.add(block2, builder);
    }

    public static LootTable.Builder createDoorTable(Block block) {
        return BlockLoot.createSinglePropConditionTable(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    public void dropPottedContents(Block block2) {
        this.add(block2, (Block block) -> BlockLoot.createPotFlowerItemTable(((FlowerPotBlock)block).getContent()));
    }

    public void otherWhenSilkTouch(Block block, Block block2) {
        this.add(block, BlockLoot.createSilkTouchOnlyTable(block2));
    }

    public void dropOther(Block block, ItemLike itemLike) {
        this.add(block, BlockLoot.createSingleItemTable(itemLike));
    }

    public void dropWhenSilkTouch(Block block) {
        this.otherWhenSilkTouch(block, block);
    }

    public void dropSelf(Block block) {
        this.dropOther(block, block);
    }

    private void add(Block block, Function<Block, LootTable.Builder> function) {
        this.add(block, function.apply(block));
    }

    private void add(Block block, LootTable.Builder builder) {
        this.map.put(block.getLootTable(), builder);
    }

    @Override
    public /* synthetic */ void accept(Object object) {
        this.accept((BiConsumer)object);
    }
}

