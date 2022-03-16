/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Locale;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.structures.BuriedTreasurePieces;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;
import net.minecraft.world.level.levelgen.structure.structures.IglooPieces;
import net.minecraft.world.level.levelgen.structure.structures.JungleTemplePiece;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressPieces;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinPieces;
import net.minecraft.world.level.levelgen.structure.structures.RuinedPortalPiece;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckPieces;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraft.world.level.levelgen.structure.structures.SwampHutPiece;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public interface StructurePieceType {
    public static final StructurePieceType MINE_SHAFT_CORRIDOR = StructurePieceType.setPieceId(MineshaftPieces.MineShaftCorridor::new, "MSCorridor");
    public static final StructurePieceType MINE_SHAFT_CROSSING = StructurePieceType.setPieceId(MineshaftPieces.MineShaftCrossing::new, "MSCrossing");
    public static final StructurePieceType MINE_SHAFT_ROOM = StructurePieceType.setPieceId(MineshaftPieces.MineShaftRoom::new, "MSRoom");
    public static final StructurePieceType MINE_SHAFT_STAIRS = StructurePieceType.setPieceId(MineshaftPieces.MineShaftStairs::new, "MSStairs");
    public static final StructurePieceType NETHER_FORTRESS_BRIDGE_CROSSING = StructurePieceType.setPieceId(NetherFortressPieces.BridgeCrossing::new, "NeBCr");
    public static final StructurePieceType NETHER_FORTRESS_BRIDGE_END_FILLER = StructurePieceType.setPieceId(NetherFortressPieces.BridgeEndFiller::new, "NeBEF");
    public static final StructurePieceType NETHER_FORTRESS_BRIDGE_STRAIGHT = StructurePieceType.setPieceId(NetherFortressPieces.BridgeStraight::new, "NeBS");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS = StructurePieceType.setPieceId(NetherFortressPieces.CastleCorridorStairsPiece::new, "NeCCS");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY = StructurePieceType.setPieceId(NetherFortressPieces.CastleCorridorTBalconyPiece::new, "NeCTB");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_ENTRANCE = StructurePieceType.setPieceId(NetherFortressPieces.CastleEntrance::new, "NeCE");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING = StructurePieceType.setPieceId(NetherFortressPieces.CastleSmallCorridorCrossingPiece::new, "NeSCSC");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN = StructurePieceType.setPieceId(NetherFortressPieces.CastleSmallCorridorLeftTurnPiece::new, "NeSCLT");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR = StructurePieceType.setPieceId(NetherFortressPieces.CastleSmallCorridorPiece::new, "NeSC");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN = StructurePieceType.setPieceId(NetherFortressPieces.CastleSmallCorridorRightTurnPiece::new, "NeSCRT");
    public static final StructurePieceType NETHER_FORTRESS_CASTLE_STALK_ROOM = StructurePieceType.setPieceId(NetherFortressPieces.CastleStalkRoom::new, "NeCSR");
    public static final StructurePieceType NETHER_FORTRESS_MONSTER_THRONE = StructurePieceType.setPieceId(NetherFortressPieces.MonsterThrone::new, "NeMT");
    public static final StructurePieceType NETHER_FORTRESS_ROOM_CROSSING = StructurePieceType.setPieceId(NetherFortressPieces.RoomCrossing::new, "NeRC");
    public static final StructurePieceType NETHER_FORTRESS_STAIRS_ROOM = StructurePieceType.setPieceId(NetherFortressPieces.StairsRoom::new, "NeSR");
    public static final StructurePieceType NETHER_FORTRESS_START = StructurePieceType.setPieceId(NetherFortressPieces.StartPiece::new, "NeStart");
    public static final StructurePieceType STRONGHOLD_CHEST_CORRIDOR = StructurePieceType.setPieceId(StrongholdPieces.ChestCorridor::new, "SHCC");
    public static final StructurePieceType STRONGHOLD_FILLER_CORRIDOR = StructurePieceType.setPieceId(StrongholdPieces.FillerCorridor::new, "SHFC");
    public static final StructurePieceType STRONGHOLD_FIVE_CROSSING = StructurePieceType.setPieceId(StrongholdPieces.FiveCrossing::new, "SH5C");
    public static final StructurePieceType STRONGHOLD_LEFT_TURN = StructurePieceType.setPieceId(StrongholdPieces.LeftTurn::new, "SHLT");
    public static final StructurePieceType STRONGHOLD_LIBRARY = StructurePieceType.setPieceId(StrongholdPieces.Library::new, "SHLi");
    public static final StructurePieceType STRONGHOLD_PORTAL_ROOM = StructurePieceType.setPieceId(StrongholdPieces.PortalRoom::new, "SHPR");
    public static final StructurePieceType STRONGHOLD_PRISON_HALL = StructurePieceType.setPieceId(StrongholdPieces.PrisonHall::new, "SHPH");
    public static final StructurePieceType STRONGHOLD_RIGHT_TURN = StructurePieceType.setPieceId(StrongholdPieces.RightTurn::new, "SHRT");
    public static final StructurePieceType STRONGHOLD_ROOM_CROSSING = StructurePieceType.setPieceId(StrongholdPieces.RoomCrossing::new, "SHRC");
    public static final StructurePieceType STRONGHOLD_STAIRS_DOWN = StructurePieceType.setPieceId(StrongholdPieces.StairsDown::new, "SHSD");
    public static final StructurePieceType STRONGHOLD_START = StructurePieceType.setPieceId(StrongholdPieces.StartPiece::new, "SHStart");
    public static final StructurePieceType STRONGHOLD_STRAIGHT = StructurePieceType.setPieceId(StrongholdPieces.Straight::new, "SHS");
    public static final StructurePieceType STRONGHOLD_STRAIGHT_STAIRS_DOWN = StructurePieceType.setPieceId(StrongholdPieces.StraightStairsDown::new, "SHSSD");
    public static final StructurePieceType JUNGLE_PYRAMID_PIECE = StructurePieceType.setPieceId(JungleTemplePiece::new, "TeJP");
    public static final StructurePieceType OCEAN_RUIN = StructurePieceType.setTemplatePieceId(OceanRuinPieces.OceanRuinPiece::new, "ORP");
    public static final StructurePieceType IGLOO = StructurePieceType.setTemplatePieceId(IglooPieces.IglooPiece::new, "Iglu");
    public static final StructurePieceType RUINED_PORTAL = StructurePieceType.setTemplatePieceId(RuinedPortalPiece::new, "RUPO");
    public static final StructurePieceType SWAMPLAND_HUT = StructurePieceType.setPieceId(SwampHutPiece::new, "TeSH");
    public static final StructurePieceType DESERT_PYRAMID_PIECE = StructurePieceType.setPieceId(DesertPyramidPiece::new, "TeDP");
    public static final StructurePieceType OCEAN_MONUMENT_BUILDING = StructurePieceType.setPieceId(OceanMonumentPieces.MonumentBuilding::new, "OMB");
    public static final StructurePieceType OCEAN_MONUMENT_CORE_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentCoreRoom::new, "OMCR");
    public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_X_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentDoubleXRoom::new, "OMDXR");
    public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_XY_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentDoubleXYRoom::new, "OMDXYR");
    public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_Y_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentDoubleYRoom::new, "OMDYR");
    public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_YZ_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentDoubleYZRoom::new, "OMDYZR");
    public static final StructurePieceType OCEAN_MONUMENT_DOUBLE_Z_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentDoubleZRoom::new, "OMDZR");
    public static final StructurePieceType OCEAN_MONUMENT_ENTRY_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentEntryRoom::new, "OMEntry");
    public static final StructurePieceType OCEAN_MONUMENT_PENTHOUSE = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentPenthouse::new, "OMPenthouse");
    public static final StructurePieceType OCEAN_MONUMENT_SIMPLE_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentSimpleRoom::new, "OMSimple");
    public static final StructurePieceType OCEAN_MONUMENT_SIMPLE_TOP_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentSimpleTopRoom::new, "OMSimpleT");
    public static final StructurePieceType OCEAN_MONUMENT_WING_ROOM = StructurePieceType.setPieceId(OceanMonumentPieces.OceanMonumentWingRoom::new, "OMWR");
    public static final StructurePieceType END_CITY_PIECE = StructurePieceType.setTemplatePieceId(EndCityPieces.EndCityPiece::new, "ECP");
    public static final StructurePieceType WOODLAND_MANSION_PIECE = StructurePieceType.setTemplatePieceId(WoodlandMansionPieces.WoodlandMansionPiece::new, "WMP");
    public static final StructurePieceType BURIED_TREASURE_PIECE = StructurePieceType.setPieceId(BuriedTreasurePieces.BuriedTreasurePiece::new, "BTP");
    public static final StructurePieceType SHIPWRECK_PIECE = StructurePieceType.setTemplatePieceId(ShipwreckPieces.ShipwreckPiece::new, "Shipwreck");
    public static final StructurePieceType NETHER_FOSSIL = StructurePieceType.setTemplatePieceId(NetherFossilPieces.NetherFossilPiece::new, "NeFos");
    public static final StructurePieceType JIGSAW = StructurePieceType.setFullContextPieceId(PoolElementStructurePiece::new, "jigsaw");

    public StructurePiece load(StructurePieceSerializationContext var1, CompoundTag var2);

    private static StructurePieceType setFullContextPieceId(StructurePieceType structurePieceType, String string) {
        return Registry.register(Registry.STRUCTURE_PIECE, string.toLowerCase(Locale.ROOT), structurePieceType);
    }

    private static StructurePieceType setPieceId(ContextlessType contextlessType, String string) {
        return StructurePieceType.setFullContextPieceId(contextlessType, string);
    }

    private static StructurePieceType setTemplatePieceId(StructureTemplateType structureTemplateType, String string) {
        return StructurePieceType.setFullContextPieceId(structureTemplateType, string);
    }

    public static interface ContextlessType
    extends StructurePieceType {
        public StructurePiece load(CompoundTag var1);

        @Override
        default public StructurePiece load(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            return this.load(compoundTag);
        }
    }

    public static interface StructureTemplateType
    extends StructurePieceType {
        public StructurePiece load(StructureTemplateManager var1, CompoundTag var2);

        @Override
        default public StructurePiece load(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            return this.load(structurePieceSerializationContext.structureTemplateManager(), compoundTag);
        }
    }
}

