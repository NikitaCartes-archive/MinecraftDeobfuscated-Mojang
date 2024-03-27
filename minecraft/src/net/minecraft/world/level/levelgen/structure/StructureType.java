package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalStructure;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckStructure;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdStructure;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutStructure;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionStructure;

public interface StructureType<S extends Structure> {
	StructureType<BuriedTreasureStructure> BURIED_TREASURE = register("buried_treasure", BuriedTreasureStructure.CODEC);
	StructureType<DesertPyramidStructure> DESERT_PYRAMID = register("desert_pyramid", DesertPyramidStructure.CODEC);
	StructureType<EndCityStructure> END_CITY = register("end_city", EndCityStructure.CODEC);
	StructureType<NetherFortressStructure> FORTRESS = register("fortress", NetherFortressStructure.CODEC);
	StructureType<IglooStructure> IGLOO = register("igloo", IglooStructure.CODEC);
	StructureType<JigsawStructure> JIGSAW = register("jigsaw", JigsawStructure.CODEC);
	StructureType<JungleTempleStructure> JUNGLE_TEMPLE = register("jungle_temple", JungleTempleStructure.CODEC);
	StructureType<MineshaftStructure> MINESHAFT = register("mineshaft", MineshaftStructure.CODEC);
	StructureType<NetherFossilStructure> NETHER_FOSSIL = register("nether_fossil", NetherFossilStructure.CODEC);
	StructureType<OceanMonumentStructure> OCEAN_MONUMENT = register("ocean_monument", OceanMonumentStructure.CODEC);
	StructureType<OceanRuinStructure> OCEAN_RUIN = register("ocean_ruin", OceanRuinStructure.CODEC);
	StructureType<RuinedPortalStructure> RUINED_PORTAL = register("ruined_portal", RuinedPortalStructure.CODEC);
	StructureType<ShipwreckStructure> SHIPWRECK = register("shipwreck", ShipwreckStructure.CODEC);
	StructureType<StrongholdStructure> STRONGHOLD = register("stronghold", StrongholdStructure.CODEC);
	StructureType<SwampHutStructure> SWAMP_HUT = register("swamp_hut", SwampHutStructure.CODEC);
	StructureType<WoodlandMansionStructure> WOODLAND_MANSION = register("woodland_mansion", WoodlandMansionStructure.CODEC);

	MapCodec<S> codec();

	private static <S extends Structure> StructureType<S> register(String string, MapCodec<S> mapCodec) {
		return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, string, () -> mapCodec);
	}
}
