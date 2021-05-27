package net.minecraft.world.level.levelgen.feature;

import com.mojang.serialization.Codec;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DripstoneClusterConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GlowLichenConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.GrowingPlantConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SmallDripstoneConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.UnderwaterMagmaConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VegetationPatchConfiguration;

public abstract class Feature<FC extends FeatureConfiguration> {
	public static final Feature<NoneFeatureConfiguration> NO_OP = register("no_op", new NoOpFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<TreeConfiguration> TREE = register("tree", new TreeFeature(TreeConfiguration.CODEC));
	public static final AbstractFlowerFeature<RandomPatchConfiguration> FLOWER = register("flower", new DefaultFlowerFeature(RandomPatchConfiguration.CODEC));
	public static final AbstractFlowerFeature<RandomPatchConfiguration> NO_BONEMEAL_FLOWER = register(
		"no_bonemeal_flower", new DefaultFlowerFeature(RandomPatchConfiguration.CODEC)
	);
	public static final Feature<RandomPatchConfiguration> RANDOM_PATCH = register("random_patch", new RandomPatchFeature(RandomPatchConfiguration.CODEC));
	public static final Feature<BlockPileConfiguration> BLOCK_PILE = register("block_pile", new BlockPileFeature(BlockPileConfiguration.CODEC));
	public static final Feature<SpringConfiguration> SPRING = register("spring_feature", new SpringFeature(SpringConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = register("chorus_plant", new ChorusPlantFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<ReplaceBlockConfiguration> REPLACE_SINGLE_BLOCK = register(
		"replace_single_block", new ReplaceBlockFeature(ReplaceBlockConfiguration.CODEC)
	);
	public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = register(
		"void_start_platform", new VoidStartPlatformFeature(NoneFeatureConfiguration.CODEC)
	);
	public static final Feature<NoneFeatureConfiguration> DESERT_WELL = register("desert_well", new DesertWellFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<FossilFeatureConfiguration> FOSSIL = register("fossil", new FossilFeature(FossilFeatureConfiguration.CODEC));
	public static final Feature<HugeMushroomFeatureConfiguration> HUGE_RED_MUSHROOM = register(
		"huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfiguration.CODEC)
	);
	public static final Feature<HugeMushroomFeatureConfiguration> HUGE_BROWN_MUSHROOM = register(
		"huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfiguration.CODEC)
	);
	public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = register("ice_spike", new IceSpikeFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = register("glowstone_blob", new GlowstoneFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = register("freeze_top_layer", new SnowAndFreezeFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> VINES = register("vines", new VinesFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<GrowingPlantConfiguration> GROWING_PLANT = register("growing_plant", new GrowingPlantFeature(GrowingPlantConfiguration.CODEC));
	public static final Feature<VegetationPatchConfiguration> VEGETATION_PATCH = register(
		"vegetation_patch", new VegetationPatchFeature(VegetationPatchConfiguration.CODEC)
	);
	public static final Feature<VegetationPatchConfiguration> WATERLOGGED_VEGETATION_PATCH = register(
		"waterlogged_vegetation_patch", new WaterloggedVegetationPatchFeature(VegetationPatchConfiguration.CODEC)
	);
	public static final Feature<RootSystemConfiguration> ROOT_SYSTEM = register("root_system", new RootSystemFeature(RootSystemConfiguration.CODEC));
	public static final Feature<GlowLichenConfiguration> GLOW_LICHEN = register("glow_lichen", new GlowLichenFeature(GlowLichenConfiguration.CODEC));
	public static final Feature<UnderwaterMagmaConfiguration> UNDERWATER_MAGMA = register(
		"underwater_magma", new UnderwaterMagmaFeature(UnderwaterMagmaConfiguration.CODEC)
	);
	public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = register("monster_room", new MonsterRoomFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> BLUE_ICE = register("blue_ice", new BlueIceFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<BlockStateConfiguration> ICEBERG = register("iceberg", new IcebergFeature(BlockStateConfiguration.CODEC));
	public static final Feature<BlockStateConfiguration> FOREST_ROCK = register("forest_rock", new BlockBlobFeature(BlockStateConfiguration.CODEC));
	public static final Feature<DiskConfiguration> DISK = register("disk", new DiskReplaceFeature(DiskConfiguration.CODEC));
	public static final Feature<DiskConfiguration> ICE_PATCH = register("ice_patch", new IcePatchFeature(DiskConfiguration.CODEC));
	public static final Feature<BlockStateConfiguration> LAKE = register("lake", new LakeFeature(BlockStateConfiguration.CODEC));
	public static final Feature<OreConfiguration> ORE = register("ore", new OreFeature(OreConfiguration.CODEC));
	public static final Feature<SpikeConfiguration> END_SPIKE = register("end_spike", new SpikeFeature(SpikeConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> END_ISLAND = register("end_island", new EndIslandFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<EndGatewayConfiguration> END_GATEWAY = register("end_gateway", new EndGatewayFeature(EndGatewayConfiguration.CODEC));
	public static final SeagrassFeature SEAGRASS = register("seagrass", new SeagrassFeature(ProbabilityFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> KELP = register("kelp", new KelpFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> CORAL_TREE = register("coral_tree", new CoralTreeFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = register("coral_mushroom", new CoralMushroomFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = register("coral_claw", new CoralClawFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<CountConfiguration> SEA_PICKLE = register("sea_pickle", new SeaPickleFeature(CountConfiguration.CODEC));
	public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = register("simple_block", new SimpleBlockFeature(SimpleBlockConfiguration.CODEC));
	public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = register("bamboo", new BambooFeature(ProbabilityFeatureConfiguration.CODEC));
	public static final Feature<HugeFungusConfiguration> HUGE_FUNGUS = register("huge_fungus", new HugeFungusFeature(HugeFungusConfiguration.CODEC));
	public static final Feature<BlockPileConfiguration> NETHER_FOREST_VEGETATION = register(
		"nether_forest_vegetation", new NetherForestVegetationFeature(BlockPileConfiguration.CODEC)
	);
	public static final Feature<NoneFeatureConfiguration> WEEPING_VINES = register("weeping_vines", new WeepingVinesFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> TWISTING_VINES = register("twisting_vines", new TwistingVinesFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<ColumnFeatureConfiguration> BASALT_COLUMNS = register("basalt_columns", new BasaltColumnsFeature(ColumnFeatureConfiguration.CODEC));
	public static final Feature<DeltaFeatureConfiguration> DELTA_FEATURE = register("delta_feature", new DeltaFeature(DeltaFeatureConfiguration.CODEC));
	public static final Feature<ReplaceSphereConfiguration> REPLACE_BLOBS = register(
		"netherrack_replace_blobs", new ReplaceBlobsFeature(ReplaceSphereConfiguration.CODEC)
	);
	public static final Feature<LayerConfiguration> FILL_LAYER = register("fill_layer", new FillLayerFeature(LayerConfiguration.CODEC));
	public static final BonusChestFeature BONUS_CHEST = register("bonus_chest", new BonusChestFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<NoneFeatureConfiguration> BASALT_PILLAR = register("basalt_pillar", new BasaltPillarFeature(NoneFeatureConfiguration.CODEC));
	public static final Feature<OreConfiguration> SCATTERED_ORE = register("scattered_ore", new ScatteredOreFeature(OreConfiguration.CODEC));
	public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = register(
		"random_selector", new RandomSelectorFeature(RandomFeatureConfiguration.CODEC)
	);
	public static final Feature<SimpleRandomFeatureConfiguration> SIMPLE_RANDOM_SELECTOR = register(
		"simple_random_selector", new SimpleRandomSelectorFeature(SimpleRandomFeatureConfiguration.CODEC)
	);
	public static final Feature<RandomBooleanFeatureConfiguration> RANDOM_BOOLEAN_SELECTOR = register(
		"random_boolean_selector", new RandomBooleanSelectorFeature(RandomBooleanFeatureConfiguration.CODEC)
	);
	public static final Feature<DecoratedFeatureConfiguration> DECORATED = register("decorated", new DecoratedFeature(DecoratedFeatureConfiguration.CODEC));
	public static final Feature<GeodeConfiguration> GEODE = register("geode", new GeodeFeature(GeodeConfiguration.CODEC));
	public static final Feature<DripstoneClusterConfiguration> DRIPSTONE_CLUSTER = register(
		"dripstone_cluster", new DripstoneClusterFeature(DripstoneClusterConfiguration.CODEC)
	);
	public static final Feature<LargeDripstoneConfiguration> LARGE_DRIPSTONE = register(
		"large_dripstone", new LargeDripstoneFeature(LargeDripstoneConfiguration.CODEC)
	);
	public static final Feature<SmallDripstoneConfiguration> SMALL_DRIPSTONE = register(
		"small_dripstone", new SmallDripstoneFeature(SmallDripstoneConfiguration.CODEC)
	);
	private final Codec<ConfiguredFeature<FC, Feature<FC>>> configuredCodec;

	private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String string, F feature) {
		return Registry.register(Registry.FEATURE, string, feature);
	}

	public Feature(Codec<FC> codec) {
		this.configuredCodec = codec.fieldOf("config")
			.<ConfiguredFeature<FC, Feature<FC>>>xmap(
				featureConfiguration -> new ConfiguredFeature<>(this, featureConfiguration), configuredFeature -> configuredFeature.config
			)
			.codec();
	}

	public Codec<ConfiguredFeature<FC, Feature<FC>>> configuredCodec() {
		return this.configuredCodec;
	}

	public ConfiguredFeature<FC, ?> configured(FC featureConfiguration) {
		return new ConfiguredFeature<>(this, featureConfiguration);
	}

	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		levelWriter.setBlock(blockPos, blockState, 3);
	}

	public static Predicate<BlockState> isReplaceable(ResourceLocation resourceLocation) {
		Tag<Block> tag = BlockTags.getAllTags().getTag(resourceLocation);
		return tag == null ? blockState -> true : blockState -> !blockState.is(tag);
	}

	protected void safeSetBlock(WorldGenLevel worldGenLevel, BlockPos blockPos, BlockState blockState, Predicate<BlockState> predicate) {
		if (predicate.test(worldGenLevel.getBlockState(blockPos))) {
			worldGenLevel.setBlock(blockPos, blockState, 2);
		}
	}

	public abstract boolean place(FeaturePlaceContext<FC> featurePlaceContext);

	protected static boolean isStone(BlockState blockState) {
		return blockState.is(BlockTags.BASE_STONE_OVERWORLD);
	}

	public static boolean isDirt(BlockState blockState) {
		return blockState.is(BlockTags.DIRT);
	}

	public static boolean isGrassOrDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, Feature::isDirt);
	}

	public static boolean isAir(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
		return levelSimulatedReader.isStateAtPosition(blockPos, BlockBehaviour.BlockStateBase::isAir);
	}

	public static boolean checkNeighbors(Function<BlockPos, BlockState> function, BlockPos blockPos, Predicate<BlockState> predicate) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

		for (Direction direction : Direction.values()) {
			mutableBlockPos.setWithOffset(blockPos, direction);
			if (predicate.test((BlockState)function.apply(mutableBlockPos))) {
				return true;
			}
		}

		return false;
	}

	public static boolean isAdjacentToAir(Function<BlockPos, BlockState> function, BlockPos blockPos) {
		return checkNeighbors(function, blockPos, BlockBehaviour.BlockStateBase::isAir);
	}

	protected void markAboveForPostProcessing(WorldGenLevel worldGenLevel, BlockPos blockPos) {
		BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();

		for (int i = 0; i < 2; i++) {
			mutableBlockPos.move(Direction.UP);
			if (worldGenLevel.getBlockState(mutableBlockPos).isAir()) {
				return;
			}

			worldGenLevel.getChunk(mutableBlockPos).markPosForPostprocessing(mutableBlockPos);
		}
	}
}
