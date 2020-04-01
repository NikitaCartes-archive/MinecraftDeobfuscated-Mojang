package net.minecraft.world.level.levelgen.feature;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Dynamic;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.special.G03;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.configurations.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CharConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MegaTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShapeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SmallTreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.VillageConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public abstract class Feature<FC extends FeatureConfiguration> {
	public static final StructureFeature<NoneFeatureConfiguration> PILLAGER_OUTPOST = register(
		"pillager_outpost", new PillagerOutpostFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register(
		"mineshaft", new MineshaftFeature(MineshaftConfiguration::deserialize, MineshaftConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register(
		"woodland_mansion", new WoodlandMansionFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register(
		"jungle_temple", new JunglePyramidFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register(
		"desert_pyramid", new DesertPyramidFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register(
		"igloo", new IglooFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register(
		"shipwreck", new ShipwreckFeature(ShipwreckConfiguration::deserialize, ShipwreckConfiguration::random)
	);
	public static final SwamplandHutFeature SWAMP_HUT = register(
		"swamp_hut", new SwamplandHutFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register(
		"stronghold", new StrongholdFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register(
		"ocean_monument", new OceanMonumentFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register(
		"ocean_ruin", new OceanRuinFeature(OceanRuinConfiguration::deserialize, OceanRuinConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register(
		"nether_bridge", new NetherFortressFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register(
		"end_city", new EndCityFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<BuriedTreasureConfiguration> BURIED_TREASURE = register(
		"buried_treasure", new BuriedTreasureFeature(BuriedTreasureConfiguration::deserialize, BuriedTreasureConfiguration::random)
	);
	public static final StructureFeature<VillageConfiguration> VILLAGE = register(
		"village", new VillageFeature(VillageConfiguration::deserialize, VillageConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> NETHER_FOSSIL = register(
		"nether_fossil", new NetherFossilFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final StructureFeature<NoneFeatureConfiguration> SHIP = register(
		"ship", new G03.ShipFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> NO_OP = register(
		"no_op", new NoOpFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<SmallTreeConfiguration> NORMAL_TREE = register(
		"normal_tree", new TreeFeature(SmallTreeConfiguration::deserialize, SmallTreeConfiguration::random)
	);
	public static final Feature<SmallTreeConfiguration> ACACIA_TREE = register(
		"acacia_tree", new AcaciaFeature(SmallTreeConfiguration::deserialize, SmallTreeConfiguration::random)
	);
	public static final Feature<SmallTreeConfiguration> FANCY_TREE = register(
		"fancy_tree", new FancyTreeFeature(SmallTreeConfiguration::deserialize, SmallTreeConfiguration::fancyRandom)
	);
	public static final Feature<TreeConfiguration> JUNGLE_GROUND_BUSH = register(
		"jungle_ground_bush", new GroundBushFeature(TreeConfiguration::deserialize, TreeConfiguration::random)
	);
	public static final Feature<MegaTreeConfiguration> DARK_OAK_TREE = register(
		"dark_oak_tree", new DarkOakFeature(MegaTreeConfiguration::deserialize, MegaTreeConfiguration::random)
	);
	public static final Feature<MegaTreeConfiguration> MEGA_JUNGLE_TREE = register(
		"mega_jungle_tree", new MegaJungleTreeFeature(MegaTreeConfiguration::deserialize, MegaTreeConfiguration::random)
	);
	public static final Feature<MegaTreeConfiguration> MEGA_SPRUCE_TREE = register(
		"mega_spruce_tree", new MegaPineTreeFeature(MegaTreeConfiguration::deserialize, MegaTreeConfiguration::random)
	);
	public static final AbstractFlowerFeature<RandomPatchConfiguration> FLOWER = register(
		"flower", new DefaultFlowerFeature(RandomPatchConfiguration::deserialize, RandomPatchConfiguration::random)
	);
	public static final Feature<RandomPatchConfiguration> RANDOM_PATCH = register(
		"random_patch", new RandomPatchFeature(RandomPatchConfiguration::deserialize, RandomPatchConfiguration::random)
	);
	public static final Feature<BlockPileConfiguration> BLOCK_PILE = register(
		"block_pile", new BlockPileFeature(BlockPileConfiguration::deserialize, BlockPileConfiguration::random)
	);
	public static final Feature<SpringConfiguration> SPRING = register(
		"spring_feature", new SpringFeature(SpringConfiguration::deserialize, SpringConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = register(
		"chorus_plant", new ChorusPlantFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<ReplaceBlockConfiguration> EMERALD_ORE = register(
		"emerald_ore", new ReplaceBlockFeature(ReplaceBlockConfiguration::deserialize, ReplaceBlockConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = register(
		"void_start_platform", new VoidStartPlatformFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> DESERT_WELL = register(
		"desert_well", new DesertWellFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> FOSSIL = register(
		"fossil", new FossilFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<HugeMushroomFeatureConfiguration> HUGE_RED_MUSHROOM = register(
		"huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfiguration::deserialize, HugeMushroomFeatureConfiguration::random)
	);
	public static final Feature<HugeMushroomFeatureConfiguration> HUGE_BROWN_MUSHROOM = register(
		"huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfiguration::deserialize, HugeMushroomFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = register(
		"ice_spike", new IceSpikeFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = register(
		"glowstone_blob", new GlowstoneFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = register(
		"freeze_top_layer", new SnowAndFreezeFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> VINES = register(
		"vines", new VinesFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = register(
		"monster_room", new MonsterRoomFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> BLUE_ICE = register(
		"blue_ice", new BlueIceFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<BlockStateConfiguration> ICEBERG = register(
		"iceberg", new IcebergFeature(BlockStateConfiguration::deserialize, BlockStateConfiguration::safeRandom)
	);
	public static final Feature<BlockBlobConfiguration> FOREST_ROCK = register(
		"forest_rock", new BlockBlobFeature(BlockBlobConfiguration::deserialize, BlockBlobConfiguration::random)
	);
	public static final Feature<DiskConfiguration> DISK = register("disk", new DiskReplaceFeature(DiskConfiguration::deserialize, DiskConfiguration::random));
	public static final Feature<FeatureRadiusConfiguration> ICE_PATCH = register(
		"ice_patch", new IcePatchFeature(FeatureRadiusConfiguration::deserialize, FeatureRadiusConfiguration::random)
	);
	public static final Feature<BlockStateConfiguration> LAKE = register(
		"lake", new LakeFeature(BlockStateConfiguration::deserialize, BlockStateConfiguration::safeRandom)
	);
	public static final Feature<OreConfiguration> ORE = register("ore", new OreFeature(OreConfiguration::deserialize, OreConfiguration::random));
	public static final Feature<SpikeConfiguration> END_SPIKE = register(
		"end_spike", new SpikeFeature(SpikeConfiguration::deserialize, SpikeConfiguration::random)
	);
	public static final Feature<BlockStateConfiguration> END_ISLAND = register(
		"end_island", new EndIslandFeature(BlockStateConfiguration::deserialize, BlockStateConfiguration::safeRandom)
	);
	public static final Feature<EndGatewayConfiguration> END_GATEWAY = register(
		"end_gateway", new EndGatewayFeature(EndGatewayConfiguration::deserialize, EndGatewayConfiguration::random)
	);
	public static final Feature<SeagrassFeatureConfiguration> SEAGRASS = register(
		"seagrass", new SeagrassFeature(SeagrassFeatureConfiguration::deserialize, SeagrassFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> KELP = register(
		"kelp", new KelpFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> CORAL_TREE = register(
		"coral_tree", new CoralTreeFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = register(
		"coral_mushroom", new CoralMushroomFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = register(
		"coral_claw", new CoralClawFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<CountFeatureConfiguration> SEA_PICKLE = register(
		"sea_pickle", new SeaPickleFeature(CountFeatureConfiguration::deserialize, CountFeatureConfiguration::random)
	);
	public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = register(
		"simple_block", new SimpleBlockFeature(SimpleBlockConfiguration::deserialize, SimpleBlockConfiguration::random)
	);
	public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = register(
		"bamboo", new BambooFeature(ProbabilityFeatureConfiguration::deserialize, ProbabilityFeatureConfiguration::random)
	);
	public static final Feature<HugeFungusConfiguration> HUGE_FUNGUS = register(
		"huge_fungus", new HugeFungusFeature(HugeFungusConfiguration::deserialize, HugeFungusConfiguration::random)
	);
	public static final Feature<BlockPileConfiguration> NETHER_FOREST_VEGETATION = register(
		"nether_forest_vegetation", new NetherForestVegetationFeature(BlockPileConfiguration::deserialize, BlockPileConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> WEEPING_VINES = register(
		"weeping_vines", new WeepingVinesFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> TWISTING_VINES = register(
		"twisting_vines", new TwistingVinesFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<LayerConfiguration> FILL_LAYER = register(
		"fill_layer", new FillLayerFeature(LayerConfiguration::deserialize, LayerConfiguration::random)
	);
	public static final BonusChestFeature BONUS_CHEST = register(
		"bonus_chest", new BonusChestFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<NoneFeatureConfiguration> BASALT_PILLAR = register(
		"basalt_pillar", new BasaltPillarFeature(NoneFeatureConfiguration::deserialize, NoneFeatureConfiguration::random)
	);
	public static final Feature<OreConfiguration> NO_SURFACE_ORE = register(
		"no_surface_ore", new NoSurfaceOreFeature(OreConfiguration::deserialize, OreConfiguration::random)
	);
	public static final Feature<RandomRandomFeatureConfiguration> RANDOM_RANDOM_SELECTOR = register(
		"random_random_selector", new RandomRandomFeature(RandomRandomFeatureConfiguration::deserialize, RandomRandomFeatureConfiguration::random)
	);
	public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = register(
		"random_selector", new RandomSelectorFeature(RandomFeatureConfiguration::deserialize, RandomFeatureConfiguration::random)
	);
	public static final Feature<SimpleRandomFeatureConfiguration> SIMPLE_RANDOM_SELECTOR = register(
		"simple_random_selector", new SimpleRandomSelectorFeature(SimpleRandomFeatureConfiguration::deserialize, SimpleRandomFeatureConfiguration::random)
	);
	public static final Feature<RandomBooleanFeatureConfiguration> RANDOM_BOOLEAN_SELECTOR = register(
		"random_boolean_selector", new RandomBooleanSelectorFeature(RandomBooleanFeatureConfiguration::deserialize, RandomBooleanFeatureConfiguration::random)
	);
	public static final Feature<DecoratedFeatureConfiguration> DECORATED = register(
		"decorated", new DecoratedFeature(DecoratedFeatureConfiguration::deserialize, DecoratedFeatureConfiguration::random)
	);
	public static final Feature<DecoratedFeatureConfiguration> DECORATED_FLOWER = register(
		"decorated_flower", new DecoratedFlowerFeature(DecoratedFeatureConfiguration::deserialize, DecoratedFeatureConfiguration::random)
	);
	public static final Feature<ShapeConfiguration> SHAPE = register("shape", new ShapeFeature(ShapeConfiguration::deserialize, ShapeConfiguration::random));
	public static final Feature<CharConfiguration> CHAR = register("char", new CharFeature(CharConfiguration::deserialize, CharConfiguration::random));
	public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = Util.make(HashBiMap.create(), hashBiMap -> {
		hashBiMap.put("Pillager_Outpost".toLowerCase(Locale.ROOT), PILLAGER_OUTPOST);
		hashBiMap.put("Mineshaft".toLowerCase(Locale.ROOT), MINESHAFT);
		hashBiMap.put("Mansion".toLowerCase(Locale.ROOT), WOODLAND_MANSION);
		hashBiMap.put("Jungle_Pyramid".toLowerCase(Locale.ROOT), JUNGLE_TEMPLE);
		hashBiMap.put("Desert_Pyramid".toLowerCase(Locale.ROOT), DESERT_PYRAMID);
		hashBiMap.put("Igloo".toLowerCase(Locale.ROOT), IGLOO);
		hashBiMap.put("Shipwreck".toLowerCase(Locale.ROOT), SHIPWRECK);
		hashBiMap.put("Swamp_Hut".toLowerCase(Locale.ROOT), SWAMP_HUT);
		hashBiMap.put("Stronghold".toLowerCase(Locale.ROOT), STRONGHOLD);
		hashBiMap.put("Monument".toLowerCase(Locale.ROOT), OCEAN_MONUMENT);
		hashBiMap.put("Ocean_Ruin".toLowerCase(Locale.ROOT), OCEAN_RUIN);
		hashBiMap.put("Fortress".toLowerCase(Locale.ROOT), NETHER_BRIDGE);
		hashBiMap.put("EndCity".toLowerCase(Locale.ROOT), END_CITY);
		hashBiMap.put("Buried_Treasure".toLowerCase(Locale.ROOT), BURIED_TREASURE);
		hashBiMap.put("Village".toLowerCase(Locale.ROOT), VILLAGE);
		hashBiMap.put("Nether_Fossil".toLowerCase(Locale.ROOT), NETHER_FOSSIL);
		hashBiMap.put("Ship".toLowerCase(Locale.ROOT), SHIP);
	});
	public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL);
	private final Function<Dynamic<?>, ? extends FC> configurationFactory;
	protected final Function<Random, ? extends FC> randomConfigurationFactory;

	private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String string, F feature) {
		return Registry.register(Registry.FEATURE, string, feature);
	}

	public Feature(Function<Dynamic<?>, ? extends FC> function, Function<Random, ? extends FC> function2) {
		this.configurationFactory = function;
		this.randomConfigurationFactory = function2;
	}

	public ConfiguredFeature<FC, ?> configured(FC featureConfiguration) {
		return new ConfiguredFeature<>(this, featureConfiguration);
	}

	public ConfiguredFeature<FC, ?> random(Random random) {
		return new ConfiguredFeature<>(this, (FC)this.randomConfigurationFactory.apply(random));
	}

	public FC createSettings(Dynamic<?> dynamic) {
		return (FC)this.configurationFactory.apply(dynamic);
	}

	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		levelWriter.setBlock(blockPos, blockState, 3);
	}

	public abstract boolean place(
		LevelAccessor levelAccessor, ChunkGenerator<? extends ChunkGeneratorSettings> chunkGenerator, Random random, BlockPos blockPos, FC featureConfiguration
	);

	public List<Biome.SpawnerData> getSpecialEnemies() {
		return Collections.emptyList();
	}

	public List<Biome.SpawnerData> getSpecialAnimals() {
		return Collections.emptyList();
	}

	protected static boolean isStone(Block block) {
		return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE;
	}

	protected static boolean isDirt(Block block) {
		return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.COARSE_DIRT || block == Blocks.MYCELIUM;
	}
}
