/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.AncientCityStructurePieces;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
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
    private static Structure.StructureSettings structure(HolderSet<Biome> holderSet, Map<MobCategory, StructureSpawnOverride> map, GenerationStep.Decoration decoration, TerrainAdjustment terrainAdjustment) {
        return new Structure.StructureSettings(holderSet, map, decoration, terrainAdjustment);
    }

    private static Structure.StructureSettings structure(HolderSet<Biome> holderSet, GenerationStep.Decoration decoration, TerrainAdjustment terrainAdjustment) {
        return Structures.structure(holderSet, Map.of(), decoration, terrainAdjustment);
    }

    private static Structure.StructureSettings structure(HolderSet<Biome> holderSet, TerrainAdjustment terrainAdjustment) {
        return Structures.structure(holderSet, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, terrainAdjustment);
    }

    public static void bootstrap(BootstapContext<Structure> bootstapContext) {
        HolderGetter<Biome> holderGetter = bootstapContext.lookup(Registry.BIOME_REGISTRY);
        HolderGetter<StructureTemplatePool> holderGetter2 = bootstapContext.lookup(Registry.TEMPLATE_POOL_REGISTRY);
        bootstapContext.register(BuiltinStructures.PILLAGER_OUTPOST, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_PILLAGER_OUTPOST), Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1)}))), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN), holderGetter2.getOrThrow(PillagerOutpostPools.START), 7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        bootstapContext.register(BuiltinStructures.MINESHAFT, new MineshaftStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_MINESHAFT), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE), MineshaftStructure.Type.NORMAL));
        bootstapContext.register(BuiltinStructures.MINESHAFT_MESA, new MineshaftStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_MINESHAFT_MESA), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE), MineshaftStructure.Type.MESA));
        bootstapContext.register(BuiltinStructures.WOODLAND_MANSION, new WoodlandMansionStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_WOODLAND_MANSION), TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.JUNGLE_TEMPLE, new JungleTempleStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_JUNGLE_TEMPLE), TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.DESERT_PYRAMID, new DesertPyramidStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_DESERT_PYRAMID), TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.IGLOO, new IglooStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_IGLOO), TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.SHIPWRECK, new ShipwreckStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_SHIPWRECK), TerrainAdjustment.NONE), false));
        bootstapContext.register(BuiltinStructures.SHIPWRECK_BEACHED, new ShipwreckStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_SHIPWRECK_BEACHED), TerrainAdjustment.NONE), true));
        bootstapContext.register(BuiltinStructures.SWAMP_HUT, new SwampHutStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_SWAMP_HUT), Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1)})), MobCategory.CREATURE, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1)}))), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.STRONGHOLD, new StrongholdStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_STRONGHOLD), TerrainAdjustment.BURY)));
        bootstapContext.register(BuiltinStructures.OCEAN_MONUMENT, new OceanMonumentStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_OCEAN_MONUMENT), Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create((WeightedEntry[])new MobSpawnSettings.SpawnerData[]{new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4)})), MobCategory.UNDERGROUND_WATER_CREATURE, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST), MobCategory.AXOLOTLS, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST)), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.OCEAN_RUIN_COLD, new OceanRuinStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_COLD), TerrainAdjustment.NONE), OceanRuinStructure.Type.COLD, 0.3f, 0.9f));
        bootstapContext.register(BuiltinStructures.OCEAN_RUIN_WARM, new OceanRuinStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_WARM), TerrainAdjustment.NONE), OceanRuinStructure.Type.WARM, 0.3f, 0.9f));
        bootstapContext.register(BuiltinStructures.FORTRESS, new NetherFortressStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_NETHER_FORTRESS), Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES)), GenerationStep.Decoration.UNDERGROUND_DECORATION, TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.NETHER_FOSSIL, new NetherFossilStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_NETHER_FOSSIL), GenerationStep.Decoration.UNDERGROUND_DECORATION, TerrainAdjustment.BEARD_THIN), UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))));
        bootstapContext.register(BuiltinStructures.END_CITY, new EndCityStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_END_CITY), TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.BURIED_TREASURE, new BuriedTreasureStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_BURIED_TREASURE), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE)));
        bootstapContext.register(BuiltinStructures.BASTION_REMNANT, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_BASTION_REMNANT), TerrainAdjustment.NONE), holderGetter2.getOrThrow(BastionPieces.START), 6, ConstantHeight.of(VerticalAnchor.absolute(33)), false));
        bootstapContext.register(BuiltinStructures.VILLAGE_PLAINS, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_PLAINS), TerrainAdjustment.BEARD_THIN), holderGetter2.getOrThrow(PlainVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        bootstapContext.register(BuiltinStructures.VILLAGE_DESERT, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_DESERT), TerrainAdjustment.BEARD_THIN), holderGetter2.getOrThrow(DesertVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        bootstapContext.register(BuiltinStructures.VILLAGE_SAVANNA, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_SAVANNA), TerrainAdjustment.BEARD_THIN), holderGetter2.getOrThrow(SavannaVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        bootstapContext.register(BuiltinStructures.VILLAGE_SNOWY, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_SNOWY), TerrainAdjustment.BEARD_THIN), holderGetter2.getOrThrow(SnowyVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        bootstapContext.register(BuiltinStructures.VILLAGE_TAIGA, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_TAIGA), TerrainAdjustment.BEARD_THIN), holderGetter2.getOrThrow(TaigaVillagePools.START), 6, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_STANDARD, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_STANDARD), TerrainAdjustment.NONE), List.of(new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.UNDERGROUND, 1.0f, 0.2f, false, false, true, false, 0.5f), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.2f, false, false, true, false, 0.5f))));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_DESERT, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_DESERT), TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED, 0.0f, 0.0f, false, false, false, false, 1.0f)));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_JUNGLE, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_JUNGLE), TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.8f, true, true, false, false, 1.0f)));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_SWAMP, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_SWAMP), TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0f, 0.5f, false, true, false, false, 1.0f)));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_MOUNTAIN, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN), TerrainAdjustment.NONE), List.of(new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN, 1.0f, 0.2f, false, false, true, false, 0.5f), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5f, 0.2f, false, false, true, false, 0.5f))));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_OCEAN, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_OCEAN), TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0f, 0.8f, false, false, true, false, 1.0f)));
        bootstapContext.register(BuiltinStructures.RUINED_PORTAL_NETHER, new RuinedPortalStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_NETHER), TerrainAdjustment.NONE), new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_NETHER, 0.5f, 0.0f, false, false, false, true, 1.0f)));
        bootstapContext.register(BuiltinStructures.ANCIENT_CITY, new JigsawStructure(Structures.structure(holderGetter.getOrThrow(BiomeTags.HAS_ANCIENT_CITY), Arrays.stream(MobCategory.values()).collect(Collectors.toMap(mobCategory -> mobCategory, mobCategory -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create()))), GenerationStep.Decoration.UNDERGROUND_DECORATION, TerrainAdjustment.BEARD_BOX), holderGetter2.getOrThrow(AncientCityStructurePieces.START), Optional.of(new ResourceLocation("city_anchor")), 7, ConstantHeight.of(VerticalAnchor.absolute(-27)), false, Optional.empty(), 116));
    }
}

