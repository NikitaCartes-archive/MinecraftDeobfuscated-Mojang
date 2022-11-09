/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.Structure;
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
    public static final StructureType<BuriedTreasureStructure> BURIED_TREASURE = StructureType.register("buried_treasure", BuriedTreasureStructure.CODEC);
    public static final StructureType<DesertPyramidStructure> DESERT_PYRAMID = StructureType.register("desert_pyramid", DesertPyramidStructure.CODEC);
    public static final StructureType<EndCityStructure> END_CITY = StructureType.register("end_city", EndCityStructure.CODEC);
    public static final StructureType<NetherFortressStructure> FORTRESS = StructureType.register("fortress", NetherFortressStructure.CODEC);
    public static final StructureType<IglooStructure> IGLOO = StructureType.register("igloo", IglooStructure.CODEC);
    public static final StructureType<JigsawStructure> JIGSAW = StructureType.register("jigsaw", JigsawStructure.CODEC);
    public static final StructureType<JungleTempleStructure> JUNGLE_TEMPLE = StructureType.register("jungle_temple", JungleTempleStructure.CODEC);
    public static final StructureType<MineshaftStructure> MINESHAFT = StructureType.register("mineshaft", MineshaftStructure.CODEC);
    public static final StructureType<NetherFossilStructure> NETHER_FOSSIL = StructureType.register("nether_fossil", NetherFossilStructure.CODEC);
    public static final StructureType<OceanMonumentStructure> OCEAN_MONUMENT = StructureType.register("ocean_monument", OceanMonumentStructure.CODEC);
    public static final StructureType<OceanRuinStructure> OCEAN_RUIN = StructureType.register("ocean_ruin", OceanRuinStructure.CODEC);
    public static final StructureType<RuinedPortalStructure> RUINED_PORTAL = StructureType.register("ruined_portal", RuinedPortalStructure.CODEC);
    public static final StructureType<ShipwreckStructure> SHIPWRECK = StructureType.register("shipwreck", ShipwreckStructure.CODEC);
    public static final StructureType<StrongholdStructure> STRONGHOLD = StructureType.register("stronghold", StrongholdStructure.CODEC);
    public static final StructureType<SwampHutStructure> SWAMP_HUT = StructureType.register("swamp_hut", SwampHutStructure.CODEC);
    public static final StructureType<WoodlandMansionStructure> WOODLAND_MANSION = StructureType.register("woodland_mansion", WoodlandMansionStructure.CODEC);

    public Codec<S> codec();

    private static <S extends Structure> StructureType<S> register(String string, Codec<S> codec) {
        return Registry.register(BuiltInRegistries.STRUCTURE_TYPE, string, () -> codec);
    }
}

