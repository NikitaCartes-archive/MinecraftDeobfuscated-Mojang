package net.minecraft.data.loot;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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

public class BlockLoot implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
	private static final LootItemCondition.Builder HAS_SILK_TOUCH = MatchTool.toolMatches(
		ItemPredicate.Builder.item().hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.Ints.atLeast(1)))
	);
	private static final LootItemCondition.Builder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
	private static final LootItemCondition.Builder HAS_SHEARS = MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
	private static final LootItemCondition.Builder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
	private static final LootItemCondition.Builder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();
	private static final Set<Item> EXPLOSION_RESISTANT = (Set<Item>)Stream.of(
			Blocks.DRAGON_EGG,
			Blocks.BEACON,
			Blocks.CONDUIT,
			Blocks.SKELETON_SKULL,
			Blocks.WITHER_SKELETON_SKULL,
			Blocks.PLAYER_HEAD,
			Blocks.ZOMBIE_HEAD,
			Blocks.CREEPER_HEAD,
			Blocks.DRAGON_HEAD,
			Blocks.SHULKER_BOX,
			Blocks.BLACK_SHULKER_BOX,
			Blocks.BLUE_SHULKER_BOX,
			Blocks.BROWN_SHULKER_BOX,
			Blocks.CYAN_SHULKER_BOX,
			Blocks.GRAY_SHULKER_BOX,
			Blocks.GREEN_SHULKER_BOX,
			Blocks.LIGHT_BLUE_SHULKER_BOX,
			Blocks.LIGHT_GRAY_SHULKER_BOX,
			Blocks.LIME_SHULKER_BOX,
			Blocks.MAGENTA_SHULKER_BOX,
			Blocks.ORANGE_SHULKER_BOX,
			Blocks.PINK_SHULKER_BOX,
			Blocks.PURPLE_SHULKER_BOX,
			Blocks.RED_SHULKER_BOX,
			Blocks.WHITE_SHULKER_BOX,
			Blocks.YELLOW_SHULKER_BOX
		)
		.map(ItemLike::asItem)
		.collect(ImmutableSet.toImmutableSet());
	private static final float[] NORMAL_LEAVES_SAPLING_CHANCES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
	private static final float[] JUNGLE_LEAVES_SAPLING_CHANGES = new float[]{0.025F, 0.027777778F, 0.03125F, 0.041666668F, 0.1F};
	private final Map<ResourceLocation, LootTable.Builder> map = Maps.<ResourceLocation, LootTable.Builder>newHashMap();

	private static <T> T applyExplosionDecay(ItemLike itemLike, FunctionUserBuilder<T> functionUserBuilder) {
		return !EXPLOSION_RESISTANT.contains(itemLike.asItem()) ? functionUserBuilder.apply(ApplyExplosionDecay.explosionDecay()) : functionUserBuilder.unwrap();
	}

	private static <T> T applyExplosionCondition(ItemLike itemLike, ConditionUserBuilder<T> conditionUserBuilder) {
		return !EXPLOSION_RESISTANT.contains(itemLike.asItem()) ? conditionUserBuilder.when(ExplosionCondition.survivesExplosion()) : conditionUserBuilder.unwrap();
	}

	private static LootTable.Builder createSingleItemTable(ItemLike itemLike) {
		return LootTable.lootTable()
			.withPool(applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike))));
	}

	private static LootTable.Builder createSelfDropDispatchTable(Block block, LootItemCondition.Builder builder, LootPoolEntryContainer.Builder<?> builder2) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(block).when(builder).otherwise(builder2)));
	}

	private static LootTable.Builder createSilkTouchDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
		return createSelfDropDispatchTable(block, HAS_SILK_TOUCH, builder);
	}

	private static LootTable.Builder createShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
		return createSelfDropDispatchTable(block, HAS_SHEARS, builder);
	}

	private static LootTable.Builder createSilkTouchOrShearsDispatchTable(Block block, LootPoolEntryContainer.Builder<?> builder) {
		return createSelfDropDispatchTable(block, HAS_SHEARS_OR_SILK_TOUCH, builder);
	}

	private static LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike) {
		return createSilkTouchDispatchTable(block, (LootPoolEntryContainer.Builder<?>)applyExplosionCondition(block, LootItem.lootTableItem(itemLike)));
	}

	private static LootTable.Builder createSingleItemTable(ItemLike itemLike, RandomIntGenerator randomIntGenerator) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantIntValue.exactly(1))
					.add(
						(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
							itemLike, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(randomIntGenerator))
						)
					)
			);
	}

	private static LootTable.Builder createSingleItemTableWithSilkTouch(Block block, ItemLike itemLike, RandomIntGenerator randomIntGenerator) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(block, LootItem.lootTableItem(itemLike).apply(SetItemCountFunction.setCount(randomIntGenerator)))
		);
	}

	private static LootTable.Builder createSilkTouchOnlyTable(ItemLike itemLike) {
		return LootTable.lootTable().withPool(LootPool.lootPool().when(HAS_SILK_TOUCH).setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike)));
	}

	private static LootTable.Builder createPotFlowerItemTable(ItemLike itemLike) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionCondition(Blocks.FLOWER_POT, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(Blocks.FLOWER_POT)))
			)
			.withPool(applyExplosionCondition(itemLike, LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).add(LootItem.lootTableItem(itemLike))));
	}

	private static LootTable.Builder createSlabItemTable(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantIntValue.exactly(1))
					.add(
						(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
							block,
							LootItem.lootTableItem(block)
								.apply(
									SetItemCountFunction.setCount(ConstantIntValue.exactly(2))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SlabBlock.TYPE, SlabType.DOUBLE))
										)
								)
						)
					)
			);
	}

	private static <T extends Comparable<T> & StringRepresentable> LootTable.Builder createSinglePropConditionTable(
		Block block, Property<T> property, T comparable
	) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
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

	private static LootTable.Builder createNameableBlockEntityTable(Block block) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(block).apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY)))
				)
			);
	}

	private static LootTable.Builder createShulkerBoxDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(block)
								.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
								.apply(
									CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY)
										.copy("Lock", "BlockEntityTag.Lock")
										.copy("LootTable", "BlockEntityTag.LootTable")
										.copy("LootTableSeed", "BlockEntityTag.LootTableSeed")
								)
								.apply(SetContainerContents.setContents().withEntry(DynamicLoot.dynamicEntry(ShulkerBoxBlock.CONTENTS)))
						)
				)
			);
	}

	private static LootTable.Builder createBannerDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionCondition(
					block,
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(block)
								.apply(CopyNameFunction.copyName(CopyNameFunction.NameSource.BLOCK_ENTITY))
								.apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Patterns", "BlockEntityTag.Patterns"))
						)
				)
			);
	}

	private static LootTable.Builder createBeeNestDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.when(HAS_SILK_TOUCH)
					.setRolls(ConstantIntValue.exactly(1))
					.add(
						LootItem.lootTableItem(block)
							.apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
							.apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))
					)
			);
	}

	private static LootTable.Builder createBeeHiveDrop(Block block) {
		return LootTable.lootTable()
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantIntValue.exactly(1))
					.add(
						LootItem.lootTableItem(block)
							.when(HAS_SILK_TOUCH)
							.apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("Bees", "BlockEntityTag.Bees"))
							.apply(CopyBlockState.copyState(block).copy(BeehiveBlock.HONEY_LEVEL))
							.otherwise(LootItem.lootTableItem(block))
					)
			);
	}

	private static LootTable.Builder createOreDrop(Block block, Item item) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
				block, LootItem.lootTableItem(item).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
			)
		);
	}

	private static LootTable.Builder createMushroomBlockDrop(Block block, ItemLike itemLike) {
		return createSilkTouchDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
				block,
				LootItem.lootTableItem(itemLike)
					.apply(SetItemCountFunction.setCount(RandomValueBounds.between(-6.0F, 2.0F)))
					.apply(LimitCount.limitCount(IntLimiter.lowerBound(0)))
			)
		);
	}

	private static LootTable.Builder createGrassDrops(Block block) {
		return createShearsDispatchTable(
			block,
			(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
				block,
				LootItem.lootTableItem(Items.WHEAT_SEEDS)
					.when(LootItemRandomChanceCondition.randomChance(0.125F))
					.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE, 2))
			)
		);
	}

	private static LootTable.Builder createStemDrops(Block block, Item item) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionDecay(
					block,
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(
							LootItem.lootTableItem(item)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.06666667F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 0))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.13333334F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 1))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.2F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 2))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.26666668F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 3))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.33333334F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 4))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.4F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 5))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.46666667F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 6))
										)
								)
								.apply(
									SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))
										.when(
											LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
												.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(StemBlock.AGE, 7))
										)
								)
						)
				)
			);
	}

	private static LootTable.Builder createAttachedStemDrops(Block block, Item item) {
		return LootTable.lootTable()
			.withPool(
				applyExplosionDecay(
					block,
					LootPool.lootPool()
						.setRolls(ConstantIntValue.exactly(1))
						.add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(BinomialDistributionGenerator.binomial(3, 0.53333336F))))
				)
			);
	}

	private static LootTable.Builder createShearsOnlyDrop(ItemLike itemLike) {
		return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantIntValue.exactly(1)).when(HAS_SHEARS).add(LootItem.lootTableItem(itemLike)));
	}

	private static LootTable.Builder createLeavesDrops(Block block, Block block2, float... fs) {
		return createSilkTouchOrShearsDispatchTable(
				block,
				((LootPoolSingletonContainer.Builder)applyExplosionCondition(block, LootItem.lootTableItem(block2)))
					.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, fs))
			)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantIntValue.exactly(1))
					.when(HAS_NO_SHEARS_OR_SILK_TOUCH)
					.add(
						((LootPoolSingletonContainer.Builder)applyExplosionDecay(
								block, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F)))
							))
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))
					)
			);
	}

	private static LootTable.Builder createOakLeavesDrops(Block block, Block block2, float... fs) {
		return createLeavesDrops(block, block2, fs)
			.withPool(
				LootPool.lootPool()
					.setRolls(ConstantIntValue.exactly(1))
					.when(HAS_NO_SHEARS_OR_SILK_TOUCH)
					.add(
						((LootPoolSingletonContainer.Builder)applyExplosionCondition(block, LootItem.lootTableItem(Items.APPLE)))
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.005F, 0.0055555557F, 0.00625F, 0.008333334F, 0.025F))
					)
			);
	}

	private static LootTable.Builder createCropDrops(Block block, Item item, Item item2, LootItemCondition.Builder builder) {
		return applyExplosionDecay(
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

	private static LootTable.Builder createDoublePlantShearsDrop(Block block) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool().when(HAS_SHEARS).add(LootItem.lootTableItem(block).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))));
	}

	private static LootTable.Builder createDoublePlantWithSeedDrops(Block block, Block block2) {
		LootPoolEntryContainer.Builder<?> builder = LootItem.lootTableItem(block2)
			.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))
			.when(HAS_SHEARS)
			.otherwise(
				((LootPoolSingletonContainer.Builder)applyExplosionCondition(block, LootItem.lootTableItem(Items.WHEAT_SEEDS)))
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

	public static LootTable.Builder noDrop() {
		return LootTable.lootTable();
	}

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
		this.dropOther(Blocks.FARMLAND, Blocks.DIRT);
		this.dropOther(Blocks.TRIPWIRE, Items.STRING);
		this.dropOther(Blocks.GRASS_PATH, Blocks.DIRT);
		this.dropOther(Blocks.KELP_PLANT, Blocks.KELP);
		this.dropOther(Blocks.BAMBOO_SAPLING, Blocks.BAMBOO);
		this.add(Blocks.STONE, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.COBBLESTONE));
		this.add(Blocks.GRASS_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DIRT));
		this.add(Blocks.PODZOL, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DIRT));
		this.add(Blocks.MYCELIUM, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DIRT));
		this.add(Blocks.TUBE_CORAL_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DEAD_TUBE_CORAL_BLOCK));
		this.add(Blocks.BRAIN_CORAL_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DEAD_BRAIN_CORAL_BLOCK));
		this.add(Blocks.BUBBLE_CORAL_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DEAD_BUBBLE_CORAL_BLOCK));
		this.add(Blocks.FIRE_CORAL_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DEAD_FIRE_CORAL_BLOCK));
		this.add(Blocks.HORN_CORAL_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.DEAD_HORN_CORAL_BLOCK));
		this.add(Blocks.CRIMSON_NYLIUM, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.NETHERRACK));
		this.add(Blocks.WARPED_NYLIUM, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.NETHERRACK));
		this.add(Blocks.BOOKSHELF, blockx -> createSingleItemTableWithSilkTouch(blockx, Items.BOOK, ConstantIntValue.exactly(3)));
		this.add(Blocks.CLAY, blockx -> createSingleItemTableWithSilkTouch(blockx, Items.CLAY_BALL, ConstantIntValue.exactly(4)));
		this.add(Blocks.ENDER_CHEST, blockx -> createSingleItemTableWithSilkTouch(blockx, Blocks.OBSIDIAN, ConstantIntValue.exactly(8)));
		this.add(Blocks.SNOW_BLOCK, blockx -> createSingleItemTableWithSilkTouch(blockx, Items.SNOWBALL, ConstantIntValue.exactly(4)));
		this.add(Blocks.CHORUS_PLANT, createSingleItemTable(Items.CHORUS_FRUIT, RandomValueBounds.between(0.0F, 1.0F)));
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
		this.add(Blocks.BLACK_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.BLUE_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.BROWN_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.CYAN_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.GRAY_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.GREEN_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LIGHT_BLUE_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LIGHT_GRAY_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LIME_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.MAGENTA_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.PURPLE_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.ORANGE_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.PINK_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.RED_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.WHITE_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.YELLOW_BED, blockx -> createSinglePropConditionTable(blockx, BedBlock.PART, BedPart.HEAD));
		this.add(Blocks.LILAC, blockx -> createSinglePropConditionTable(blockx, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(Blocks.SUNFLOWER, blockx -> createSinglePropConditionTable(blockx, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(Blocks.PEONY, blockx -> createSinglePropConditionTable(blockx, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(Blocks.ROSE_BUSH, blockx -> createSinglePropConditionTable(blockx, DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
		this.add(
			Blocks.TNT,
			LootTable.lootTable()
				.withPool(
					applyExplosionCondition(
						Blocks.TNT,
						LootPool.lootPool()
							.setRolls(ConstantIntValue.exactly(1))
							.add(
								LootItem.lootTableItem(Blocks.TNT)
									.when(
										LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TNT)
											.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(TntBlock.UNSTABLE, false))
									)
							)
					)
				)
		);
		this.add(
			Blocks.COCOA,
			blockx -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantIntValue.exactly(1))
							.add(
								(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
									blockx,
									LootItem.lootTableItem(Items.COCOA_BEANS)
										.apply(
											SetItemCountFunction.setCount(ConstantIntValue.exactly(3))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CocoaBlock.AGE, 2))
												)
										)
								)
							)
					)
		);
		this.add(
			Blocks.SEA_PICKLE,
			blockx -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantIntValue.exactly(1))
							.add(
								(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
									Blocks.SEA_PICKLE,
									LootItem.lootTableItem(blockx)
										.apply(
											SetItemCountFunction.setCount(ConstantIntValue.exactly(2))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 2))
												)
										)
										.apply(
											SetItemCountFunction.setCount(ConstantIntValue.exactly(3))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 3))
												)
										)
										.apply(
											SetItemCountFunction.setCount(ConstantIntValue.exactly(4))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SeaPickleBlock.PICKLES, 4))
												)
										)
								)
							)
					)
		);
		this.add(
			Blocks.COMPOSTER,
			blockx -> LootTable.lootTable()
					.withPool(LootPool.lootPool().add((LootPoolEntryContainer.Builder<?>)applyExplosionDecay(blockx, LootItem.lootTableItem(Items.COMPOSTER))))
					.withPool(
						LootPool.lootPool()
							.add(LootItem.lootTableItem(Items.BONE_MEAL))
							.when(
								LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
									.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(ComposterBlock.LEVEL, 8))
							)
					)
		);
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
		this.add(
			Blocks.PLAYER_HEAD,
			blockx -> LootTable.lootTable()
					.withPool(
						applyExplosionCondition(
							blockx,
							LootPool.lootPool()
								.setRolls(ConstantIntValue.exactly(1))
								.add(LootItem.lootTableItem(blockx).apply(CopyNbtFunction.copyData(CopyNbtFunction.DataSource.BLOCK_ENTITY).copy("SkullOwner", "SkullOwner")))
						)
					)
		);
		this.add(Blocks.BEE_NEST, BlockLoot::createBeeNestDrop);
		this.add(Blocks.BEEHIVE, BlockLoot::createBeeHiveDrop);
		this.add(Blocks.BIRCH_LEAVES, blockx -> createLeavesDrops(blockx, Blocks.BIRCH_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.ACACIA_LEAVES, blockx -> createLeavesDrops(blockx, Blocks.ACACIA_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.JUNGLE_LEAVES, blockx -> createLeavesDrops(blockx, Blocks.JUNGLE_SAPLING, JUNGLE_LEAVES_SAPLING_CHANGES));
		this.add(Blocks.SPRUCE_LEAVES, blockx -> createLeavesDrops(blockx, Blocks.SPRUCE_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.OAK_LEAVES, blockx -> createOakLeavesDrops(blockx, Blocks.OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		this.add(Blocks.DARK_OAK_LEAVES, blockx -> createOakLeavesDrops(blockx, Blocks.DARK_OAK_SAPLING, NORMAL_LEAVES_SAPLING_CHANCES));
		LootItemCondition.Builder builder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.BEETROOTS)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(BeetrootBlock.AGE, 3));
		this.add(Blocks.BEETROOTS, createCropDrops(Blocks.BEETROOTS, Items.BEETROOT, Items.BEETROOT_SEEDS, builder));
		LootItemCondition.Builder builder2 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.WHEAT)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7));
		this.add(Blocks.WHEAT, createCropDrops(Blocks.WHEAT, Items.WHEAT, Items.WHEAT_SEEDS, builder2));
		LootItemCondition.Builder builder3 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.CARROTS)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CarrotBlock.AGE, 7));
		this.add(
			Blocks.CARROTS,
			applyExplosionDecay(
				Blocks.CARROTS,
				LootTable.lootTable()
					.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.CARROT)))
					.withPool(
						LootPool.lootPool()
							.when(builder3)
							.add(LootItem.lootTableItem(Items.CARROT).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))
					)
			)
		);
		LootItemCondition.Builder builder4 = LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.POTATOES)
			.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(PotatoBlock.AGE, 7));
		this.add(
			Blocks.POTATOES,
			applyExplosionDecay(
				Blocks.POTATOES,
				LootTable.lootTable()
					.withPool(LootPool.lootPool().add(LootItem.lootTableItem(Items.POTATO)))
					.withPool(
						LootPool.lootPool()
							.when(builder4)
							.add(LootItem.lootTableItem(Items.POTATO).apply(ApplyBonusCount.addBonusBinomialDistributionCount(Enchantments.BLOCK_FORTUNE, 0.5714286F, 3)))
					)
					.withPool(LootPool.lootPool().when(builder4).add(LootItem.lootTableItem(Items.POISONOUS_POTATO).when(LootItemRandomChanceCondition.randomChance(0.02F))))
			)
		);
		this.add(
			Blocks.SWEET_BERRY_BUSH,
			blockx -> applyExplosionDecay(
					blockx,
					LootTable.lootTable()
						.withPool(
							LootPool.lootPool()
								.when(
									LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 3))
								)
								.add(LootItem.lootTableItem(Items.SWEET_BERRIES))
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F)))
								.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
						)
						.withPool(
							LootPool.lootPool()
								.when(
									LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SWEET_BERRY_BUSH)
										.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SweetBerryBushBlock.AGE, 2))
								)
								.add(LootItem.lootTableItem(Items.SWEET_BERRIES))
								.apply(SetItemCountFunction.setCount(RandomValueBounds.between(1.0F, 2.0F)))
								.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
						)
				)
		);
		this.add(Blocks.BROWN_MUSHROOM_BLOCK, blockx -> createMushroomBlockDrop(blockx, Blocks.BROWN_MUSHROOM));
		this.add(Blocks.RED_MUSHROOM_BLOCK, blockx -> createMushroomBlockDrop(blockx, Blocks.RED_MUSHROOM));
		this.add(Blocks.COAL_ORE, blockx -> createOreDrop(blockx, Items.COAL));
		this.add(Blocks.EMERALD_ORE, blockx -> createOreDrop(blockx, Items.EMERALD));
		this.add(Blocks.NETHER_QUARTZ_ORE, blockx -> createOreDrop(blockx, Items.QUARTZ));
		this.add(Blocks.DIAMOND_ORE, blockx -> createOreDrop(blockx, Items.DIAMOND));
		this.add(
			Blocks.NETHER_GOLD_ORE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx,
						LootItem.lootTableItem(Items.GOLD_NUGGET)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 6.0F)))
							.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
					)
				)
		);
		this.add(
			Blocks.LAPIS_ORE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx,
						LootItem.lootTableItem(Items.LAPIS_LAZULI)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 9.0F)))
							.apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
					)
				)
		);
		this.add(
			Blocks.COBWEB,
			blockx -> createSilkTouchOrShearsDispatchTable(
					blockx, (LootPoolEntryContainer.Builder<?>)applyExplosionCondition(blockx, LootItem.lootTableItem(Items.STRING))
				)
		);
		this.add(
			Blocks.DEAD_BUSH,
			blockx -> createShearsDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx, LootItem.lootTableItem(Items.STICK).apply(SetItemCountFunction.setCount(RandomValueBounds.between(0.0F, 2.0F)))
					)
				)
		);
		this.add(Blocks.NETHER_SPROUTS, BlockLoot::createShearsOnlyDrop);
		this.add(Blocks.SEAGRASS, BlockLoot::createShearsOnlyDrop);
		this.add(Blocks.VINE, BlockLoot::createShearsOnlyDrop);
		this.add(Blocks.TALL_SEAGRASS, createDoublePlantShearsDrop(Blocks.SEAGRASS));
		this.add(Blocks.LARGE_FERN, blockx -> createDoublePlantWithSeedDrops(blockx, Blocks.FERN));
		this.add(Blocks.TALL_GRASS, blockx -> createDoublePlantWithSeedDrops(blockx, Blocks.GRASS));
		this.add(Blocks.MELON_STEM, blockx -> createStemDrops(blockx, Items.MELON_SEEDS));
		this.add(Blocks.ATTACHED_MELON_STEM, blockx -> createAttachedStemDrops(blockx, Items.MELON_SEEDS));
		this.add(Blocks.PUMPKIN_STEM, blockx -> createStemDrops(blockx, Items.PUMPKIN_SEEDS));
		this.add(Blocks.ATTACHED_PUMPKIN_STEM, blockx -> createAttachedStemDrops(blockx, Items.PUMPKIN_SEEDS));
		this.add(
			Blocks.CHORUS_FLOWER,
			blockx -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.setRolls(ConstantIntValue.exactly(1))
							.add(
								((LootPoolSingletonContainer.Builder)applyExplosionCondition(blockx, LootItem.lootTableItem(blockx)))
									.when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
							)
					)
		);
		this.add(Blocks.FERN, BlockLoot::createGrassDrops);
		this.add(Blocks.GRASS, BlockLoot::createGrassDrops);
		this.add(
			Blocks.GLOWSTONE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx,
						LootItem.lootTableItem(Items.GLOWSTONE_DUST)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
							.apply(LimitCount.limitCount(IntLimiter.clamp(1, 4)))
					)
				)
		);
		this.add(
			Blocks.MELON,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx,
						LootItem.lootTableItem(Items.MELON_SLICE)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(3.0F, 7.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
							.apply(LimitCount.limitCount(IntLimiter.upperBound(9)))
					)
				)
		);
		this.add(
			Blocks.REDSTONE_ORE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx,
						LootItem.lootTableItem(Items.REDSTONE)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(4.0F, 5.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
					)
				)
		);
		this.add(
			Blocks.SEA_LANTERN,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionDecay(
						blockx,
						LootItem.lootTableItem(Items.PRISMARINE_CRYSTALS)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 3.0F)))
							.apply(ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE))
							.apply(LimitCount.limitCount(IntLimiter.clamp(1, 5)))
					)
				)
		);
		this.add(
			Blocks.NETHER_WART,
			blockx -> LootTable.lootTable()
					.withPool(
						applyExplosionDecay(
							blockx,
							LootPool.lootPool()
								.setRolls(ConstantIntValue.exactly(1))
								.add(
									LootItem.lootTableItem(Items.NETHER_WART)
										.apply(
											SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 4.0F))
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))
												)
										)
										.apply(
											ApplyBonusCount.addUniformBonusCount(Enchantments.BLOCK_FORTUNE)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(NetherWartBlock.AGE, 3))
												)
										)
								)
						)
					)
		);
		this.add(
			Blocks.SNOW,
			blockx -> LootTable.lootTable()
					.withPool(
						LootPool.lootPool()
							.when(LootItemEntityPropertyCondition.entityPresent(LootContext.EntityTarget.THIS))
							.add(
								AlternativesEntry.alternatives(
									AlternativesEntry.alternatives(
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 1))
												),
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2))
												)
												.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2))),
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3))
												)
												.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3))),
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4))
												)
												.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4))),
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5))
												)
												.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(5))),
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6))
												)
												.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(6))),
											LootItem.lootTableItem(Items.SNOWBALL)
												.when(
													LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
														.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7))
												)
												.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(7))),
											LootItem.lootTableItem(Items.SNOWBALL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(8)))
										)
										.when(HAS_NO_SILK_TOUCH),
									AlternativesEntry.alternatives(
										LootItem.lootTableItem(Blocks.SNOW)
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 1))
											),
										LootItem.lootTableItem(Blocks.SNOW)
											.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 2))
											),
										LootItem.lootTableItem(Blocks.SNOW)
											.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(3)))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 3))
											),
										LootItem.lootTableItem(Blocks.SNOW)
											.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(4)))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 4))
											),
										LootItem.lootTableItem(Blocks.SNOW)
											.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(5)))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 5))
											),
										LootItem.lootTableItem(Blocks.SNOW)
											.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(6)))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 6))
											),
										LootItem.lootTableItem(Blocks.SNOW)
											.apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(7)))
											.when(
												LootItemBlockStatePropertyCondition.hasBlockStateProperties(blockx)
													.setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SnowLayerBlock.LAYERS, 7))
											),
										LootItem.lootTableItem(Blocks.SNOW_BLOCK)
									)
								)
							)
					)
		);
		this.add(
			Blocks.GRAVEL,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					applyExplosionCondition(
						blockx,
						LootItem.lootTableItem(Items.FLINT)
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1F, 0.14285715F, 0.25F, 1.0F))
							.otherwise(LootItem.lootTableItem(blockx))
					)
				)
		);
		this.add(
			Blocks.CAMPFIRE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionCondition(
						blockx, LootItem.lootTableItem(Items.CHARCOAL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(2)))
					)
				)
		);
		this.add(
			Blocks.GILDED_BLACKSTONE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					applyExplosionCondition(
						blockx,
						LootItem.lootTableItem(Items.GOLD_NUGGET)
							.apply(SetItemCountFunction.setCount(RandomValueBounds.between(2.0F, 5.0F)))
							.when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.1F, 0.14285715F, 0.25F, 1.0F))
							.otherwise(LootItem.lootTableItem(blockx))
					)
				)
		);
		this.add(
			Blocks.SOUL_CAMPFIRE,
			blockx -> createSilkTouchDispatchTable(
					blockx,
					(LootPoolEntryContainer.Builder<?>)applyExplosionCondition(
						blockx, LootItem.lootTableItem(Items.SOUL_SOIL).apply(SetItemCountFunction.setCount(ConstantIntValue.exactly(1)))
					)
				)
		);
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
		this.add(Blocks.CAKE, noDrop());
		this.add(Blocks.FROSTED_ICE, noDrop());
		this.add(Blocks.SPAWNER, noDrop());
		this.add(Blocks.FIRE, noDrop());
		this.add(Blocks.SOUL_FIRE, noDrop());
		this.add(Blocks.NETHER_PORTAL, noDrop());
		Set<ResourceLocation> set = Sets.<ResourceLocation>newHashSet();

		for (Block block : Registry.BLOCK) {
			ResourceLocation resourceLocation = block.getLootTable();
			if (resourceLocation != BuiltInLootTables.EMPTY && set.add(resourceLocation)) {
				LootTable.Builder builder5 = (LootTable.Builder)this.map.remove(resourceLocation);
				if (builder5 == null) {
					throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", resourceLocation, Registry.BLOCK.getKey(block)));
				}

				biConsumer.accept(resourceLocation, builder5);
			}
		}

		if (!this.map.isEmpty()) {
			throw new IllegalStateException("Created block loot tables for non-blocks: " + this.map.keySet());
		}
	}

	private void addNetherVinesDropTable(Block block, Block block2) {
		LootTable.Builder builder = createSilkTouchOrShearsDispatchTable(
			block, LootItem.lootTableItem(block).when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.33F, 0.55F, 0.77F, 1.0F))
		);
		this.add(block, builder);
		this.add(block2, builder);
	}

	public static LootTable.Builder createDoorTable(Block block) {
		return createSinglePropConditionTable(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
	}

	public void dropPottedContents(Block block) {
		this.add(block, blockx -> createPotFlowerItemTable(((FlowerPotBlock)blockx).getContent()));
	}

	public void otherWhenSilkTouch(Block block, Block block2) {
		this.add(block, createSilkTouchOnlyTable(block2));
	}

	public void dropOther(Block block, ItemLike itemLike) {
		this.add(block, createSingleItemTable(itemLike));
	}

	public void dropWhenSilkTouch(Block block) {
		this.otherWhenSilkTouch(block, block);
	}

	public void dropSelf(Block block) {
		this.dropOther(block, block);
	}

	private void add(Block block, Function<Block, LootTable.Builder> function) {
		this.add(block, (LootTable.Builder)function.apply(block));
	}

	private void add(Block block, LootTable.Builder builder) {
		this.map.put(block.getLootTable(), builder);
	}
}
