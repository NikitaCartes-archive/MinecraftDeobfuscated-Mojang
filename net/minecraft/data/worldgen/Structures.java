/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.AncientCityStructurePieces;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasureStructure;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidStructure;
import net.minecraft.world.level.levelgen.structure.structures.EndCityStructure;
import net.minecraft.world.level.levelgen.structure.structures.IglooStructure;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.structures.JungleTempleStructure;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentStructure;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinStructure;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;

public class Structures {
    public static final Holder<Structure> PILLAGER_OUTPOST = Structures.register(BuiltinStructures.PILLAGER_OUTPOST, new JigsawStructure(Structures.structure(BiomeTags.HAS_PILLAGER_OUTPOST, Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1)}))), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN), PillagerOutpostPools.START, 7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final Holder<Structure> MINESHAFT = Structures.register(BuiltinStructures.MINESHAFT, new MineshaftStructure(Structures.structure(BiomeTags.HAS_MINESHAFT, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE), MineshaftStructure.Type.NORMAL));
    public static final Holder<Structure> MINESHAFT_MESA = Structures.register(BuiltinStructures.MINESHAFT_MESA, new MineshaftStructure(Structures.structure(BiomeTags.HAS_MINESHAFT_MESA, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE), MineshaftStructure.Type.MESA));
    public static final Holder<Structure> WOODLAND_MANSION = Structures.register(BuiltinStructures.WOODLAND_MANSION, new WoodlandMansionStructure(Structures.structure(BiomeTags.HAS_WOODLAND_MANSION, TerrainAdjustment.NONE)));
    public static final Holder<Structure> JUNGLE_TEMPLE = Structures.register(BuiltinStructures.JUNGLE_TEMPLE, new JungleTempleStructure(Structures.structure(BiomeTags.HAS_JUNGLE_TEMPLE, TerrainAdjustment.NONE)));
    public static final Holder<Structure> DESERT_PYRAMID = Structures.register(BuiltinStructures.DESERT_PYRAMID, new DesertPyramidStructure(Structures.structure(BiomeTags.HAS_DESERT_PYRAMID, TerrainAdjustment.NONE)));
    public static final Holder<Structure> IGLOO = Structures.register(BuiltinStructures.IGLOO, new IglooStructure(Structures.structure(BiomeTags.HAS_IGLOO, TerrainAdjustment.NONE)));
    public static final Holder<Structure> SHIPWRECK = Structures.register(BuiltinStructures.SHIPWRECK, new ShipwreckStructure(Structures.structure(BiomeTags.HAS_SHIPWRECK, TerrainAdjustment.NONE), false));
    public static final Holder<Structure> SHIPWRECK_BEACHED = Structures.register(BuiltinStructures.SHIPWRECK_BEACHED, new ShipwreckStructure(Structures.structure(BiomeTags.HAS_SHIPWRECK_BEACHED, TerrainAdjustment.NONE), true));
    public static final Holder<Structure> SWAMP_HUT = Structures.register(BuiltinStructures.SWAMP_HUT, new SwampHutStructure(Structures.structure(BiomeTags.HAS_SWAMP_HUT, Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)})), MobCategory.CREATURE, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1)}))), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
    public static final Holder<Structure> STRONGHOLD = Structures.register(BuiltinStructures.STRONGHOLD, new StrongholdStructure(Structures.structure(BiomeTags.HAS_STRONGHOLD, TerrainAdjustment.BURY)));
    public static final Holder<Structure> OCEAN_MONUMENT = Structures.register(BuiltinStructures.OCEAN_MONUMENT, new OceanMonumentStructure(Structures.structure(BiomeTags.HAS_OCEAN_MONUMENT, Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)})), MobCategory.UNDERGROUND_WATER_CREATURE, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST), MobCategory.AXOLOTLS, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST)), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
    public static final Holder<Structure> OCEAN_RUIN_COLD = Structures.register(BuiltinStructures.OCEAN_RUIN_COLD, new OceanRuinStructure(Structures.structure(BiomeTags.HAS_OCEAN_RUIN_COLD, TerrainAdjustment.NONE), OceanRuinStructure.Type.COLD, 0.3f, 0.9f));
    public static final Holder<Structure> OCEAN_RUIN_WARM = Structures.register(BuiltinStructures.OCEAN_RUIN_WARM, new OceanRuinStructure(Structures.structure(BiomeTags.HAS_OCEAN_RUIN_WARM, TerrainAdjustment.NONE), OceanRuinStructure.Type.WARM, 0.3f, 0.9f));
    public static final Holder<Structure> FORTRESS = Structures.register(BuiltinStructures.FORTRESS, new NetherFortressStructure(Structures.structure(BiomeTags.HAS_NETHER_FORTRESS, Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES)), GenerationStep.Decoration.UNDERGROUND_DECORATION, TerrainAdjustment.NONE)));
    public static final Holder<Structure> NETHER_FOSSIL = Structures.register(BuiltinStructures.NETHER_FOSSIL, new NetherFossilStructure(Structures.structure(BiomeTags.HAS_NETHER_FOSSIL, GenerationStep.Decoration.UNDERGROUND_DECORATION, TerrainAdjustment.BEARD_THIN), UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))));
    public static final Holder<Structure> END_CITY = Structures.register(BuiltinStructures.END_CITY, new EndCityStructure(Structures.structure(BiomeTags.HAS_END_CITY, TerrainAdjustment.NONE)));
    public static final Holder<Structure> BURIED_TREASURE = Structures.register(BuiltinStructures.BURIED_TREASURE, new BuriedTreasureStructure(Structures.structure(BiomeTags.HAS_BURIED_TREASURE, GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE)));
    public static final Holder<Structure> BASTION_REMNANT = Structures.register(BuiltinStructures.BASTION_REMNANT, new JigsawStructure(Structures.structure(BiomeTags.HAS_BASTION_REMNANT, TerrainAdjustment.NONE), BastionPieces.START, 6, ConstantHeight.of(VerticalAnchor.absolute(33)), false));
    public static final Holder<Structure> VILLAGE_PLAINS = Structures.register(BuiltinStructures.VILLAGE_PLAINS, new JigsawStructure(Structures.structure(BiomeTags.HAS_VILLAGE_PLAINS, TerrainAdjustment.BEARD_THIN), PlainVillagePools.START, 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final Holder<Structure> VILLAGE_DESERT = Structures.register(BuiltinStructures.VILLAGE_DESERT, new JigsawStructure(Structures.structure(BiomeTags.HAS_VILLAGE_DESERT, TerrainAdjustment.BEARD_THIN), DesertVillagePools.START, 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final Holder<Structure> VILLAGE_SAVANNA = Structures.register(BuiltinStructures.VILLAGE_SAVANNA, new JigsawStructure(Structures.structure(BiomeTags.HAS_VILLAGE_SAVANNA, TerrainAdjustment.BEARD_THIN), SavannaVillagePools.START, 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final Holder<Structure> VILLAGE_SNOWY = Structures.register(BuiltinStructures.VILLAGE_SNOWY, new JigsawStructure(Structures.structure(BiomeTags.HAS_VILLAGE_SNOWY, TerrainAdjustment.BEARD_THIN), SnowyVillagePools.START, 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final Holder<Structure> VILLAGE_TAIGA = Structures.register(BuiltinStructures.VILLAGE_TAIGA, new JigsawStructure(Structures.structure(BiomeTags.HAS_VILLAGE_TAIGA, TerrainAdjustment.BEARD_THIN), TaigaVillagePools.START, 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final Holder<Structure> RUINED_PORTAL_STANDARD = Structures.register(BuiltinStructures.RUINED_PORTAL_STANDARD, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_STANDARD, TerrainAdjustment.NONE), List.of(new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.UNDERGROUND, 1.0f, 0.2f, false, false, true, false, 0.5f), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.2f, false, false, true, false, 0.5f))));
    public static final Holder<Structure> RUINED_PORTAL_DESERT = Structures.register(BuiltinStructures.RUINED_PORTAL_DESERT, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_DESERT, TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED, 0.0f, 0.0f, false, false, false, false, 1.0f)));
    public static final Holder<Structure> RUINED_PORTAL_JUNGLE = Structures.register(BuiltinStructures.RUINED_PORTAL_JUNGLE, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_JUNGLE, TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.8f, true, true, false, false, 1.0f)));
    public static final Holder<Structure> RUINED_PORTAL_SWAMP = Structures.register(BuiltinStructures.RUINED_PORTAL_SWAMP, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_SWAMP, TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0f, 0.5f, false, true, false, false, 1.0f)));
    public static final Holder<Structure> RUINED_PORTAL_MOUNTAIN = Structures.register(BuiltinStructures.RUINED_PORTAL_MOUNTAIN, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN, TerrainAdjustment.NONE), List.of(new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN, 1.0f, 0.2f, false, false, true, false, 0.5f), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.2f, false, false, true, false, 0.5f))));
    public static final Holder<Structure> RUINED_PORTAL_OCEAN = Structures.register(BuiltinStructures.RUINED_PORTAL_OCEAN, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_OCEAN, TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0f, 0.8f, false, false, true, false, 1.0f)));
    public static final Holder<Structure> RUINED_PORTAL_NETHER = Structures.register(BuiltinStructures.RUINED_PORTAL_NETHER, new RuinedPortalStructure(Structures.structure(BiomeTags.HAS_RUINED_PORTAL_NETHER, TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_NETHER, 0.5f, 0.0f, false, false, false, true, 1.0f)));
    public static final Holder<Structure> ANCIENT_CITY = Structures.register(BuiltinStructures.ANCIENT_CITY, new JigsawStructure(Structures.structure(BiomeTags.HAS_ANCIENT_CITY, Arrays.stream(MobCategory.values()).collect(Collectors.toMap(mobCategory -> mobCategory, mobCategory -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), GenerationStep.Decoration.UNDERGROUND_DECORATION, TerrainAdjustment.BEARD_BOX), AncientCityStructurePieces.START, 7, ConstantHeight.of(VerticalAnchor.absolute(-51)), false, Optional.empty(), 100));

    public static Holder<? extends Structure> bootstrap() {
        return MINESHAFT;
    }

    private static Structure.StructureSettings structure(TagKey<Biome> tagKey, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, TerrainAdjustment terrainAdjustment) {
        return new Structure.StructureSettings(Structures.biomes(tagKey), map, decoration, terrainAdjustment);
    }

    private static Structure.StructureSettings structure(TagKey<Biome> tagKey, GenerationStep.Decoration decoration, TerrainAdjustment terrainAdjustment) {
        return Structures.structure(tagKey, Map.of(), decoration, terrainAdjustment);
    }

    private static Structure.StructureSettings structure(TagKey<Biome> tagKey, TerrainAdjustment terrainAdjustment) {
        return Structures.structure(tagKey, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, terrainAdjustment);
    }

    private static Holder<Structure> register(ResourceKey<Structure> resourceKey, Structure structure) {
        return BuiltinRegistries.register(BuiltinRegistries.STRUCTURES, resourceKey, structure);
    }

    private static HolderSet<Biome> biomes(TagKey<Biome> tagKey) {
        return BuiltinRegistries.BIOME.getOrCreateTag(tagKey);
    }
}

