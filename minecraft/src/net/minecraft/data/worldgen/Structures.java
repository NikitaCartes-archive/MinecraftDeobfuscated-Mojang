package net.minecraft.data.worldgen;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
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
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
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
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

public class Structures {
	public static void bootstrap(BootstrapContext<Structure> bootstrapContext) {
		HolderGetter<Biome> holderGetter = bootstrapContext.lookup(Registries.BIOME);
		HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
		bootstrapContext.register(
			BuiltinStructures.PILLAGER_OUTPOST,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_PILLAGER_OUTPOST))
					.spawnOverrides(
						Map.of(
							MobCategory.MONSTER,
							new StructureSpawnOverride(
								StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1))
							)
						)
					)
					.terrainAdapation(TerrainAdjustment.BEARD_THIN)
					.build(),
				holderGetter2.getOrThrow(PillagerOutpostPools.START),
				7,
				ConstantHeight.of(VerticalAnchor.absolute(0)),
				true,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.MINESHAFT,
			new MineshaftStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_MINESHAFT))
					.generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
					.build(),
				MineshaftStructure.Type.NORMAL
			)
		);
		bootstrapContext.register(
			BuiltinStructures.MINESHAFT_MESA,
			new MineshaftStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_MINESHAFT_MESA))
					.generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
					.build(),
				MineshaftStructure.Type.MESA
			)
		);
		bootstrapContext.register(
			BuiltinStructures.WOODLAND_MANSION, new WoodlandMansionStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_WOODLAND_MANSION)))
		);
		bootstrapContext.register(
			BuiltinStructures.JUNGLE_TEMPLE, new JungleTempleStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_JUNGLE_TEMPLE)))
		);
		bootstrapContext.register(
			BuiltinStructures.DESERT_PYRAMID, new DesertPyramidStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_DESERT_PYRAMID)))
		);
		bootstrapContext.register(BuiltinStructures.IGLOO, new IglooStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_IGLOO))));
		bootstrapContext.register(
			BuiltinStructures.SHIPWRECK, new ShipwreckStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_SHIPWRECK)), false)
		);
		bootstrapContext.register(
			BuiltinStructures.SHIPWRECK_BEACHED, new ShipwreckStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_SHIPWRECK_BEACHED)), true)
		);
		bootstrapContext.register(
			BuiltinStructures.SWAMP_HUT,
			new SwampHutStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_SWAMP_HUT))
					.spawnOverrides(
						Map.of(
							MobCategory.MONSTER,
							new StructureSpawnOverride(
								StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1))
							),
							MobCategory.CREATURE,
							new StructureSpawnOverride(
								StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1))
							)
						)
					)
					.build()
			)
		);
		bootstrapContext.register(
			BuiltinStructures.STRONGHOLD,
			new StrongholdStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_STRONGHOLD)).terrainAdapation(TerrainAdjustment.BURY).build()
			)
		);
		bootstrapContext.register(
			BuiltinStructures.OCEAN_MONUMENT,
			new OceanMonumentStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_OCEAN_MONUMENT))
					.spawnOverrides(
						Map.of(
							MobCategory.MONSTER,
							new StructureSpawnOverride(
								StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4))
							),
							MobCategory.UNDERGROUND_WATER_CREATURE,
							new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST),
							MobCategory.AXOLOTLS,
							new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST)
						)
					)
					.build()
			)
		);
		bootstrapContext.register(
			BuiltinStructures.OCEAN_RUIN_COLD,
			new OceanRuinStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_COLD)), OceanRuinStructure.Type.COLD, 0.3F, 0.9F)
		);
		bootstrapContext.register(
			BuiltinStructures.OCEAN_RUIN_WARM,
			new OceanRuinStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_OCEAN_RUIN_WARM)), OceanRuinStructure.Type.WARM, 0.3F, 0.9F)
		);
		bootstrapContext.register(
			BuiltinStructures.FORTRESS,
			new NetherFortressStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_NETHER_FORTRESS))
					.spawnOverrides(
						Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES))
					)
					.generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
					.build()
			)
		);
		bootstrapContext.register(
			BuiltinStructures.NETHER_FOSSIL,
			new NetherFossilStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_NETHER_FOSSIL))
					.generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
					.terrainAdapation(TerrainAdjustment.BEARD_THIN)
					.build(),
				UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))
			)
		);
		bootstrapContext.register(BuiltinStructures.END_CITY, new EndCityStructure(new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_END_CITY))));
		bootstrapContext.register(
			BuiltinStructures.BURIED_TREASURE,
			new BuriedTreasureStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_BURIED_TREASURE))
					.generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
					.build()
			)
		);
		bootstrapContext.register(
			BuiltinStructures.BASTION_REMNANT,
			new JigsawStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_BASTION_REMNANT)),
				holderGetter2.getOrThrow(BastionPieces.START),
				6,
				ConstantHeight.of(VerticalAnchor.absolute(33)),
				false
			)
		);
		bootstrapContext.register(
			BuiltinStructures.VILLAGE_PLAINS,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_PLAINS)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
				holderGetter2.getOrThrow(PlainVillagePools.START),
				6,
				ConstantHeight.of(VerticalAnchor.absolute(0)),
				true,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.VILLAGE_DESERT,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_DESERT)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
				holderGetter2.getOrThrow(DesertVillagePools.START),
				6,
				ConstantHeight.of(VerticalAnchor.absolute(0)),
				true,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.VILLAGE_SAVANNA,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_SAVANNA)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
				holderGetter2.getOrThrow(SavannaVillagePools.START),
				6,
				ConstantHeight.of(VerticalAnchor.absolute(0)),
				true,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.VILLAGE_SNOWY,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_SNOWY)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
				holderGetter2.getOrThrow(SnowyVillagePools.START),
				6,
				ConstantHeight.of(VerticalAnchor.absolute(0)),
				true,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.VILLAGE_TAIGA,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_VILLAGE_TAIGA)).terrainAdapation(TerrainAdjustment.BEARD_THIN).build(),
				holderGetter2.getOrThrow(TaigaVillagePools.START),
				6,
				ConstantHeight.of(VerticalAnchor.absolute(0)),
				true,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_STANDARD,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_STANDARD)),
				List.of(
					new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.UNDERGROUND, 1.0F, 0.2F, false, false, true, false, 0.5F),
					new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
				)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_DESERT,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_DESERT)),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED, 0.0F, 0.0F, false, false, false, false, 1.0F)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_JUNGLE,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_JUNGLE)),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.8F, true, true, false, false, 1.0F)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_SWAMP,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_SWAMP)),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.5F, false, true, false, false, 1.0F)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_MOUNTAIN,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN)),
				List.of(
					new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN, 1.0F, 0.2F, false, false, true, false, 0.5F),
					new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
				)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_OCEAN,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_OCEAN)),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.8F, false, false, true, false, 1.0F)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.RUINED_PORTAL_NETHER,
			new RuinedPortalStructure(
				new Structure.StructureSettings(holderGetter.getOrThrow(BiomeTags.HAS_RUINED_PORTAL_NETHER)),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_NETHER, 0.5F, 0.0F, false, false, false, true, 1.0F)
			)
		);
		bootstrapContext.register(
			BuiltinStructures.ANCIENT_CITY,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_ANCIENT_CITY))
					.spawnOverrides(
						(Map<MobCategory, StructureSpawnOverride>)Arrays.stream(MobCategory.values())
							.collect(
								Collectors.toMap(
									mobCategory -> mobCategory, mobCategory -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create())
								)
							)
					)
					.generationStep(GenerationStep.Decoration.UNDERGROUND_DECORATION)
					.terrainAdapation(TerrainAdjustment.BEARD_BOX)
					.build(),
				holderGetter2.getOrThrow(AncientCityStructurePieces.START),
				Optional.of(ResourceLocation.withDefaultNamespace("city_anchor")),
				7,
				ConstantHeight.of(VerticalAnchor.absolute(-27)),
				false,
				Optional.empty(),
				116,
				List.of(),
				JigsawStructure.DEFAULT_DIMENSION_PADDING,
				JigsawStructure.DEFAULT_LIQUID_SETTINGS
			)
		);
		bootstrapContext.register(
			BuiltinStructures.TRAIL_RUINS,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_TRAIL_RUINS))
					.generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
					.terrainAdapation(TerrainAdjustment.BURY)
					.build(),
				holderGetter2.getOrThrow(TrailRuinsStructurePools.START),
				7,
				ConstantHeight.of(VerticalAnchor.absolute(-15)),
				false,
				Heightmap.Types.WORLD_SURFACE_WG
			)
		);
		bootstrapContext.register(
			BuiltinStructures.TRIAL_CHAMBERS,
			new JigsawStructure(
				new Structure.StructureSettings.Builder(holderGetter.getOrThrow(BiomeTags.HAS_TRIAL_CHAMBERS))
					.generationStep(GenerationStep.Decoration.UNDERGROUND_STRUCTURES)
					.terrainAdapation(TerrainAdjustment.ENCAPSULATE)
					.spawnOverrides(
						(Map<MobCategory, StructureSpawnOverride>)Arrays.stream(MobCategory.values())
							.collect(
								Collectors.toMap(
									mobCategory -> mobCategory, mobCategory -> new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create())
								)
							)
					)
					.build(),
				holderGetter2.getOrThrow(TrialChambersStructurePools.START),
				Optional.empty(),
				20,
				UniformHeight.of(VerticalAnchor.absolute(-40), VerticalAnchor.absolute(-20)),
				false,
				Optional.empty(),
				116,
				TrialChambersStructurePools.ALIAS_BINDINGS,
				new DimensionPadding(10),
				LiquidSettings.IGNORE_WATERLOGGING
			)
		);
	}
}
