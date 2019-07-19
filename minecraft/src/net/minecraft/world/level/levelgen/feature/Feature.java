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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public abstract class Feature<FC extends FeatureConfiguration> {
	public static final StructureFeature<PillagerOutpostConfiguration> PILLAGER_OUTPOST = register(
		"pillager_outpost", new PillagerOutpostFeature(PillagerOutpostConfiguration::deserialize)
	);
	public static final StructureFeature<MineshaftConfiguration> MINESHAFT = register("mineshaft", new MineshaftFeature(MineshaftConfiguration::deserialize));
	public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = register(
		"woodland_mansion", new WoodlandMansionFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = register(
		"jungle_temple", new JunglePyramidFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = register(
		"desert_pyramid", new DesertPyramidFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final StructureFeature<NoneFeatureConfiguration> IGLOO = register("igloo", new IglooFeature(NoneFeatureConfiguration::deserialize));
	public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = register("shipwreck", new ShipwreckFeature(ShipwreckConfiguration::deserialize));
	public static final SwamplandHutFeature SWAMP_HUT = register("swamp_hut", new SwamplandHutFeature(NoneFeatureConfiguration::deserialize));
	public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = register(
		"stronghold", new StrongholdFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = register(
		"ocean_monument", new OceanMonumentFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = register("ocean_ruin", new OceanRuinFeature(OceanRuinConfiguration::deserialize));
	public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = register(
		"nether_bridge", new NetherFortressFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final StructureFeature<NoneFeatureConfiguration> END_CITY = register("end_city", new EndCityFeature(NoneFeatureConfiguration::deserialize));
	public static final StructureFeature<BuriedTreasureConfiguration> BURIED_TREASURE = register(
		"buried_treasure", new BuriedTreasureFeature(BuriedTreasureConfiguration::deserialize)
	);
	public static final StructureFeature<VillageConfiguration> VILLAGE = register("village", new VillageFeature(VillageConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> FANCY_TREE = register("fancy_tree", new BigTreeFeature(NoneFeatureConfiguration::deserialize, false));
	public static final Feature<NoneFeatureConfiguration> BIRCH_TREE = register(
		"birch_tree", new BirchFeature(NoneFeatureConfiguration::deserialize, false, false)
	);
	public static final Feature<NoneFeatureConfiguration> SUPER_BIRCH_TREE = register(
		"super_birch_tree", new BirchFeature(NoneFeatureConfiguration::deserialize, false, true)
	);
	public static final Feature<NoneFeatureConfiguration> JUNGLE_GROUND_BUSH = register(
		"jungle_ground_bush",
		new GroundBushFeature(NoneFeatureConfiguration::deserialize, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState())
	);
	public static final Feature<NoneFeatureConfiguration> JUNGLE_TREE = register(
		"jungle_tree",
		new JungleTreeFeature(NoneFeatureConfiguration::deserialize, false, 4, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState(), true)
	);
	public static final Feature<NoneFeatureConfiguration> PINE_TREE = register("pine_tree", new PineFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> DARK_OAK_TREE = register(
		"dark_oak_tree", new DarkOakFeature(NoneFeatureConfiguration::deserialize, false)
	);
	public static final Feature<NoneFeatureConfiguration> SAVANNA_TREE = register(
		"savanna_tree", new SavannaTreeFeature(NoneFeatureConfiguration::deserialize, false)
	);
	public static final Feature<NoneFeatureConfiguration> SPRUCE_TREE = register("spruce_tree", new SpruceFeature(NoneFeatureConfiguration::deserialize, false));
	public static final Feature<NoneFeatureConfiguration> SWAMP_TREE = register("swamp_tree", new SwampTreeFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> NORMAL_TREE = register("normal_tree", new TreeFeature(NoneFeatureConfiguration::deserialize, false));
	public static final Feature<NoneFeatureConfiguration> MEGA_JUNGLE_TREE = register(
		"mega_jungle_tree",
		new MegaJungleTreeFeature(
			NoneFeatureConfiguration::deserialize, false, 10, 20, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState()
		)
	);
	public static final Feature<NoneFeatureConfiguration> MEGA_PINE_TREE = register(
		"mega_pine_tree", new MegaPineTreeFeature(NoneFeatureConfiguration::deserialize, false, false)
	);
	public static final Feature<NoneFeatureConfiguration> MEGA_SPRUCE_TREE = register(
		"mega_spruce_tree", new MegaPineTreeFeature(NoneFeatureConfiguration::deserialize, false, true)
	);
	public static final FlowerFeature DEFAULT_FLOWER = register("default_flower", new DefaultFlowerFeature(NoneFeatureConfiguration::deserialize));
	public static final FlowerFeature FOREST_FLOWER = register("forest_flower", new ForestFlowerFeature(NoneFeatureConfiguration::deserialize));
	public static final FlowerFeature PLAIN_FLOWER = register("plain_flower", new PlainFlowerFeature(NoneFeatureConfiguration::deserialize));
	public static final FlowerFeature SWAMP_FLOWER = register("swamp_flower", new SwampFlowerFeature(NoneFeatureConfiguration::deserialize));
	public static final FlowerFeature GENERAL_FOREST_FLOWER = register(
		"general_forest_flower", new GeneralForestFlowerFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final Feature<NoneFeatureConfiguration> JUNGLE_GRASS = register("jungle_grass", new JungleGrassFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> TAIGA_GRASS = register("taiga_grass", new TaigaGrassFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<GrassConfiguration> GRASS = register("grass", new GrassFeature(GrassConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = register(
		"void_start_platform", new VoidStartPlatformFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final Feature<NoneFeatureConfiguration> CACTUS = register("cactus", new CactusFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> DEAD_BUSH = register("dead_bush", new DeadBushFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> DESERT_WELL = register("desert_well", new DesertWellFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> FOSSIL = register("fossil", new FossilFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> HELL_FIRE = register("hell_fire", new HellFireFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<HugeMushroomFeatureConfig> HUGE_RED_MUSHROOM = register(
		"huge_red_mushroom", new HugeRedMushroomFeature(HugeMushroomFeatureConfig::deserialize)
	);
	public static final Feature<HugeMushroomFeatureConfig> HUGE_BROWN_MUSHROOM = register(
		"huge_brown_mushroom", new HugeBrownMushroomFeature(HugeMushroomFeatureConfig::deserialize)
	);
	public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = register("ice_spike", new IceSpikeFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = register("glowstone_blob", new GlowstoneFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> MELON = register("melon", new MelonFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> PUMPKIN = register(
		"pumpkin", new CentralSpikedFeature(NoneFeatureConfiguration::deserialize, Blocks.PUMPKIN.defaultBlockState())
	);
	public static final Feature<NoneFeatureConfiguration> REED = register("reed", new ReedsFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = register(
		"freeze_top_layer", new SnowAndFreezeFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final Feature<NoneFeatureConfiguration> VINES = register("vines", new VinesFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> WATERLILY = register("waterlily", new WaterlilyFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = register("monster_room", new MonsterRoomFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> BLUE_ICE = register("blue_ice", new BlueIceFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<IcebergConfiguration> ICEBERG = register("iceberg", new IcebergFeature(IcebergConfiguration::deserialize));
	public static final Feature<BlockBlobConfiguration> FOREST_ROCK = register("forest_rock", new BlockBlobFeature(BlockBlobConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> HAY_PILE = register("hay_pile", new HayBlockPileFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> SNOW_PILE = register("snow_pile", new SnowBlockPileFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> ICE_PILE = register("ice_pile", new IceBlockPileFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> MELON_PILE = register("melon_pile", new MelonBlockPileFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> PUMPKIN_PILE = register(
		"pumpkin_pile", new PumpkinBlockPileFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final Feature<BushConfiguration> BUSH = register("bush", new BushFeature(BushConfiguration::deserialize));
	public static final Feature<DiskConfiguration> DISK = register("disk", new DiskReplaceFeature(DiskConfiguration::deserialize));
	public static final Feature<DoublePlantConfiguration> DOUBLE_PLANT = register("double_plant", new DoublePlantFeature(DoublePlantConfiguration::deserialize));
	public static final Feature<HellSpringConfiguration> NETHER_SPRING = register("nether_spring", new NetherSpringFeature(HellSpringConfiguration::deserialize));
	public static final Feature<FeatureRadius> ICE_PATCH = register("ice_patch", new IcePatchFeature(FeatureRadius::deserialize));
	public static final Feature<LakeConfiguration> LAKE = register("lake", new LakeFeature(LakeConfiguration::deserialize));
	public static final Feature<OreConfiguration> ORE = register("ore", new OreFeature(OreConfiguration::deserialize));
	public static final Feature<RandomRandomFeatureConfig> RANDOM_RANDOM_SELECTOR = register(
		"random_random_selector", new RandomRandomFeature(RandomRandomFeatureConfig::deserialize)
	);
	public static final Feature<RandomFeatureConfig> RANDOM_SELECTOR = register("random_selector", new RandomSelectorFeature(RandomFeatureConfig::deserialize));
	public static final Feature<SimpleRandomFeatureConfig> SIMPLE_RANDOM_SELECTOR = register(
		"simple_random_selector", new SimpleRandomSelectorFeature(SimpleRandomFeatureConfig::deserialize)
	);
	public static final Feature<RandomBooleanFeatureConfig> RANDOM_BOOLEAN_SELECTOR = register(
		"random_boolean_selector", new RandomBooleanSelectorFeature(RandomBooleanFeatureConfig::deserialize)
	);
	public static final Feature<ReplaceBlockConfiguration> EMERALD_ORE = register("emerald_ore", new ReplaceBlockFeature(ReplaceBlockConfiguration::deserialize));
	public static final Feature<SpringConfiguration> SPRING = register("spring_feature", new SpringFeature(SpringConfiguration::deserialize));
	public static final Feature<SpikeConfiguration> END_SPIKE = register("end_spike", new SpikeFeature(SpikeConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> END_ISLAND = register("end_island", new EndIslandFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = register("chorus_plant", new ChorusPlantFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<EndGatewayConfiguration> END_GATEWAY = register("end_gateway", new EndGatewayFeature(EndGatewayConfiguration::deserialize));
	public static final Feature<SeagrassFeatureConfiguration> SEAGRASS = register("seagrass", new SeagrassFeature(SeagrassFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> KELP = register("kelp", new KelpFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> CORAL_TREE = register("coral_tree", new CoralTreeFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = register(
		"coral_mushroom", new CoralMushroomFeature(NoneFeatureConfiguration::deserialize)
	);
	public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = register("coral_claw", new CoralClawFeature(NoneFeatureConfiguration::deserialize));
	public static final Feature<CountFeatureConfiguration> SEA_PICKLE = register("sea_pickle", new SeaPickleFeature(CountFeatureConfiguration::deserialize));
	public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = register("simple_block", new SimpleBlockFeature(SimpleBlockConfiguration::deserialize));
	public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = register("bamboo", new BambooFeature(ProbabilityFeatureConfiguration::deserialize));
	public static final Feature<DecoratedFeatureConfiguration> DECORATED = register("decorated", new DecoratedFeature(DecoratedFeatureConfiguration::deserialize));
	public static final Feature<DecoratedFeatureConfiguration> DECORATED_FLOWER = register(
		"decorated_flower", new DecoratedFlowerFeature(DecoratedFeatureConfiguration::deserialize)
	);
	public static final Feature<NoneFeatureConfiguration> SWEET_BERRY_BUSH = register(
		"sweet_berry_bush",
		new CentralSpikedFeature(
			NoneFeatureConfiguration::deserialize, Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, Integer.valueOf(3))
		)
	);
	public static final Feature<LayerConfiguration> FILL_LAYER = register("fill_layer", new FillLayerFeature(LayerConfiguration::deserialize));
	public static final BonusChestFeature BONUS_CHEST = register("bonus_chest", new BonusChestFeature(NoneFeatureConfiguration::deserialize));
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
	});
	public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE);
	private final Function<Dynamic<?>, ? extends FC> configurationFactory;
	protected final boolean doUpdate;

	private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String string, F feature) {
		return Registry.register(Registry.FEATURE, string, feature);
	}

	public Feature(Function<Dynamic<?>, ? extends FC> function) {
		this.configurationFactory = function;
		this.doUpdate = false;
	}

	public Feature(Function<Dynamic<?>, ? extends FC> function, boolean bl) {
		this.configurationFactory = function;
		this.doUpdate = bl;
	}

	public FC createSettings(Dynamic<?> dynamic) {
		return (FC)this.configurationFactory.apply(dynamic);
	}

	protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
		if (this.doUpdate) {
			levelWriter.setBlock(blockPos, blockState, 3);
		} else {
			levelWriter.setBlock(blockPos, blockState, 2);
		}
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
}
