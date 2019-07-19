/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
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
import net.minecraft.world.level.levelgen.feature.BambooFeature;
import net.minecraft.world.level.levelgen.feature.BigTreeFeature;
import net.minecraft.world.level.levelgen.feature.BirchFeature;
import net.minecraft.world.level.levelgen.feature.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.BlockBlobFeature;
import net.minecraft.world.level.levelgen.feature.BlueIceFeature;
import net.minecraft.world.level.levelgen.feature.BonusChestFeature;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureFeature;
import net.minecraft.world.level.levelgen.feature.BushConfiguration;
import net.minecraft.world.level.levelgen.feature.BushFeature;
import net.minecraft.world.level.levelgen.feature.CactusFeature;
import net.minecraft.world.level.levelgen.feature.CentralSpikedFeature;
import net.minecraft.world.level.levelgen.feature.ChorusPlantFeature;
import net.minecraft.world.level.levelgen.feature.CoralClawFeature;
import net.minecraft.world.level.levelgen.feature.CoralMushroomFeature;
import net.minecraft.world.level.levelgen.feature.CoralTreeFeature;
import net.minecraft.world.level.levelgen.feature.CountFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.DarkOakFeature;
import net.minecraft.world.level.levelgen.feature.DeadBushFeature;
import net.minecraft.world.level.levelgen.feature.DecoratedFeature;
import net.minecraft.world.level.levelgen.feature.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.DecoratedFlowerFeature;
import net.minecraft.world.level.levelgen.feature.DefaultFlowerFeature;
import net.minecraft.world.level.levelgen.feature.DesertPyramidFeature;
import net.minecraft.world.level.levelgen.feature.DesertWellFeature;
import net.minecraft.world.level.levelgen.feature.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.DiskReplaceFeature;
import net.minecraft.world.level.levelgen.feature.DoublePlantConfiguration;
import net.minecraft.world.level.levelgen.feature.DoublePlantFeature;
import net.minecraft.world.level.levelgen.feature.EndCityFeature;
import net.minecraft.world.level.levelgen.feature.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.EndGatewayFeature;
import net.minecraft.world.level.levelgen.feature.EndIslandFeature;
import net.minecraft.world.level.levelgen.feature.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.FeatureRadius;
import net.minecraft.world.level.levelgen.feature.FillLayerFeature;
import net.minecraft.world.level.levelgen.feature.FlowerFeature;
import net.minecraft.world.level.levelgen.feature.ForestFlowerFeature;
import net.minecraft.world.level.levelgen.feature.FossilFeature;
import net.minecraft.world.level.levelgen.feature.GeneralForestFlowerFeature;
import net.minecraft.world.level.levelgen.feature.GlowstoneFeature;
import net.minecraft.world.level.levelgen.feature.GrassConfiguration;
import net.minecraft.world.level.levelgen.feature.GrassFeature;
import net.minecraft.world.level.levelgen.feature.GroundBushFeature;
import net.minecraft.world.level.levelgen.feature.HayBlockPileFeature;
import net.minecraft.world.level.levelgen.feature.HellFireFeature;
import net.minecraft.world.level.levelgen.feature.HellSpringConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeBrownMushroomFeature;
import net.minecraft.world.level.levelgen.feature.HugeMushroomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.HugeRedMushroomFeature;
import net.minecraft.world.level.levelgen.feature.IceBlockPileFeature;
import net.minecraft.world.level.levelgen.feature.IcePatchFeature;
import net.minecraft.world.level.levelgen.feature.IceSpikeFeature;
import net.minecraft.world.level.levelgen.feature.IcebergConfiguration;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraft.world.level.levelgen.feature.JungleGrassFeature;
import net.minecraft.world.level.levelgen.feature.JunglePyramidFeature;
import net.minecraft.world.level.levelgen.feature.JungleTreeFeature;
import net.minecraft.world.level.levelgen.feature.KelpFeature;
import net.minecraft.world.level.levelgen.feature.LakeConfiguration;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.MegaJungleTreeFeature;
import net.minecraft.world.level.levelgen.feature.MegaPineTreeFeature;
import net.minecraft.world.level.levelgen.feature.MelonBlockPileFeature;
import net.minecraft.world.level.levelgen.feature.MelonFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.NetherSpringFeature;
import net.minecraft.world.level.levelgen.feature.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostConfiguration;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.PineFeature;
import net.minecraft.world.level.levelgen.feature.PlainFlowerFeature;
import net.minecraft.world.level.levelgen.feature.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.PumpkinBlockPileFeature;
import net.minecraft.world.level.levelgen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.level.levelgen.feature.RandomBooleanSelectorFeature;
import net.minecraft.world.level.levelgen.feature.RandomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.RandomRandomFeature;
import net.minecraft.world.level.levelgen.feature.RandomRandomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.RandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.ReedsFeature;
import net.minecraft.world.level.levelgen.feature.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.ReplaceBlockFeature;
import net.minecraft.world.level.levelgen.feature.SavannaTreeFeature;
import net.minecraft.world.level.levelgen.feature.SeaPickleFeature;
import net.minecraft.world.level.levelgen.feature.SeagrassFeature;
import net.minecraft.world.level.levelgen.feature.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.ShipwreckFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.SimpleRandomFeatureConfig;
import net.minecraft.world.level.levelgen.feature.SimpleRandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.SnowBlockPileFeature;
import net.minecraft.world.level.levelgen.feature.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.SpruceFeature;
import net.minecraft.world.level.levelgen.feature.StrongholdFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.SwampFlowerFeature;
import net.minecraft.world.level.levelgen.feature.SwampTreeFeature;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.feature.TaigaGrassFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.VillageConfiguration;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.VinesFeature;
import net.minecraft.world.level.levelgen.feature.VoidStartPlatformFeature;
import net.minecraft.world.level.levelgen.feature.WaterlilyFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public abstract class Feature<FC extends FeatureConfiguration> {
    public static final StructureFeature<PillagerOutpostConfiguration> PILLAGER_OUTPOST = Feature.register("pillager_outpost", new PillagerOutpostFeature((Function<Dynamic<?>, ? extends PillagerOutpostConfiguration>)((Function<Dynamic<?>, PillagerOutpostConfiguration>)PillagerOutpostConfiguration::deserialize)));
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = Feature.register("mineshaft", new MineshaftFeature((Function<Dynamic<?>, ? extends MineshaftConfiguration>)((Function<Dynamic<?>, MineshaftConfiguration>)MineshaftConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = Feature.register("woodland_mansion", new WoodlandMansionFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = Feature.register("jungle_temple", new JunglePyramidFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = Feature.register("desert_pyramid", new DesertPyramidFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = Feature.register("igloo", new IglooFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = Feature.register("shipwreck", new ShipwreckFeature((Function<Dynamic<?>, ? extends ShipwreckConfiguration>)((Function<Dynamic<?>, ShipwreckConfiguration>)ShipwreckConfiguration::deserialize)));
    public static final SwamplandHutFeature SWAMP_HUT = Feature.register("swamp_hut", new SwamplandHutFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = Feature.register("stronghold", new StrongholdFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = Feature.register("ocean_monument", new OceanMonumentFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = Feature.register("ocean_ruin", new OceanRuinFeature((Function<Dynamic<?>, ? extends OceanRuinConfiguration>)((Function<Dynamic<?>, OceanRuinConfiguration>)OceanRuinConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = Feature.register("nether_bridge", new NetherFortressFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = Feature.register("end_city", new EndCityFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<BuriedTreasureConfiguration> BURIED_TREASURE = Feature.register("buried_treasure", new BuriedTreasureFeature((Function<Dynamic<?>, ? extends BuriedTreasureConfiguration>)((Function<Dynamic<?>, BuriedTreasureConfiguration>)BuriedTreasureConfiguration::deserialize)));
    public static final StructureFeature<VillageConfiguration> VILLAGE = Feature.register("village", new VillageFeature((Function<Dynamic<?>, ? extends VillageConfiguration>)((Function<Dynamic<?>, VillageConfiguration>)VillageConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> FANCY_TREE = Feature.register("fancy_tree", new BigTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), false));
    public static final Feature<NoneFeatureConfiguration> BIRCH_TREE = Feature.register("birch_tree", new BirchFeature(NoneFeatureConfiguration::deserialize, false, false));
    public static final Feature<NoneFeatureConfiguration> SUPER_BIRCH_TREE = Feature.register("super_birch_tree", new BirchFeature(NoneFeatureConfiguration::deserialize, false, true));
    public static final Feature<NoneFeatureConfiguration> JUNGLE_GROUND_BUSH = Feature.register("jungle_ground_bush", new GroundBushFeature(NoneFeatureConfiguration::deserialize, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.OAK_LEAVES.defaultBlockState()));
    public static final Feature<NoneFeatureConfiguration> JUNGLE_TREE = Feature.register("jungle_tree", new JungleTreeFeature(NoneFeatureConfiguration::deserialize, false, 4, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState(), true));
    public static final Feature<NoneFeatureConfiguration> PINE_TREE = Feature.register("pine_tree", new PineFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> DARK_OAK_TREE = Feature.register("dark_oak_tree", new DarkOakFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), false));
    public static final Feature<NoneFeatureConfiguration> SAVANNA_TREE = Feature.register("savanna_tree", new SavannaTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), false));
    public static final Feature<NoneFeatureConfiguration> SPRUCE_TREE = Feature.register("spruce_tree", new SpruceFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), false));
    public static final Feature<NoneFeatureConfiguration> SWAMP_TREE = Feature.register("swamp_tree", new SwampTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> NORMAL_TREE = Feature.register("normal_tree", new TreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), false));
    public static final Feature<NoneFeatureConfiguration> MEGA_JUNGLE_TREE = Feature.register("mega_jungle_tree", new MegaJungleTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize), false, 10, 20, Blocks.JUNGLE_LOG.defaultBlockState(), Blocks.JUNGLE_LEAVES.defaultBlockState()));
    public static final Feature<NoneFeatureConfiguration> MEGA_PINE_TREE = Feature.register("mega_pine_tree", new MegaPineTreeFeature(NoneFeatureConfiguration::deserialize, false, false));
    public static final Feature<NoneFeatureConfiguration> MEGA_SPRUCE_TREE = Feature.register("mega_spruce_tree", new MegaPineTreeFeature(NoneFeatureConfiguration::deserialize, false, true));
    public static final FlowerFeature DEFAULT_FLOWER = Feature.register("default_flower", new DefaultFlowerFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final FlowerFeature FOREST_FLOWER = Feature.register("forest_flower", new ForestFlowerFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final FlowerFeature PLAIN_FLOWER = Feature.register("plain_flower", new PlainFlowerFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final FlowerFeature SWAMP_FLOWER = Feature.register("swamp_flower", new SwampFlowerFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final FlowerFeature GENERAL_FOREST_FLOWER = Feature.register("general_forest_flower", new GeneralForestFlowerFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> JUNGLE_GRASS = Feature.register("jungle_grass", new JungleGrassFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> TAIGA_GRASS = Feature.register("taiga_grass", new TaigaGrassFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<GrassConfiguration> GRASS = Feature.register("grass", new GrassFeature((Function<Dynamic<?>, ? extends GrassConfiguration>)((Function<Dynamic<?>, GrassConfiguration>)GrassConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = Feature.register("void_start_platform", new VoidStartPlatformFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CACTUS = Feature.register("cactus", new CactusFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> DEAD_BUSH = Feature.register("dead_bush", new DeadBushFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> DESERT_WELL = Feature.register("desert_well", new DesertWellFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> FOSSIL = Feature.register("fossil", new FossilFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> HELL_FIRE = Feature.register("hell_fire", new HellFireFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<HugeMushroomFeatureConfig> HUGE_RED_MUSHROOM = Feature.register("huge_red_mushroom", new HugeRedMushroomFeature((Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig>)((Function<Dynamic<?>, HugeMushroomFeatureConfig>)HugeMushroomFeatureConfig::deserialize)));
    public static final Feature<HugeMushroomFeatureConfig> HUGE_BROWN_MUSHROOM = Feature.register("huge_brown_mushroom", new HugeBrownMushroomFeature((Function<Dynamic<?>, ? extends HugeMushroomFeatureConfig>)((Function<Dynamic<?>, HugeMushroomFeatureConfig>)HugeMushroomFeatureConfig::deserialize)));
    public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = Feature.register("ice_spike", new IceSpikeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = Feature.register("glowstone_blob", new GlowstoneFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> MELON = Feature.register("melon", new MelonFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> PUMPKIN = Feature.register("pumpkin", new CentralSpikedFeature(NoneFeatureConfiguration::deserialize, Blocks.PUMPKIN.defaultBlockState()));
    public static final Feature<NoneFeatureConfiguration> REED = Feature.register("reed", new ReedsFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = Feature.register("freeze_top_layer", new SnowAndFreezeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> VINES = Feature.register("vines", new VinesFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> WATERLILY = Feature.register("waterlily", new WaterlilyFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = Feature.register("monster_room", new MonsterRoomFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> BLUE_ICE = Feature.register("blue_ice", new BlueIceFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<IcebergConfiguration> ICEBERG = Feature.register("iceberg", new IcebergFeature((Function<Dynamic<?>, ? extends IcebergConfiguration>)((Function<Dynamic<?>, IcebergConfiguration>)IcebergConfiguration::deserialize)));
    public static final Feature<BlockBlobConfiguration> FOREST_ROCK = Feature.register("forest_rock", new BlockBlobFeature((Function<Dynamic<?>, ? extends BlockBlobConfiguration>)((Function<Dynamic<?>, BlockBlobConfiguration>)BlockBlobConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> HAY_PILE = Feature.register("hay_pile", new HayBlockPileFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> SNOW_PILE = Feature.register("snow_pile", new SnowBlockPileFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> ICE_PILE = Feature.register("ice_pile", new IceBlockPileFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> MELON_PILE = Feature.register("melon_pile", new MelonBlockPileFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> PUMPKIN_PILE = Feature.register("pumpkin_pile", new PumpkinBlockPileFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<BushConfiguration> BUSH = Feature.register("bush", new BushFeature((Function<Dynamic<?>, ? extends BushConfiguration>)((Function<Dynamic<?>, BushConfiguration>)BushConfiguration::deserialize)));
    public static final Feature<DiskConfiguration> DISK = Feature.register("disk", new DiskReplaceFeature((Function<Dynamic<?>, ? extends DiskConfiguration>)((Function<Dynamic<?>, DiskConfiguration>)DiskConfiguration::deserialize)));
    public static final Feature<DoublePlantConfiguration> DOUBLE_PLANT = Feature.register("double_plant", new DoublePlantFeature((Function<Dynamic<?>, ? extends DoublePlantConfiguration>)((Function<Dynamic<?>, DoublePlantConfiguration>)DoublePlantConfiguration::deserialize)));
    public static final Feature<HellSpringConfiguration> NETHER_SPRING = Feature.register("nether_spring", new NetherSpringFeature((Function<Dynamic<?>, ? extends HellSpringConfiguration>)((Function<Dynamic<?>, HellSpringConfiguration>)HellSpringConfiguration::deserialize)));
    public static final Feature<FeatureRadius> ICE_PATCH = Feature.register("ice_patch", new IcePatchFeature((Function<Dynamic<?>, ? extends FeatureRadius>)((Function<Dynamic<?>, FeatureRadius>)FeatureRadius::deserialize)));
    public static final Feature<LakeConfiguration> LAKE = Feature.register("lake", new LakeFeature((Function<Dynamic<?>, ? extends LakeConfiguration>)((Function<Dynamic<?>, LakeConfiguration>)LakeConfiguration::deserialize)));
    public static final Feature<OreConfiguration> ORE = Feature.register("ore", new OreFeature((Function<Dynamic<?>, ? extends OreConfiguration>)((Function<Dynamic<?>, OreConfiguration>)OreConfiguration::deserialize)));
    public static final Feature<RandomRandomFeatureConfig> RANDOM_RANDOM_SELECTOR = Feature.register("random_random_selector", new RandomRandomFeature((Function<Dynamic<?>, ? extends RandomRandomFeatureConfig>)((Function<Dynamic<?>, RandomRandomFeatureConfig>)RandomRandomFeatureConfig::deserialize)));
    public static final Feature<RandomFeatureConfig> RANDOM_SELECTOR = Feature.register("random_selector", new RandomSelectorFeature((Function<Dynamic<?>, ? extends RandomFeatureConfig>)((Function<Dynamic<?>, RandomFeatureConfig>)RandomFeatureConfig::deserialize)));
    public static final Feature<SimpleRandomFeatureConfig> SIMPLE_RANDOM_SELECTOR = Feature.register("simple_random_selector", new SimpleRandomSelectorFeature((Function<Dynamic<?>, ? extends SimpleRandomFeatureConfig>)((Function<Dynamic<?>, SimpleRandomFeatureConfig>)SimpleRandomFeatureConfig::deserialize)));
    public static final Feature<RandomBooleanFeatureConfig> RANDOM_BOOLEAN_SELECTOR = Feature.register("random_boolean_selector", new RandomBooleanSelectorFeature((Function<Dynamic<?>, ? extends RandomBooleanFeatureConfig>)((Function<Dynamic<?>, RandomBooleanFeatureConfig>)RandomBooleanFeatureConfig::deserialize)));
    public static final Feature<ReplaceBlockConfiguration> EMERALD_ORE = Feature.register("emerald_ore", new ReplaceBlockFeature((Function<Dynamic<?>, ? extends ReplaceBlockConfiguration>)((Function<Dynamic<?>, ReplaceBlockConfiguration>)ReplaceBlockConfiguration::deserialize)));
    public static final Feature<SpringConfiguration> SPRING = Feature.register("spring_feature", new SpringFeature((Function<Dynamic<?>, ? extends SpringConfiguration>)((Function<Dynamic<?>, SpringConfiguration>)SpringConfiguration::deserialize)));
    public static final Feature<SpikeConfiguration> END_SPIKE = Feature.register("end_spike", new SpikeFeature((Function<Dynamic<?>, ? extends SpikeConfiguration>)((Function<Dynamic<?>, SpikeConfiguration>)SpikeConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> END_ISLAND = Feature.register("end_island", new EndIslandFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = Feature.register("chorus_plant", new ChorusPlantFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<EndGatewayConfiguration> END_GATEWAY = Feature.register("end_gateway", new EndGatewayFeature((Function<Dynamic<?>, ? extends EndGatewayConfiguration>)((Function<Dynamic<?>, EndGatewayConfiguration>)EndGatewayConfiguration::deserialize)));
    public static final Feature<SeagrassFeatureConfiguration> SEAGRASS = Feature.register("seagrass", new SeagrassFeature((Function<Dynamic<?>, ? extends SeagrassFeatureConfiguration>)((Function<Dynamic<?>, SeagrassFeatureConfiguration>)SeagrassFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> KELP = Feature.register("kelp", new KelpFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CORAL_TREE = Feature.register("coral_tree", new CoralTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = Feature.register("coral_mushroom", new CoralMushroomFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = Feature.register("coral_claw", new CoralClawFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<CountFeatureConfiguration> SEA_PICKLE = Feature.register("sea_pickle", new SeaPickleFeature((Function<Dynamic<?>, ? extends CountFeatureConfiguration>)((Function<Dynamic<?>, CountFeatureConfiguration>)CountFeatureConfiguration::deserialize)));
    public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = Feature.register("simple_block", new SimpleBlockFeature((Function<Dynamic<?>, ? extends SimpleBlockConfiguration>)((Function<Dynamic<?>, SimpleBlockConfiguration>)SimpleBlockConfiguration::deserialize)));
    public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = Feature.register("bamboo", new BambooFeature((Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration>)((Function<Dynamic<?>, ProbabilityFeatureConfiguration>)ProbabilityFeatureConfiguration::deserialize)));
    public static final Feature<DecoratedFeatureConfiguration> DECORATED = Feature.register("decorated", new DecoratedFeature((Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration>)((Function<Dynamic<?>, DecoratedFeatureConfiguration>)DecoratedFeatureConfiguration::deserialize)));
    public static final Feature<DecoratedFeatureConfiguration> DECORATED_FLOWER = Feature.register("decorated_flower", new DecoratedFlowerFeature((Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration>)((Function<Dynamic<?>, DecoratedFeatureConfiguration>)DecoratedFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> SWEET_BERRY_BUSH = Feature.register("sweet_berry_bush", new CentralSpikedFeature(NoneFeatureConfiguration::deserialize, (BlockState)Blocks.SWEET_BERRY_BUSH.defaultBlockState().setValue(SweetBerryBushBlock.AGE, 3)));
    public static final Feature<LayerConfiguration> FILL_LAYER = Feature.register("fill_layer", new FillLayerFeature((Function<Dynamic<?>, ? extends LayerConfiguration>)((Function<Dynamic<?>, LayerConfiguration>)LayerConfiguration::deserialize)));
    public static final BonusChestFeature BONUS_CHEST = Feature.register("bonus_chest", new BonusChestFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
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
        return (F)Registry.register(Registry.FEATURE, string, feature);
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
        return (FC)((FeatureConfiguration)this.configurationFactory.apply(dynamic));
    }

    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        if (this.doUpdate) {
            levelWriter.setBlock(blockPos, blockState, 3);
        } else {
            levelWriter.setBlock(blockPos, blockState, 2);
        }
    }

    public abstract boolean place(LevelAccessor var1, ChunkGenerator<? extends ChunkGeneratorSettings> var2, Random var3, BlockPos var4, FC var5);

    public List<Biome.SpawnerData> getSpecialEnemies() {
        return Collections.emptyList();
    }

    public List<Biome.SpawnerData> getSpecialAnimals() {
        return Collections.emptyList();
    }
}

