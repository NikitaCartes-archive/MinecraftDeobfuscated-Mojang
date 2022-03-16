package net.minecraft.data.worldgen;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
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
	public static final Holder<Structure> PILLAGER_OUTPOST = register(
		BuiltinStructures.PILLAGER_OUTPOST,
		new JigsawStructure(
			biomes(BiomeTags.HAS_PILLAGER_OUTPOST),
			Map.of(
				MobCategory.MONSTER,
				new StructureSpawnOverride(
					StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.PILLAGER, 1, 1, 1))
				)
			),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			true,
			PillagerOutpostPools.START,
			7,
			ConstantHeight.of(VerticalAnchor.absolute(0)),
			true,
			Heightmap.Types.WORLD_SURFACE_WG
		)
	);
	public static final Holder<Structure> MINESHAFT = register(
		BuiltinStructures.MINESHAFT,
		new MineshaftStructure(biomes(BiomeTags.HAS_MINESHAFT), Map.of(), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, false, MineshaftStructure.Type.NORMAL)
	);
	public static final Holder<Structure> MINESHAFT_MESA = register(
		BuiltinStructures.MINESHAFT_MESA,
		new MineshaftStructure(biomes(BiomeTags.HAS_MINESHAFT_MESA), Map.of(), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, false, MineshaftStructure.Type.MESA)
	);
	public static final Holder<Structure> WOODLAND_MANSION = register(
		BuiltinStructures.WOODLAND_MANSION,
		new WoodlandMansionStructure(biomes(BiomeTags.HAS_WOODLAND_MANSION), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false)
	);
	public static final Holder<Structure> JUNGLE_TEMPLE = register(
		BuiltinStructures.JUNGLE_TEMPLE,
		new JungleTempleStructure(biomes(BiomeTags.HAS_JUNGLE_TEMPLE), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false)
	);
	public static final Holder<Structure> DESERT_PYRAMID = register(
		BuiltinStructures.DESERT_PYRAMID,
		new DesertPyramidStructure(biomes(BiomeTags.HAS_DESERT_PYRAMID), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false)
	);
	public static final Holder<Structure> IGLOO = register(
		BuiltinStructures.IGLOO, new IglooStructure(biomes(BiomeTags.HAS_IGLOO), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false)
	);
	public static final Holder<Structure> SHIPWRECK = register(
		BuiltinStructures.SHIPWRECK, new ShipwreckStructure(biomes(BiomeTags.HAS_SHIPWRECK), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false, false)
	);
	public static final Holder<Structure> SHIPWRECK_BEACHED = register(
		BuiltinStructures.SHIPWRECK_BEACHED,
		new ShipwreckStructure(biomes(BiomeTags.HAS_SHIPWRECK_BEACHED), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false, true)
	);
	public static final Holder<Structure> SWAMP_HUT = register(
		BuiltinStructures.SWAMP_HUT,
		new SwampHutStructure(
			biomes(BiomeTags.HAS_SWAMP_HUT),
			Map.of(
				MobCategory.MONSTER,
				new StructureSpawnOverride(
					StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.WITCH, 1, 1, 1))
				),
				MobCategory.CREATURE,
				new StructureSpawnOverride(
					StructureSpawnOverride.BoundingBoxType.PIECE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.CAT, 1, 1, 1))
				)
			),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false
		)
	);
	public static final Holder<Structure> STRONGHOLD = register(
		BuiltinStructures.STRONGHOLD, new StrongholdStructure(biomes(BiomeTags.HAS_STRONGHOLD), Map.of(), GenerationStep.Decoration.STRONGHOLDS, true)
	);
	public static final Holder<Structure> OCEAN_MONUMENT = register(
		BuiltinStructures.OCEAN_MONUMENT,
		new OceanMonumentStructure(
			biomes(BiomeTags.HAS_OCEAN_MONUMENT),
			Map.of(
				MobCategory.MONSTER,
				new StructureSpawnOverride(
					StructureSpawnOverride.BoundingBoxType.STRUCTURE, WeightedRandomList.create(new MobSpawnSettings.SpawnerData(EntityType.GUARDIAN, 1, 2, 4))
				),
				MobCategory.UNDERGROUND_WATER_CREATURE,
				new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST),
				MobCategory.AXOLOTLS,
				new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.STRUCTURE, MobSpawnSettings.EMPTY_MOB_LIST)
			),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false
		)
	);
	public static final Holder<Structure> OCEAN_RUIN_COLD = register(
		BuiltinStructures.OCEAN_RUIN_COLD,
		new OceanRuinStructure(
			biomes(BiomeTags.HAS_OCEAN_RUIN_COLD), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false, OceanRuinStructure.Type.COLD, 0.3F, 0.9F
		)
	);
	public static final Holder<Structure> OCEAN_RUIN_WARM = register(
		BuiltinStructures.OCEAN_RUIN_WARM,
		new OceanRuinStructure(
			biomes(BiomeTags.HAS_OCEAN_RUIN_WARM), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false, OceanRuinStructure.Type.WARM, 0.3F, 0.9F
		)
	);
	public static final Holder<Structure> FORTRESS = register(
		BuiltinStructures.FORTRESS,
		new NetherFortressStructure(
			biomes(BiomeTags.HAS_NETHER_FORTRESS),
			Map.of(MobCategory.MONSTER, new StructureSpawnOverride(StructureSpawnOverride.BoundingBoxType.PIECE, NetherFortressStructure.FORTRESS_ENEMIES)),
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			false
		)
	);
	public static final Holder<Structure> NETHER_FOSSIL = register(
		BuiltinStructures.NETHER_FOSSIL,
		new NetherFossilStructure(
			biomes(BiomeTags.HAS_NETHER_FOSSIL),
			Map.of(),
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			true,
			UniformHeight.of(VerticalAnchor.absolute(32), VerticalAnchor.belowTop(2))
		)
	);
	public static final Holder<Structure> END_CITY = register(
		BuiltinStructures.END_CITY, new EndCityStructure(biomes(BiomeTags.HAS_END_CITY), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, false)
	);
	public static final Holder<Structure> BURIED_TREASURE = register(
		BuiltinStructures.BURIED_TREASURE,
		new BuriedTreasureStructure(biomes(BiomeTags.HAS_BURIED_TREASURE), Map.of(), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, false)
	);
	public static final Holder<Structure> BASTION_REMNANT = register(
		BuiltinStructures.BASTION_REMNANT,
		new JigsawStructure(
			biomes(BiomeTags.HAS_BASTION_REMNANT),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			BastionPieces.START,
			6,
			ConstantHeight.of(VerticalAnchor.absolute(33)),
			false
		)
	);
	public static final Holder<Structure> VILLAGE_PLAINS = register(
		BuiltinStructures.VILLAGE_PLAINS,
		new JigsawStructure(
			biomes(BiomeTags.HAS_VILLAGE_PLAINS),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			true,
			PlainVillagePools.START,
			6,
			ConstantHeight.of(VerticalAnchor.absolute(0)),
			true,
			Heightmap.Types.WORLD_SURFACE_WG
		)
	);
	public static final Holder<Structure> VILLAGE_DESERT = register(
		BuiltinStructures.VILLAGE_DESERT,
		new JigsawStructure(
			biomes(BiomeTags.HAS_VILLAGE_DESERT),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			true,
			DesertVillagePools.START,
			6,
			ConstantHeight.of(VerticalAnchor.absolute(0)),
			true,
			Heightmap.Types.WORLD_SURFACE_WG
		)
	);
	public static final Holder<Structure> VILLAGE_SAVANNA = register(
		BuiltinStructures.VILLAGE_SAVANNA,
		new JigsawStructure(
			biomes(BiomeTags.HAS_VILLAGE_SAVANNA),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			true,
			SavannaVillagePools.START,
			6,
			ConstantHeight.of(VerticalAnchor.absolute(0)),
			true,
			Heightmap.Types.WORLD_SURFACE_WG
		)
	);
	public static final Holder<Structure> VILLAGE_SNOWY = register(
		BuiltinStructures.VILLAGE_SNOWY,
		new JigsawStructure(
			biomes(BiomeTags.HAS_VILLAGE_SNOWY),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			true,
			SnowyVillagePools.START,
			6,
			ConstantHeight.of(VerticalAnchor.absolute(0)),
			true,
			Heightmap.Types.WORLD_SURFACE_WG
		)
	);
	public static final Holder<Structure> VILLAGE_TAIGA = register(
		BuiltinStructures.VILLAGE_TAIGA,
		new JigsawStructure(
			biomes(BiomeTags.HAS_VILLAGE_TAIGA),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			true,
			TaigaVillagePools.START,
			6,
			ConstantHeight.of(VerticalAnchor.absolute(0)),
			true,
			Heightmap.Types.WORLD_SURFACE_WG
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_STANDARD = register(
		BuiltinStructures.RUINED_PORTAL_STANDARD,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_STANDARD),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			List.of(
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.UNDERGROUND, 1.0F, 0.2F, false, false, true, false, 0.5F),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
			)
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_DESERT = register(
		BuiltinStructures.RUINED_PORTAL_DESERT,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_DESERT),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.PARTLY_BURIED, 0.0F, 0.0F, false, false, false, false, 1.0F)
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_JUNGLE = register(
		BuiltinStructures.RUINED_PORTAL_JUNGLE,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_JUNGLE),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.8F, true, true, false, false, 1.0F)
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_SWAMP = register(
		BuiltinStructures.RUINED_PORTAL_SWAMP,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_SWAMP),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.5F, false, true, false, false, 1.0F)
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_MOUNTAIN = register(
		BuiltinStructures.RUINED_PORTAL_MOUNTAIN,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_MOUNTAIN),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			List.of(
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_MOUNTAIN, 1.0F, 0.2F, false, false, true, false, 0.5F),
				new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_LAND_SURFACE, 0.5F, 0.2F, false, false, true, false, 0.5F)
			)
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_OCEAN = register(
		BuiltinStructures.RUINED_PORTAL_OCEAN,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_OCEAN),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.ON_OCEAN_FLOOR, 0.0F, 0.8F, false, false, true, false, 1.0F)
		)
	);
	public static final Holder<Structure> RUINED_PORTAL_NETHER = register(
		BuiltinStructures.RUINED_PORTAL_NETHER,
		new RuinedPortalStructure(
			biomes(BiomeTags.HAS_RUINED_PORTAL_NETHER),
			Map.of(),
			GenerationStep.Decoration.SURFACE_STRUCTURES,
			false,
			new RuinedPortalStructure.Setup(RuinedPortalPiece.VerticalPlacement.IN_NETHER, 0.5F, 0.0F, false, false, false, true, 1.0F)
		)
	);

	public static Holder<? extends Structure> bootstrap() {
		return MINESHAFT;
	}

	private static Holder<Structure> register(ResourceKey<Structure> resourceKey, Structure structure) {
		return BuiltinRegistries.register(BuiltinRegistries.STRUCTURES, resourceKey, structure);
	}

	private static HolderSet<Biome> biomes(TagKey<Biome> tagKey) {
		return BuiltinRegistries.BIOME.getOrCreateTag(tagKey);
	}
}
