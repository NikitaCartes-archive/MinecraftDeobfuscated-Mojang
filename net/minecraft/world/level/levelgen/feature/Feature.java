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
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.LevelWriter;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.AbstractFlowerFeature;
import net.minecraft.world.level.levelgen.feature.BambooFeature;
import net.minecraft.world.level.levelgen.feature.BasaltColumnsFeature;
import net.minecraft.world.level.levelgen.feature.BasaltPillarFeature;
import net.minecraft.world.level.levelgen.feature.BastionFeature;
import net.minecraft.world.level.levelgen.feature.BlockBlobFeature;
import net.minecraft.world.level.levelgen.feature.BlockPileFeature;
import net.minecraft.world.level.levelgen.feature.BlueIceFeature;
import net.minecraft.world.level.levelgen.feature.BonusChestFeature;
import net.minecraft.world.level.levelgen.feature.BuriedTreasureFeature;
import net.minecraft.world.level.levelgen.feature.ChorusPlantFeature;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.CoralClawFeature;
import net.minecraft.world.level.levelgen.feature.CoralMushroomFeature;
import net.minecraft.world.level.levelgen.feature.CoralTreeFeature;
import net.minecraft.world.level.levelgen.feature.DecoratedFeature;
import net.minecraft.world.level.levelgen.feature.DecoratedFlowerFeature;
import net.minecraft.world.level.levelgen.feature.DefaultFlowerFeature;
import net.minecraft.world.level.levelgen.feature.DeltaFeature;
import net.minecraft.world.level.levelgen.feature.DesertPyramidFeature;
import net.minecraft.world.level.levelgen.feature.DesertWellFeature;
import net.minecraft.world.level.levelgen.feature.DiskReplaceFeature;
import net.minecraft.world.level.levelgen.feature.EndCityFeature;
import net.minecraft.world.level.levelgen.feature.EndGatewayFeature;
import net.minecraft.world.level.levelgen.feature.EndIslandFeature;
import net.minecraft.world.level.levelgen.feature.FillLayerFeature;
import net.minecraft.world.level.levelgen.feature.FossilFeature;
import net.minecraft.world.level.levelgen.feature.GlowstoneFeature;
import net.minecraft.world.level.levelgen.feature.HugeBrownMushroomFeature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.HugeFungusFeature;
import net.minecraft.world.level.levelgen.feature.HugeRedMushroomFeature;
import net.minecraft.world.level.levelgen.feature.IcePatchFeature;
import net.minecraft.world.level.levelgen.feature.IceSpikeFeature;
import net.minecraft.world.level.levelgen.feature.IcebergFeature;
import net.minecraft.world.level.levelgen.feature.IglooFeature;
import net.minecraft.world.level.levelgen.feature.JunglePyramidFeature;
import net.minecraft.world.level.levelgen.feature.KelpFeature;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.feature.MineshaftFeature;
import net.minecraft.world.level.levelgen.feature.MonsterRoomFeature;
import net.minecraft.world.level.levelgen.feature.NetherForestVegetationFeature;
import net.minecraft.world.level.levelgen.feature.NetherFortressFeature;
import net.minecraft.world.level.levelgen.feature.NoOpFeature;
import net.minecraft.world.level.levelgen.feature.NoSurfaceOreFeature;
import net.minecraft.world.level.levelgen.feature.OceanMonumentFeature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.PillagerOutpostFeature;
import net.minecraft.world.level.levelgen.feature.RandomBooleanSelectorFeature;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.RandomRandomFeature;
import net.minecraft.world.level.levelgen.feature.RandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.ReplaceBlobsFeature;
import net.minecraft.world.level.levelgen.feature.ReplaceBlockFeature;
import net.minecraft.world.level.levelgen.feature.RuinedPortalFeature;
import net.minecraft.world.level.levelgen.feature.SeaPickleFeature;
import net.minecraft.world.level.levelgen.feature.SeagrassFeature;
import net.minecraft.world.level.levelgen.feature.ShipwreckFeature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.SimpleRandomSelectorFeature;
import net.minecraft.world.level.levelgen.feature.SnowAndFreezeFeature;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;
import net.minecraft.world.level.levelgen.feature.SpringFeature;
import net.minecraft.world.level.levelgen.feature.StrongholdFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.SwamplandHutFeature;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.TwistingVinesFeature;
import net.minecraft.world.level.levelgen.feature.VillageFeature;
import net.minecraft.world.level.levelgen.feature.VinesFeature;
import net.minecraft.world.level.levelgen.feature.VoidStartPlatformFeature;
import net.minecraft.world.level.levelgen.feature.WeepingVinesFeature;
import net.minecraft.world.level.levelgen.feature.WoodlandMansionFeature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockBlobConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockPileConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.BuriedTreasureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.CountFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DecoratedFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DiskConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.EndGatewayConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureRadiusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.LayerConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MineshaftConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.MultiJigsawConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OceanRuinConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ProbabilityFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomBooleanFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSpheroidConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RuinedPortalConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SeagrassFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ShipwreckConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleRandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpikeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.structure.NetherFossilFeature;
import net.minecraft.world.level.levelgen.structure.OceanRuinFeature;

public abstract class Feature<FC extends FeatureConfiguration> {
    public static final StructureFeature<NoneFeatureConfiguration> PILLAGER_OUTPOST = Feature.register("pillager_outpost", new PillagerOutpostFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<MineshaftConfiguration> MINESHAFT = Feature.register("mineshaft", new MineshaftFeature((Function<Dynamic<?>, ? extends MineshaftConfiguration>)((Function<Dynamic<?>, MineshaftConfiguration>)MineshaftConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> WOODLAND_MANSION = Feature.register("woodland_mansion", new WoodlandMansionFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> JUNGLE_TEMPLE = Feature.register("jungle_temple", new JunglePyramidFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> DESERT_PYRAMID = Feature.register("desert_pyramid", new DesertPyramidFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> IGLOO = Feature.register("igloo", new IglooFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<RuinedPortalConfiguration> RUINED_PORTAL = Feature.register("ruined_portal", new RuinedPortalFeature((Function<Dynamic<?>, ? extends RuinedPortalConfiguration>)((Function<Dynamic<?>, RuinedPortalConfiguration>)RuinedPortalConfiguration::deserialize)));
    public static final StructureFeature<ShipwreckConfiguration> SHIPWRECK = Feature.register("shipwreck", new ShipwreckFeature((Function<Dynamic<?>, ? extends ShipwreckConfiguration>)((Function<Dynamic<?>, ShipwreckConfiguration>)ShipwreckConfiguration::deserialize)));
    public static final SwamplandHutFeature SWAMP_HUT = Feature.register("swamp_hut", new SwamplandHutFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> STRONGHOLD = Feature.register("stronghold", new StrongholdFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> OCEAN_MONUMENT = Feature.register("ocean_monument", new OceanMonumentFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<OceanRuinConfiguration> OCEAN_RUIN = Feature.register("ocean_ruin", new OceanRuinFeature((Function<Dynamic<?>, ? extends OceanRuinConfiguration>)((Function<Dynamic<?>, OceanRuinConfiguration>)OceanRuinConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_BRIDGE = Feature.register("nether_bridge", new NetherFortressFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> END_CITY = Feature.register("end_city", new EndCityFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<BuriedTreasureConfiguration> BURIED_TREASURE = Feature.register("buried_treasure", new BuriedTreasureFeature((Function<Dynamic<?>, ? extends BuriedTreasureConfiguration>)((Function<Dynamic<?>, BuriedTreasureConfiguration>)BuriedTreasureConfiguration::deserialize)));
    public static final StructureFeature<JigsawConfiguration> VILLAGE = Feature.register("village", new VillageFeature((Function<Dynamic<?>, ? extends JigsawConfiguration>)((Function<Dynamic<?>, JigsawConfiguration>)JigsawConfiguration::deserialize)));
    public static final StructureFeature<NoneFeatureConfiguration> NETHER_FOSSIL = Feature.register("nether_fossil", new NetherFossilFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final StructureFeature<MultiJigsawConfiguration> BASTION_REMNANT = Feature.register("bastion_remnant", new BastionFeature((Function<Dynamic<?>, ? extends MultiJigsawConfiguration>)((Function<Dynamic<?>, MultiJigsawConfiguration>)MultiJigsawConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> NO_OP = Feature.register("no_op", new NoOpFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<TreeConfiguration> TREE = Feature.register("tree", new TreeFeature((Function<Dynamic<?>, ? extends TreeConfiguration>)((Function<Dynamic<?>, TreeConfiguration>)TreeConfiguration::deserialize)));
    public static final AbstractFlowerFeature<RandomPatchConfiguration> FLOWER = Feature.register("flower", new DefaultFlowerFeature((Function<Dynamic<?>, ? extends RandomPatchConfiguration>)((Function<Dynamic<?>, RandomPatchConfiguration>)RandomPatchConfiguration::deserialize)));
    public static final Feature<RandomPatchConfiguration> RANDOM_PATCH = Feature.register("random_patch", new RandomPatchFeature((Function<Dynamic<?>, ? extends RandomPatchConfiguration>)((Function<Dynamic<?>, RandomPatchConfiguration>)RandomPatchConfiguration::deserialize)));
    public static final Feature<BlockPileConfiguration> BLOCK_PILE = Feature.register("block_pile", new BlockPileFeature((Function<Dynamic<?>, ? extends BlockPileConfiguration>)((Function<Dynamic<?>, BlockPileConfiguration>)BlockPileConfiguration::deserialize)));
    public static final Feature<SpringConfiguration> SPRING = Feature.register("spring_feature", new SpringFeature((Function<Dynamic<?>, ? extends SpringConfiguration>)((Function<Dynamic<?>, SpringConfiguration>)SpringConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CHORUS_PLANT = Feature.register("chorus_plant", new ChorusPlantFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<ReplaceBlockConfiguration> EMERALD_ORE = Feature.register("emerald_ore", new ReplaceBlockFeature((Function<Dynamic<?>, ? extends ReplaceBlockConfiguration>)((Function<Dynamic<?>, ReplaceBlockConfiguration>)ReplaceBlockConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> VOID_START_PLATFORM = Feature.register("void_start_platform", new VoidStartPlatformFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> DESERT_WELL = Feature.register("desert_well", new DesertWellFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> FOSSIL = Feature.register("fossil", new FossilFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_RED_MUSHROOM = Feature.register("huge_red_mushroom", new HugeRedMushroomFeature((Function<Dynamic<?>, ? extends HugeMushroomFeatureConfiguration>)((Function<Dynamic<?>, HugeMushroomFeatureConfiguration>)HugeMushroomFeatureConfiguration::deserialize)));
    public static final Feature<HugeMushroomFeatureConfiguration> HUGE_BROWN_MUSHROOM = Feature.register("huge_brown_mushroom", new HugeBrownMushroomFeature((Function<Dynamic<?>, ? extends HugeMushroomFeatureConfiguration>)((Function<Dynamic<?>, HugeMushroomFeatureConfiguration>)HugeMushroomFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> ICE_SPIKE = Feature.register("ice_spike", new IceSpikeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> GLOWSTONE_BLOB = Feature.register("glowstone_blob", new GlowstoneFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> FREEZE_TOP_LAYER = Feature.register("freeze_top_layer", new SnowAndFreezeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> VINES = Feature.register("vines", new VinesFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> MONSTER_ROOM = Feature.register("monster_room", new MonsterRoomFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> BLUE_ICE = Feature.register("blue_ice", new BlueIceFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<BlockStateConfiguration> ICEBERG = Feature.register("iceberg", new IcebergFeature((Function<Dynamic<?>, ? extends BlockStateConfiguration>)((Function<Dynamic<?>, BlockStateConfiguration>)BlockStateConfiguration::deserialize)));
    public static final Feature<BlockBlobConfiguration> FOREST_ROCK = Feature.register("forest_rock", new BlockBlobFeature((Function<Dynamic<?>, ? extends BlockBlobConfiguration>)((Function<Dynamic<?>, BlockBlobConfiguration>)BlockBlobConfiguration::deserialize)));
    public static final Feature<DiskConfiguration> DISK = Feature.register("disk", new DiskReplaceFeature((Function<Dynamic<?>, ? extends DiskConfiguration>)((Function<Dynamic<?>, DiskConfiguration>)DiskConfiguration::deserialize)));
    public static final Feature<FeatureRadiusConfiguration> ICE_PATCH = Feature.register("ice_patch", new IcePatchFeature((Function<Dynamic<?>, ? extends FeatureRadiusConfiguration>)((Function<Dynamic<?>, FeatureRadiusConfiguration>)FeatureRadiusConfiguration::deserialize)));
    public static final Feature<BlockStateConfiguration> LAKE = Feature.register("lake", new LakeFeature((Function<Dynamic<?>, ? extends BlockStateConfiguration>)((Function<Dynamic<?>, BlockStateConfiguration>)BlockStateConfiguration::deserialize)));
    public static final Feature<OreConfiguration> ORE = Feature.register("ore", new OreFeature((Function<Dynamic<?>, ? extends OreConfiguration>)((Function<Dynamic<?>, OreConfiguration>)OreConfiguration::deserialize)));
    public static final Feature<SpikeConfiguration> END_SPIKE = Feature.register("end_spike", new SpikeFeature((Function<Dynamic<?>, ? extends SpikeConfiguration>)((Function<Dynamic<?>, SpikeConfiguration>)SpikeConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> END_ISLAND = Feature.register("end_island", new EndIslandFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<EndGatewayConfiguration> END_GATEWAY = Feature.register("end_gateway", new EndGatewayFeature((Function<Dynamic<?>, ? extends EndGatewayConfiguration>)((Function<Dynamic<?>, EndGatewayConfiguration>)EndGatewayConfiguration::deserialize)));
    public static final Feature<SeagrassFeatureConfiguration> SEAGRASS = Feature.register("seagrass", new SeagrassFeature((Function<Dynamic<?>, ? extends SeagrassFeatureConfiguration>)((Function<Dynamic<?>, SeagrassFeatureConfiguration>)SeagrassFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> KELP = Feature.register("kelp", new KelpFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CORAL_TREE = Feature.register("coral_tree", new CoralTreeFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CORAL_MUSHROOM = Feature.register("coral_mushroom", new CoralMushroomFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> CORAL_CLAW = Feature.register("coral_claw", new CoralClawFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<CountFeatureConfiguration> SEA_PICKLE = Feature.register("sea_pickle", new SeaPickleFeature((Function<Dynamic<?>, ? extends CountFeatureConfiguration>)((Function<Dynamic<?>, CountFeatureConfiguration>)CountFeatureConfiguration::deserialize)));
    public static final Feature<SimpleBlockConfiguration> SIMPLE_BLOCK = Feature.register("simple_block", new SimpleBlockFeature((Function<Dynamic<?>, ? extends SimpleBlockConfiguration>)((Function<Dynamic<?>, SimpleBlockConfiguration>)SimpleBlockConfiguration::deserialize)));
    public static final Feature<ProbabilityFeatureConfiguration> BAMBOO = Feature.register("bamboo", new BambooFeature((Function<Dynamic<?>, ? extends ProbabilityFeatureConfiguration>)((Function<Dynamic<?>, ProbabilityFeatureConfiguration>)ProbabilityFeatureConfiguration::deserialize)));
    public static final Feature<HugeFungusConfiguration> HUGE_FUNGUS = Feature.register("huge_fungus", new HugeFungusFeature((Function<Dynamic<?>, ? extends HugeFungusConfiguration>)((Function<Dynamic<?>, HugeFungusConfiguration>)HugeFungusConfiguration::deserialize)));
    public static final Feature<BlockPileConfiguration> NETHER_FOREST_VEGETATION = Feature.register("nether_forest_vegetation", new NetherForestVegetationFeature((Function<Dynamic<?>, ? extends BlockPileConfiguration>)((Function<Dynamic<?>, BlockPileConfiguration>)BlockPileConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> WEEPING_VINES = Feature.register("weeping_vines", new WeepingVinesFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> TWISTING_VINES = Feature.register("twisting_vines", new TwistingVinesFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<ColumnFeatureConfiguration> BASALT_COLUMNS = Feature.register("basalt_columns", new BasaltColumnsFeature((Function<Dynamic<?>, ? extends ColumnFeatureConfiguration>)((Function<Dynamic<?>, ColumnFeatureConfiguration>)ColumnFeatureConfiguration::deserialize)));
    public static final Feature<DeltaFeatureConfiguration> DELTA_FEATURE = Feature.register("delta_feature", new DeltaFeature((Function<Dynamic<?>, ? extends DeltaFeatureConfiguration>)((Function<Dynamic<?>, DeltaFeatureConfiguration>)DeltaFeatureConfiguration::deserialize)));
    public static final Feature<ReplaceSpheroidConfiguration> REPLACE_BLOBS = Feature.register("netherrack_replace_blobs", new ReplaceBlobsFeature((Function<Dynamic<?>, ? extends ReplaceSpheroidConfiguration>)((Function<Dynamic<?>, ReplaceSpheroidConfiguration>)ReplaceSpheroidConfiguration::deserialize)));
    public static final Feature<LayerConfiguration> FILL_LAYER = Feature.register("fill_layer", new FillLayerFeature((Function<Dynamic<?>, ? extends LayerConfiguration>)((Function<Dynamic<?>, LayerConfiguration>)LayerConfiguration::deserialize)));
    public static final BonusChestFeature BONUS_CHEST = Feature.register("bonus_chest", new BonusChestFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<NoneFeatureConfiguration> BASALT_PILLAR = Feature.register("basalt_pillar", new BasaltPillarFeature((Function<Dynamic<?>, ? extends NoneFeatureConfiguration>)((Function<Dynamic<?>, NoneFeatureConfiguration>)NoneFeatureConfiguration::deserialize)));
    public static final Feature<OreConfiguration> NO_SURFACE_ORE = Feature.register("no_surface_ore", new NoSurfaceOreFeature((Function<Dynamic<?>, ? extends OreConfiguration>)((Function<Dynamic<?>, OreConfiguration>)OreConfiguration::deserialize)));
    public static final Feature<RandomRandomFeatureConfiguration> RANDOM_RANDOM_SELECTOR = Feature.register("random_random_selector", new RandomRandomFeature((Function<Dynamic<?>, ? extends RandomRandomFeatureConfiguration>)((Function<Dynamic<?>, RandomRandomFeatureConfiguration>)RandomRandomFeatureConfiguration::deserialize)));
    public static final Feature<RandomFeatureConfiguration> RANDOM_SELECTOR = Feature.register("random_selector", new RandomSelectorFeature((Function<Dynamic<?>, ? extends RandomFeatureConfiguration>)((Function<Dynamic<?>, RandomFeatureConfiguration>)RandomFeatureConfiguration::deserialize)));
    public static final Feature<SimpleRandomFeatureConfiguration> SIMPLE_RANDOM_SELECTOR = Feature.register("simple_random_selector", new SimpleRandomSelectorFeature((Function<Dynamic<?>, ? extends SimpleRandomFeatureConfiguration>)((Function<Dynamic<?>, SimpleRandomFeatureConfiguration>)SimpleRandomFeatureConfiguration::deserialize)));
    public static final Feature<RandomBooleanFeatureConfiguration> RANDOM_BOOLEAN_SELECTOR = Feature.register("random_boolean_selector", new RandomBooleanSelectorFeature((Function<Dynamic<?>, ? extends RandomBooleanFeatureConfiguration>)((Function<Dynamic<?>, RandomBooleanFeatureConfiguration>)RandomBooleanFeatureConfiguration::deserialize)));
    public static final Feature<DecoratedFeatureConfiguration> DECORATED = Feature.register("decorated", new DecoratedFeature((Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration>)((Function<Dynamic<?>, DecoratedFeatureConfiguration>)DecoratedFeatureConfiguration::deserialize)));
    public static final Feature<DecoratedFeatureConfiguration> DECORATED_FLOWER = Feature.register("decorated_flower", new DecoratedFlowerFeature((Function<Dynamic<?>, ? extends DecoratedFeatureConfiguration>)((Function<Dynamic<?>, DecoratedFeatureConfiguration>)DecoratedFeatureConfiguration::deserialize)));
    public static final BiMap<String, StructureFeature<?>> STRUCTURES_REGISTRY = Util.make(HashBiMap.create(), hashBiMap -> {
        hashBiMap.put("Pillager_Outpost".toLowerCase(Locale.ROOT), PILLAGER_OUTPOST);
        hashBiMap.put("Mineshaft".toLowerCase(Locale.ROOT), MINESHAFT);
        hashBiMap.put("Mansion".toLowerCase(Locale.ROOT), WOODLAND_MANSION);
        hashBiMap.put("Jungle_Pyramid".toLowerCase(Locale.ROOT), JUNGLE_TEMPLE);
        hashBiMap.put("Desert_Pyramid".toLowerCase(Locale.ROOT), DESERT_PYRAMID);
        hashBiMap.put("Igloo".toLowerCase(Locale.ROOT), IGLOO);
        hashBiMap.put("Ruined_Portal".toLowerCase(Locale.ROOT), RUINED_PORTAL);
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
        hashBiMap.put("Bastion_Remnant".toLowerCase(Locale.ROOT), BASTION_REMNANT);
    });
    public static final List<StructureFeature<?>> NOISE_AFFECTING_FEATURES = ImmutableList.of(PILLAGER_OUTPOST, VILLAGE, NETHER_FOSSIL);
    private final Function<Dynamic<?>, ? extends FC> configurationFactory;

    private static <C extends FeatureConfiguration, F extends Feature<C>> F register(String string, F feature) {
        return (F)Registry.register(Registry.FEATURE, string, feature);
    }

    public Feature(Function<Dynamic<?>, ? extends FC> function) {
        this.configurationFactory = function;
    }

    public ConfiguredFeature<FC, ?> configured(FC featureConfiguration) {
        return new ConfiguredFeature<FC, Feature>(this, featureConfiguration);
    }

    public FC createSettings(Dynamic<?> dynamic) {
        return (FC)((FeatureConfiguration)this.configurationFactory.apply(dynamic));
    }

    protected void setBlock(LevelWriter levelWriter, BlockPos blockPos, BlockState blockState) {
        levelWriter.setBlock(blockPos, blockState, 3);
    }

    public abstract boolean place(LevelAccessor var1, StructureFeatureManager var2, ChunkGenerator<? extends ChunkGeneratorSettings> var3, Random var4, BlockPos var5, FC var6);

    public List<Biome.SpawnerData> getSpecialEnemies() {
        return Collections.emptyList();
    }

    public List<Biome.SpawnerData> getSpecialAnimals() {
        return Collections.emptyList();
    }

    protected static boolean isStone(Block block) {
        return block == Blocks.STONE || block == Blocks.GRANITE || block == Blocks.DIORITE || block == Blocks.ANDESITE;
    }

    public static boolean isDirt(Block block) {
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK || block == Blocks.PODZOL || block == Blocks.COARSE_DIRT || block == Blocks.MYCELIUM;
    }

    public static boolean isGrassOrDirt(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, blockState -> Feature.isDirt(blockState.getBlock()));
    }

    public static boolean isAir(LevelSimulatedReader levelSimulatedReader, BlockPos blockPos) {
        return levelSimulatedReader.isStateAtPosition(blockPos, BlockBehaviour.BlockStateBase::isAir);
    }
}

