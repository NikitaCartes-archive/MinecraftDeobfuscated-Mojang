package net.minecraft.data.loot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CaveVines;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.PinkPetalsBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
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
import net.minecraft.world.level.storage.loot.functions.SetContainerContents;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.predicates.LocationCheck;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.BinomialDistributionGenerator;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public abstract class BlockLootSubProvider implements LootTableSubProvider {
	protected static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(
		ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))
	);
	protected static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
	protected static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
	private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
	private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
	private final Set<Item> explosionResistant;
	private final FeatureFlagSet enabledFeatures;
	private final Map<ResourceLocation, LootTable.Builder> map = new HashMap();
	protected static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
	private static final float[] NORMAL_LEAVES_STICK_CHANCES = new float[]{0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F};

	protected BlockLootSubProvider(Set<Item> set, FeatureFlagSet featureFlagSet) {
		this.explosionResistant = set;
		this.enabledFeatures = featureFlagSet;
	}

	protected <T extends FunctionUserBuilder<T>> T applyExplosionDecay(ItemLike itemLike, FunctionUserBuilder<T> functionUserBuilder) {
		return !this.explosionResistant.contains(itemLike.asItem()) ? functionUserBuilder.apply(ApplyExplosionDecay.explosionDecay()) : functionUserBuilder.unwrap();
	}

	protected <T extends ConditionUserBuilder<T>> T applyExplosionCondition(ItemLike itemLike, ConditionUserBuilder<T> conditionUserBuilder) {
		return !this.explosionResistant.contains(itemLike.asItem())
			? conditionUserBuilder.when(ExplosionCondition.survivesExplosion())
			: conditionUserBuilder.unwrap();
	}

	public LootTable.Builder createSingleItemTable(ItemLike itemLike) {
		return LootTable.lootTable()
			.withPool(this.applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(itemLike))));
	}

	private static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder builder, LootPoolEntryContainer.Builder<?> builder2) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block).when(builder).otherwise(builder2)));
	}

	protected static LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
		return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
	}

	protected static LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
		return createSelfDropDispatchTable(block, HAS_SHEARS, builder);
	}

	protected static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
		return createSelfDropDispatchTable(block, HAS_SHEARS_OR_SILK_TOUCH, builder);
	}

	protected LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike) {
		return createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder<?>)this.applyExplosionCondition(block, LootItem.lootTableItem(itemLike)));
	}

	protected LootTable.Builder createSingleItemTable(ItemLike itemLike, NumberProvider numberProvider) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
							itemLike, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(numberProvider))
						)
					)
			);
	}

	protected LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike, NumberProvider numberProvider) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(block, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(numberProvider)))
		);
	}

	private static LootTable.Builder createSilkTouchOnlyTable(ItemLike itemLike) {
		return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(itemLike)));
	}

	private LootTable.Builder createPotFlowerItemTable(ItemLike itemLike) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))
			)
			.withPool(this.applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(itemLike))));
	}

	protected LootTable.Builder createSlabItemTable(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
							block,
							LootItem.lootTableItem(block)
								.apply(
									SetItemCountFunction.setCount(ConstantValue.exactly(2.0F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))
										)
								)
						)
					)
			);
	}

	protected <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(Block block, Property<T> property, T comparable) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(
							LootItem.lootTableItem(block)
								.when(
									LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(property, comparable))
								)
						)
				)
			);
	}

	protected LootTable.Builder createNameableBlockEntityTable(Block block) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(block).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))
				)
			);
	}

	protected LootTable.Builder createShulkerBoxDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(
							LootItem.lootTableItem(block)
								.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
								.apply(
									CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
										.copy("Lock", "BlockEntityTag.Lock")
										.copy("LootTable", "BlockEntityTag.LootTable")
										.copy("LootTableSeed", "BlockEntityTag.LootTableSeed")
								)
								.apply(SetContainerContents.setContents(BlockEntityType.SHULKER_BOX).withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS)))
						)
				)
			);
	}

	protected LootTable.Builder createCopperOreDrops(Block block) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
				block,
				LootItem.lootTableItem(Items.RAW_COPPER)
					.apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F)))
					.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
			)
		);
	}

	protected LootTable.Builder createLapisOreDrops(Block block) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
				block,
				LootItem.lootTableItem(Items.LAPIS_LAZULI)
					.apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 9.0F)))
					.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
			)
		);
	}

	protected LootTable.Builder createRedstoneOreDrops(Block block) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
				block,
				LootItem.lootTableItem(Items.REDSTONE)
					.apply(SetItemCountFunction.setCount(UniformGenerator.between(4.0F, 5.0F)))
					.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
			)
		);
	}

	protected LootTable.Builder createBannerDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(
							LootItem.lootTableItem(block)
								.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
								.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns"))
						)
				)
			);
	}

	protected static LootTable.Builder createBeeNestDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.when(HAS_SILK_TOUCH)
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						LootItem.lootTableItem(block)
							.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
							.apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))
					)
			);
	}

	protected static LootTable.Builder createBeeHiveDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						LootItem.lootTableItem(block)
							.when(HAS_SILK_TOUCH)
							.apply(CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
							.apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))
							.otherwise(LootItem.lootTableItem(block))
					)
			);
	}

	protected static LootTable.Builder createCaveVinesDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.add(LootItem.lootTableItem(Items.GLOW_BERRIES))
					.when(
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
							.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CaveVines.BERRIES, true))
					)
			);
	}

	protected LootTable.Builder createOreDrop(Block block, Item item) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
				block, LootItem.lootTableItem(item).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
			)
		);
	}

	protected LootTable.Builder createMushroomBlockDrop(Block block, ItemLike itemLike) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
				block,
				LootItem.lootTableItem(itemLike)
					.apply(SetItemCountFunction.setCount(UniformGenerator.between(-6.0F, 2.0F)))
					.apply(LimitCount.limitCount(IntRange.lowerBound(0)))
			)
		);
	}

	protected LootTable.Builder createGrassDrops(Block block) {
		return createShearsDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
				block,
				LootItem.lootTableItem(Items.WHEAT_SEEDS)
					.when(LootItemRandomChanceCondition.randomChance(0.125F))
					.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))
			)
		);
	}

	public LootTable.Builder createStemDrops(Block block, Item item) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionDecay(
					block,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(
							LootItem.lootTableItem(item)
								.apply(
									StemBlock.AGE.getPossibleValues(),
									integer -> SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, (float)(integer + 1) / 15.0F))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, integer))
											)
								)
						)
				)
			);
	}

	public LootTable.Builder createAttachedStemDrops(Block block, Item item) {
		return LootTable.lootTable()
			.withPool(
				this.applyExplosionDecay(
					block,
					LootPool.lootPool()
						.setRolls(ConstantValue.exactly(1.0F))
						.add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))
				)
			);
	}

	protected static LootTable.Builder createShearsOnlyDrop(ItemLike itemLike) {
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).when(HAS_SHEARS).add(LootItem.lootTableItem(itemLike)));
	}

	protected LootTable.Builder createMultifaceBlockDrops(Block block, LootItemCondition.Builder builder) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.add(
						(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
							block,
							LootItem.lootTableItem(block)
								.when(builder)
								.apply(
									Direction.values(),
									direction -> SetItemCountFunction.setCount(ConstantValue.exactly(1.0F), true)
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(MultifaceBlock.getFaceProperty(direction), true))
											)
								)
								.apply(SetItemCountFunction.setCount(ConstantValue.exactly(-1.0F), true))
						)
					)
			);
	}

	protected LootTable.Builder createLeavesDrops(Block block, Block block2, float... fs) {
		return createSilkTouchOrShearsDispatchTable(
				block,
				((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(block2)))
					.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, fs))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.when(HAS_NO_SHEARS_OR_SILK_TOUCH)
					.add(
						((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(
								block, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
							))
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, NORMAL_LEAVES_STICK_CHANCES))
					)
			);
	}

	protected LootTable.Builder createOakLeavesDrops(Block block, Block block2, float... fs) {
		return this.createLeavesDrops(block, block2, fs)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.when(HAS_NO_SHEARS_OR_SILK_TOUCH)
					.add(
						((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(Items.APPLE)))
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F))
					)
			);
	}

	protected LootTable.Builder createMangroveLeavesDrops(Block block) {
		return createSilkTouchOrShearsDispatchTable(
			block,
			((LootPoolSingletonContainer.Builder)this.applyExplosionDecay(
					Blocks.MANGROVE_LEAVES, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
				))
				.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, NORMAL_LEAVES_STICK_CHANCES))
		);
	}

	protected LootTable.Builder createCropDrops(Block block, Item item, Item item2, LootItemCondition.Builder builder) {
		return this.applyExplosionDecay(
			block,
			LootTable.lootTable()
				.withPool(LootPool.lootPool().add(LootItem.lootTableItem(item).when(builder).otherwise(LootItem.lootTableItem(item2))))
				.withPool(
					LootPool.lootPool()
						.when(builder)
						.add(LootItem.lootTableItem(item2).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))
				)
		);
	}

	protected static LootTable.Builder createDoublePlantShearsDrop(Block block) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().when(HAS_SHEARS).add(LootItem.lootTableItem(block).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))));
	}

	protected LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block block2) {
		LootPoolEntryContainer.Builder<?> builder = LootItem.lootTableItem(block2)
			.apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
			.when(HAS_SHEARS)
			.otherwise(
				((LootPoolSingletonContainer.Builder)this.applyExplosionCondition(block, LootItem.lootTableItem(Items.WHEAT_SEEDS)))
					.when(LootItemRandomChanceCondition.randomChance(0.125F))
			);
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.add(builder)
					.when(
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
							.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER))
					)
					.when(
						LocationCheck.checkLocation(
							LocationPredicate.Builder.location()
								.setBlock(
									BlockPredicate.Builder.block()
										.of(block)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER).build())
										.build()
								),
							new BlockPos(0, 1, 0)
						)
					)
			)
			.withPool(
				LootPool.lootPool()
					.add(builder)
					.when(
						LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
							.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER))
					)
					.when(
						LocationCheck.checkLocation(
							LocationPredicate.Builder.location()
								.setBlock(
									BlockPredicate.Builder.block()
										.of(block)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER).build())
										.build()
								),
							new BlockPos(0, -1, 0)
						)
					)
			);
	}

	protected LootTable.Builder createCandleDrops(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
							block,
							LootItem.lootTableItem(block)
								.apply(
									List.of(2, 3, 4),
									integer -> SetItemCountFunction.setCount(ConstantValue.exactly((float)integer.intValue()))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CandleBlock.CANDLES, integer))
											)
								)
						)
					)
			);
	}

	protected LootTable.Builder createPetalsDrops(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.add(
						(LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
							block,
							LootItem.lootTableItem(block)
								.apply(
									IntStream.rangeClosed(1, 4).boxed().toList(),
									integer -> SetItemCountFunction.setCount(ConstantValue.exactly((float)integer.intValue()))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PinkPetalsBlock.AMOUNT, integer))
											)
								)
						)
					)
			);
	}

	protected static LootTable.Builder createCandleCakeDrops(Block block) {
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(block)));
	}

	public static LootTable.Builder noDrop() {
		return LootTable.lootTable();
	}

	protected abstract void generate();

	@Override
	public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
		this.generate();
		Set<ResourceLocation> set = new HashSet();

		for (Block block : BuiltInRegistries.BLOCK) {
			if (block.isEnabled(this.enabledFeatures)) {
				ResourceLocation resourceLocation = block.getLootTable();
				if (resourceLocation != BuiltInLootTables.EMPTY && set.add(resourceLocation)) {
					LootTable.Builder builder = (LootTable.Builder)this.map.remove(resourceLocation);
					if (builder == null) {
						throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", resourceLocation, BuiltInRegistries.BLOCK.getKey(block)));
					}

					biConsumer.accept(resourceLocation, builder);
				}
			}
		}

		if (!this.map.isEmpty()) {
			throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
		}
	}

	protected void addNetherVinesDropTable(Block block, Block block2) {
		LootTable.Builder builder = createSilkTouchOrShearsDispatchTable(
			block, LootItem.lootTableItem(block).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.33F, 0.55F, 0.77F, 1.0F))
		);
		this.add(block, builder);
		this.add(block2, builder);
	}

	protected LootTable.Builder createDoorTable(Block block) {
		return this.createSinglePropConditionTable(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
	}

	protected void dropPottedContents(Block block) {
		this.add(block, blockx -> this.createPotFlowerItemTable(((FlowerPotBlock)blockx).getContent()));
	}

	protected void otherWhenSilkTouch(Block block, Block block2) {
		this.add(block, createSilkTouchOnlyTable(block2));
	}

	protected void dropOther(Block block, ItemLike itemLike) {
		this.add(block, this.createSingleItemTable(itemLike));
	}

	protected void dropWhenSilkTouch(Block block) {
		this.otherWhenSilkTouch(block, block);
	}

	protected void dropSelf(Block block) {
		this.dropOther(block, block);
	}

	protected void add(Block block, Function<Block, LootTable.Builder> function) {
		this.add(block, (LootTable.Builder)function.apply(block));
	}

	protected void add(Block block, LootTable.Builder builder) {
		this.map.put(block.getLootTable(), builder);
	}
}
