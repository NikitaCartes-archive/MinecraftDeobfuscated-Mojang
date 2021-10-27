/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.Nullable;

public class NetherBridgePieces {
    private static final int MAX_DEPTH = 30;
    private static final int LOWEST_Y_POSITION = 10;
    public static final int MAGIC_START_Y = 64;
    static final PieceWeight[] BRIDGE_PIECE_WEIGHTS = new PieceWeight[]{new PieceWeight(BridgeStraight.class, 30, 0, true), new PieceWeight(BridgeCrossing.class, 10, 4), new PieceWeight(RoomCrossing.class, 10, 4), new PieceWeight(StairsRoom.class, 10, 3), new PieceWeight(MonsterThrone.class, 5, 2), new PieceWeight(CastleEntrance.class, 5, 1)};
    static final PieceWeight[] CASTLE_PIECE_WEIGHTS = new PieceWeight[]{new PieceWeight(CastleSmallCorridorPiece.class, 25, 0, true), new PieceWeight(CastleSmallCorridorCrossingPiece.class, 15, 5), new PieceWeight(CastleSmallCorridorRightTurnPiece.class, 5, 10), new PieceWeight(CastleSmallCorridorLeftTurnPiece.class, 5, 10), new PieceWeight(CastleCorridorStairsPiece.class, 10, 3, true), new PieceWeight(CastleCorridorTBalconyPiece.class, 7, 2), new PieceWeight(CastleStalkRoom.class, 5, 2)};

    static NetherBridgePiece findAndCreateBridgePieceFactory(PieceWeight pieceWeight, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
        Class<? extends NetherBridgePiece> class_ = pieceWeight.pieceClass;
        NetherBridgePiece netherBridgePiece = null;
        if (class_ == BridgeStraight.class) {
            netherBridgePiece = BridgeStraight.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
        } else if (class_ == BridgeCrossing.class) {
            netherBridgePiece = BridgeCrossing.createPiece(structurePieceAccessor, i, j, k, direction, l);
        } else if (class_ == RoomCrossing.class) {
            netherBridgePiece = RoomCrossing.createPiece(structurePieceAccessor, i, j, k, direction, l);
        } else if (class_ == StairsRoom.class) {
            netherBridgePiece = StairsRoom.createPiece(structurePieceAccessor, i, j, k, l, direction);
        } else if (class_ == MonsterThrone.class) {
            netherBridgePiece = MonsterThrone.createPiece(structurePieceAccessor, i, j, k, l, direction);
        } else if (class_ == CastleEntrance.class) {
            netherBridgePiece = CastleEntrance.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
        } else if (class_ == CastleSmallCorridorPiece.class) {
            netherBridgePiece = CastleSmallCorridorPiece.createPiece(structurePieceAccessor, i, j, k, direction, l);
        } else if (class_ == CastleSmallCorridorRightTurnPiece.class) {
            netherBridgePiece = CastleSmallCorridorRightTurnPiece.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
        } else if (class_ == CastleSmallCorridorLeftTurnPiece.class) {
            netherBridgePiece = CastleSmallCorridorLeftTurnPiece.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
        } else if (class_ == CastleCorridorStairsPiece.class) {
            netherBridgePiece = CastleCorridorStairsPiece.createPiece(structurePieceAccessor, i, j, k, direction, l);
        } else if (class_ == CastleCorridorTBalconyPiece.class) {
            netherBridgePiece = CastleCorridorTBalconyPiece.createPiece(structurePieceAccessor, i, j, k, direction, l);
        } else if (class_ == CastleSmallCorridorCrossingPiece.class) {
            netherBridgePiece = CastleSmallCorridorCrossingPiece.createPiece(structurePieceAccessor, i, j, k, direction, l);
        } else if (class_ == CastleStalkRoom.class) {
            netherBridgePiece = CastleStalkRoom.createPiece(structurePieceAccessor, i, j, k, direction, l);
        }
        return netherBridgePiece;
    }

    static class PieceWeight {
        public final Class<? extends NetherBridgePiece> pieceClass;
        public final int weight;
        public int placeCount;
        public final int maxPlaceCount;
        public final boolean allowInRow;

        public PieceWeight(Class<? extends NetherBridgePiece> class_, int i, int j, boolean bl) {
            this.pieceClass = class_;
            this.weight = i;
            this.maxPlaceCount = j;
            this.allowInRow = bl;
        }

        public PieceWeight(Class<? extends NetherBridgePiece> class_, int i, int j) {
            this(class_, i, j, false);
        }

        public boolean doPlace(int i) {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }

        public boolean isValid() {
            return this.maxPlaceCount == 0 || this.placeCount < this.maxPlaceCount;
        }
    }

    public static class BridgeStraight
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 10;
        private static final int DEPTH = 19;

        public BridgeStraight(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, i, boundingBox);
            this.setOrientation(direction);
        }

        public BridgeStraight(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_STRAIGHT, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 1, 3, false);
        }

        public static BridgeStraight createPiece(StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -3, 0, 5, 10, 19, direction);
            if (!BridgeStraight.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new BridgeStraight(l, random, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 4, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 5, 0, 3, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 0, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 5, 0, 4, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 13, 4, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 15, 4, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 2; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, boundingBox);
                }
            }
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            BlockState blockState2 = (BlockState)blockState.setValue(FenceBlock.EAST, true);
            BlockState blockState3 = (BlockState)blockState.setValue(FenceBlock.WEST, true);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 1, 0, 4, 1, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 4, 0, 4, 4, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 14, 0, 4, 14, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 1, 17, 0, 4, 17, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 1, 4, 4, 1, blockState3, blockState3, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 4, 4, 4, 4, blockState3, blockState3, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 14, 4, 4, 14, blockState3, blockState3, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 1, 17, 4, 4, 17, blockState3, blockState3, false);
        }
    }

    public static class BridgeCrossing
    extends NetherBridgePiece {
        private static final int WIDTH = 19;
        private static final int HEIGHT = 10;
        private static final int DEPTH = 19;

        public BridgeCrossing(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, i, boundingBox);
            this.setOrientation(direction);
        }

        protected BridgeCrossing(int i, int j, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, 0, StructurePiece.makeBoundingBox(i, 64, j, direction, 19, 10, 19));
            this.setOrientation(direction);
        }

        protected BridgeCrossing(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }

        public BridgeCrossing(CompoundTag compoundTag) {
            this(StructurePieceType.NETHER_FORTRESS_BRIDGE_CROSSING, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 8, 3, false);
            this.generateChildLeft((StartPiece)structurePiece, structurePieceAccessor, random, 3, 8, false);
            this.generateChildRight((StartPiece)structurePiece, structurePieceAccessor, random, 3, 8, false);
        }

        public static BridgeCrossing createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -8, -3, 0, 19, 10, 19, direction);
            if (!BridgeCrossing.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new BridgeCrossing(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int j;
            int i;
            this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 11, 4, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 7, 18, 4, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 0, 10, 7, 18, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 8, 18, 7, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 7, 5, 0, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 7, 5, 11, 7, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 11, 5, 0, 11, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 11, 5, 11, 11, 5, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 7, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 11, 5, 7, 18, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 11, 7, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 11, 5, 11, 18, 5, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 7, 2, 0, 11, 2, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 7, 2, 13, 11, 2, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 7, 0, 0, 11, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 7, 0, 15, 11, 1, 18, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (i = 7; i <= 11; ++i) {
                for (j = 0; j <= 2; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 18 - j, boundingBox);
                }
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 7, 5, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 13, 2, 7, 18, 2, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 7, 3, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 15, 0, 7, 18, 1, 11, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (i = 0; i <= 2; ++i) {
                for (j = 7; j <= 11; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 18 - i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class RoomCrossing
    extends NetherBridgePiece {
        private static final int WIDTH = 7;
        private static final int HEIGHT = 9;
        private static final int DEPTH = 7;

        public RoomCrossing(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, i, boundingBox);
            this.setOrientation(direction);
        }

        public RoomCrossing(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_ROOM_CROSSING, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 2, 0, false);
            this.generateChildLeft((StartPiece)structurePiece, structurePieceAccessor, random, 0, 2, false);
            this.generateChildRight((StartPiece)structurePiece, structurePieceAccessor, random, 0, 2, false);
        }

        public static RoomCrossing createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -2, 0, 0, 7, 9, 7, direction);
            if (!RoomCrossing.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new RoomCrossing(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 6, 7, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 6, 1, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 6, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 5, 0, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 2, 0, 6, 6, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 2, 5, 6, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            this.generateBox(worldGenLevel, boundingBox, 2, 6, 0, 4, 6, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 2, 6, 6, 4, 6, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 6, 4, 5, 6, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 2, 0, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 2, 0, 5, 4, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 6, 2, 6, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 5, 2, 6, 5, 4, blockState2, blockState2, false);
            for (int i = 0; i <= 6; ++i) {
                for (int j = 0; j <= 6; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class StairsRoom
    extends NetherBridgePiece {
        private static final int WIDTH = 7;
        private static final int HEIGHT = 11;
        private static final int DEPTH = 7;

        public StairsRoom(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, i, boundingBox);
            this.setOrientation(direction);
        }

        public StairsRoom(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_STAIRS_ROOM, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildRight((StartPiece)structurePiece, structurePieceAccessor, random, 6, 2, false);
        }

        public static StairsRoom createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, int l, Direction direction) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -2, 0, 0, 7, 11, 7, direction);
            if (!StairsRoom.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new StairsRoom(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 6, 1, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 6, 10, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 1, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 0, 6, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 1, 0, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 2, 1, 6, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 6, 5, 8, 6, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 2, 0, 5, 4, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 3, 2, 6, 5, 2, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 3, 4, 6, 5, 4, blockState2, blockState2, false);
            this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 5, 2, 5, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 5, 4, 3, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 3, 2, 5, 3, 4, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 2, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 5, 1, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 7, 1, 5, 7, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 8, 2, 6, 8, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 6, 0, 4, 8, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 5, 0, blockState, blockState, false);
            for (int i = 0; i <= 6; ++i) {
                for (int j = 0; j <= 6; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class MonsterThrone
    extends NetherBridgePiece {
        private static final int WIDTH = 7;
        private static final int HEIGHT = 8;
        private static final int DEPTH = 9;
        private boolean hasPlacedSpawner;

        public MonsterThrone(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, i, boundingBox);
            this.setOrientation(direction);
        }

        public MonsterThrone(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_MONSTER_THRONE, compoundTag);
            this.hasPlacedSpawner = compoundTag.getBoolean("Mob");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Mob", this.hasPlacedSpawner);
        }

        public static MonsterThrone createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, int l, Direction direction) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -2, 0, 0, 7, 8, 9, direction);
            if (!MonsterThrone.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new MonsterThrone(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockPos.MutableBlockPos blockPos2;
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 6, 7, 7, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 0, 0, 5, 1, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 1, 5, 2, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 2, 5, 3, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 3, 5, 4, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 0, 1, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 2, 0, 5, 4, 2, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 5, 2, 1, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 5, 2, 5, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 3, 0, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 5, 3, 6, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 5, 8, 5, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 1, 6, 3, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 5, 6, 3, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)).setValue(FenceBlock.NORTH, true), 0, 6, 3, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.NORTH, true), 6, 6, 3, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 4, 0, 6, 7, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 6, 6, 4, 6, 6, 7, blockState2, blockState2, false);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)).setValue(FenceBlock.SOUTH, true), 0, 6, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.SOUTH, true), 6, 6, 8, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 1, 6, 8, 5, 6, 8, blockState, blockState, false);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 1, 7, 8, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 2, 7, 8, 4, 7, 8, blockState, blockState, false);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 5, 7, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true), 2, 8, 8, boundingBox);
            this.placeBlock(worldGenLevel, blockState, 3, 8, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true), 4, 8, 8, boundingBox);
            if (!this.hasPlacedSpawner && boundingBox.isInside(blockPos2 = this.getWorldPos(3, 5, 5))) {
                this.hasPlacedSpawner = true;
                worldGenLevel.setBlock(blockPos2, Blocks.SPAWNER.defaultBlockState(), 2);
                BlockEntity blockEntity = worldGenLevel.getBlockEntity(blockPos2);
                if (blockEntity instanceof SpawnerBlockEntity) {
                    ((SpawnerBlockEntity)blockEntity).getSpawner().setEntityId(EntityType.BLAZE);
                }
            }
            for (int i = 0; i <= 6; ++i) {
                for (int j = 0; j <= 6; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class CastleEntrance
    extends NetherBridgePiece {
        private static final int WIDTH = 13;
        private static final int HEIGHT = 14;
        private static final int DEPTH = 13;

        public CastleEntrance(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, i, boundingBox);
            this.setOrientation(direction);
        }

        public CastleEntrance(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_ENTRANCE, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 5, 3, true);
        }

        public static CastleEntrance createPiece(StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -5, -3, 0, 13, 14, 13, direction);
            if (!CastleEntrance.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleEntrance(l, random, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int j;
            int i;
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 8, 0, 7, 8, 0, Blocks.NETHER_BRICK_FENCE.defaultBlockState(), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            for (i = 1; i <= 11; i += 2) {
                this.generateBox(worldGenLevel, boundingBox, i, 10, 0, i, 11, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, i, 10, 12, i, 11, 12, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 10, i, 0, 11, i, blockState2, blockState2, false);
                this.generateBox(worldGenLevel, boundingBox, 12, 10, i, 12, 11, i, blockState2, blockState2, false);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, boundingBox);
                if (i == 11) continue;
                this.placeBlock(worldGenLevel, blockState, i + 1, 13, 0, boundingBox);
                this.placeBlock(worldGenLevel, blockState, i + 1, 13, 12, boundingBox);
                this.placeBlock(worldGenLevel, blockState2, 0, 13, i + 1, boundingBox);
                this.placeBlock(worldGenLevel, blockState2, 12, 13, i + 1, boundingBox);
            }
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 0, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 0, boundingBox);
            for (i = 3; i <= 9; i += 2) {
                this.generateBox(worldGenLevel, boundingBox, 1, 7, i, 1, 8, i, (BlockState)blockState2.setValue(FenceBlock.WEST, true), (BlockState)blockState2.setValue(FenceBlock.WEST, true), false);
                this.generateBox(worldGenLevel, boundingBox, 11, 7, i, 11, 8, i, (BlockState)blockState2.setValue(FenceBlock.EAST, true), (BlockState)blockState2.setValue(FenceBlock.EAST, true), false);
            }
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (i = 4; i <= 8; ++i) {
                for (j = 0; j <= 2; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, 12 - j, boundingBox);
                }
            }
            for (i = 0; i <= 2; ++i) {
                for (j = 4; j <= 8; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - i, -1, j, boundingBox);
                }
            }
            this.generateBox(worldGenLevel, boundingBox, 5, 5, 5, 7, 5, 7, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 1, 6, 6, 4, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 6, 0, 6, boundingBox);
            this.placeBlock(worldGenLevel, Blocks.LAVA.defaultBlockState(), 6, 5, 6, boundingBox);
            BlockPos.MutableBlockPos blockPos2 = this.getWorldPos(6, 5, 6);
            if (boundingBox.isInside(blockPos2)) {
                worldGenLevel.scheduleTick((BlockPos)blockPos2, Fluids.LAVA, 0);
            }
        }
    }

    public static class CastleSmallCorridorPiece
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;

        public CastleSmallCorridorPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, i, boundingBox);
            this.setOrientation(direction);
        }

        public CastleSmallCorridorPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 1, 0, true);
        }

        public static CastleSmallCorridorPiece createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
            if (!CastleSmallCorridorPiece.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleSmallCorridorPiece(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 4, 1, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 4, 3, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 1, 4, 4, 1, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 3, 4, 4, 3, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class CastleSmallCorridorRightTurnPiece
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;
        private boolean isNeedingChest;

        public CastleSmallCorridorRightTurnPiece(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, i, boundingBox);
            this.setOrientation(direction);
            this.isNeedingChest = random.nextInt(3) == 0;
        }

        public CastleSmallCorridorRightTurnPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_RIGHT_TURN, compoundTag);
            this.isNeedingChest = compoundTag.getBoolean("Chest");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Chest", this.isNeedingChest);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildRight((StartPiece)structurePiece, structurePieceAccessor, random, 0, 1, true);
        }

        public static CastleSmallCorridorRightTurnPiece createPiece(StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
            if (!CastleSmallCorridorRightTurnPiece.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleSmallCorridorRightTurnPiece(l, random, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 1, 0, 4, 1, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 3, 0, 4, 3, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
            if (this.isNeedingChest && boundingBox.isInside(this.getWorldPos(1, 2, 3))) {
                this.isNeedingChest = false;
                this.createChest(worldGenLevel, boundingBox, random, 1, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class CastleSmallCorridorLeftTurnPiece
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;
        private boolean isNeedingChest;

        public CastleSmallCorridorLeftTurnPiece(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, i, boundingBox);
            this.setOrientation(direction);
            this.isNeedingChest = random.nextInt(3) == 0;
        }

        public CastleSmallCorridorLeftTurnPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_LEFT_TURN, compoundTag);
            this.isNeedingChest = compoundTag.getBoolean("Chest");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("Chest", this.isNeedingChest);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildLeft((StartPiece)structurePiece, structurePieceAccessor, random, 0, 1, true);
        }

        public static CastleSmallCorridorLeftTurnPiece createPiece(StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
            if (!CastleSmallCorridorLeftTurnPiece.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleSmallCorridorLeftTurnPiece(l, random, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 1, 4, 4, 1, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 4, 3, 3, 4, 4, 3, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 4, 3, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 4, 1, 4, 4, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 3, 3, 4, 3, 4, 4, blockState, blockState, false);
            if (this.isNeedingChest && boundingBox.isInside(this.getWorldPos(3, 2, 3))) {
                this.isNeedingChest = false;
                this.createChest(worldGenLevel, boundingBox, random, 3, 2, 3, BuiltInLootTables.NETHER_BRIDGE);
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class CastleCorridorStairsPiece
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 14;
        private static final int DEPTH = 10;

        public CastleCorridorStairsPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, i, boundingBox);
            this.setOrientation(direction);
        }

        public CastleCorridorStairsPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_STAIRS, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 1, 0, true);
        }

        public static CastleCorridorStairsPiece createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -7, 0, 5, 14, 10, direction);
            if (!CastleCorridorStairsPiece.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleCorridorStairsPiece(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockState blockState = (BlockState)Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            for (int i = 0; i <= 9; ++i) {
                int j = Math.max(1, 7 - i);
                int k = Math.min(Math.max(j + 5, 14 - i), 13);
                int l = i;
                this.generateBox(worldGenLevel, boundingBox, 0, 0, l, 4, j, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 1, j + 1, l, 3, k - 1, l, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
                if (i <= 6) {
                    this.placeBlock(worldGenLevel, blockState, 1, j + 1, l, boundingBox);
                    this.placeBlock(worldGenLevel, blockState, 2, j + 1, l, boundingBox);
                    this.placeBlock(worldGenLevel, blockState, 3, j + 1, l, boundingBox);
                }
                this.generateBox(worldGenLevel, boundingBox, 0, k, l, 4, k, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 0, j + 1, l, 0, k - 1, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                this.generateBox(worldGenLevel, boundingBox, 4, j + 1, l, 4, k - 1, l, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                if ((i & 1) == 0) {
                    this.generateBox(worldGenLevel, boundingBox, 0, j + 2, l, 0, j + 3, l, blockState2, blockState2, false);
                    this.generateBox(worldGenLevel, boundingBox, 4, j + 2, l, 4, j + 3, l, blockState2, blockState2, false);
                }
                for (int m = 0; m <= 4; ++m) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), m, -1, l, boundingBox);
                }
            }
        }
    }

    public static class CastleCorridorTBalconyPiece
    extends NetherBridgePiece {
        private static final int WIDTH = 9;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 9;

        public CastleCorridorTBalconyPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, i, boundingBox);
            this.setOrientation(direction);
        }

        public CastleCorridorTBalconyPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_CORRIDOR_T_BALCONY, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            int i = 1;
            Direction direction = this.getOrientation();
            if (direction == Direction.WEST || direction == Direction.NORTH) {
                i = 5;
            }
            this.generateChildLeft((StartPiece)structurePiece, structurePieceAccessor, random, 0, i, random.nextInt(8) > 0);
            this.generateChildRight((StartPiece)structurePiece, structurePieceAccessor, random, 0, i, random.nextInt(8) > 0);
        }

        public static CastleCorridorTBalconyPiece createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -3, 0, 0, 9, 7, 9, direction);
            if (!CastleCorridorTBalconyPiece.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleCorridorTBalconyPiece(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 8, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 8, 5, 8, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 0, 8, 6, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 2, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 2, 0, 8, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 0, 1, 4, 0, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 3, 0, 7, 4, 0, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 4, 8, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 1, 4, 2, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 1, 4, 7, 2, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 8, 7, 3, 8, blockState2, blockState2, false);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.EAST, true)).setValue(FenceBlock.SOUTH, true), 0, 3, 8, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.SOUTH, true), 8, 3, 8, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 6, 0, 3, 7, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 8, 3, 6, 8, 3, 7, blockState, blockState, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 4, 0, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 3, 4, 8, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 3, 5, 2, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 6, 3, 5, 7, 5, 5, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 1, 4, 5, 1, 5, 5, blockState2, blockState2, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 4, 5, 7, 5, 5, blockState2, blockState2, false);
            for (int i = 0; i <= 5; ++i) {
                for (int j = 0; j <= 8; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), j, -1, i, boundingBox);
                }
            }
        }
    }

    public static class CastleSmallCorridorCrossingPiece
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 7;
        private static final int DEPTH = 5;

        public CastleSmallCorridorCrossingPiece(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, i, boundingBox);
            this.setOrientation(direction);
        }

        public CastleSmallCorridorCrossingPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_SMALL_CORRIDOR_CROSSING, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 1, 0, true);
            this.generateChildLeft((StartPiece)structurePiece, structurePieceAccessor, random, 0, 1, true);
            this.generateChildRight((StartPiece)structurePiece, structurePieceAccessor, random, 0, 1, true);
        }

        public static CastleSmallCorridorCrossingPiece createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, 0, 0, 5, 7, 5, direction);
            if (!CastleSmallCorridorCrossingPiece.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleSmallCorridorCrossingPiece(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 4, 1, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 4, 5, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 0, 0, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 0, 4, 5, 0, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 4, 0, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 4, 4, 5, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 6, 0, 4, 6, 4, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (int i = 0; i <= 4; ++i) {
                for (int j = 0; j <= 4; ++j) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, -1, j, boundingBox);
                }
            }
        }
    }

    public static class CastleStalkRoom
    extends NetherBridgePiece {
        private static final int WIDTH = 13;
        private static final int HEIGHT = 14;
        private static final int DEPTH = 13;

        public CastleStalkRoom(int i, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, i, boundingBox);
            this.setOrientation(direction);
        }

        public CastleStalkRoom(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_CASTLE_STALK_ROOM, compoundTag);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, Random random) {
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 5, 3, true);
            this.generateChildForward((StartPiece)structurePiece, structurePieceAccessor, random, 5, 11, true);
        }

        public static CastleStalkRoom createPiece(StructurePieceAccessor structurePieceAccessor, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -5, -3, 0, 13, 14, 13, direction);
            if (!CastleStalkRoom.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new CastleStalkRoom(l, boundingBox, direction);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int m;
            int l;
            int j;
            int i;
            this.generateBox(worldGenLevel, boundingBox, 0, 3, 0, 12, 4, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 12, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 1, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 11, 5, 0, 12, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 11, 4, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 11, 10, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 9, 11, 7, 12, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 0, 4, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 0, 10, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 5, 9, 0, 7, 12, 1, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 11, 2, 10, 12, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.WEST, true)).setValue(FenceBlock.EAST, true);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.SOUTH, true);
            BlockState blockState3 = (BlockState)blockState2.setValue(FenceBlock.WEST, true);
            BlockState blockState4 = (BlockState)blockState2.setValue(FenceBlock.EAST, true);
            for (i = 1; i <= 11; i += 2) {
                this.generateBox(worldGenLevel, boundingBox, i, 10, 0, i, 11, 0, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, i, 10, 12, i, 11, 12, blockState, blockState, false);
                this.generateBox(worldGenLevel, boundingBox, 0, 10, i, 0, 11, i, blockState2, blockState2, false);
                this.generateBox(worldGenLevel, boundingBox, 12, 10, i, 12, 11, i, blockState2, blockState2, false);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 0, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), i, 13, 12, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 0, 13, i, boundingBox);
                this.placeBlock(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12, 13, i, boundingBox);
                if (i == 11) continue;
                this.placeBlock(worldGenLevel, blockState, i + 1, 13, 0, boundingBox);
                this.placeBlock(worldGenLevel, blockState, i + 1, 13, 12, boundingBox);
                this.placeBlock(worldGenLevel, blockState2, 0, 13, i + 1, boundingBox);
                this.placeBlock(worldGenLevel, blockState2, 12, 13, i + 1, boundingBox);
            }
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 0, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.EAST, true), 0, 13, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.SOUTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 12, boundingBox);
            this.placeBlock(worldGenLevel, (BlockState)((BlockState)Blocks.NETHER_BRICK_FENCE.defaultBlockState().setValue(FenceBlock.NORTH, true)).setValue(FenceBlock.WEST, true), 12, 13, 0, boundingBox);
            for (i = 3; i <= 9; i += 2) {
                this.generateBox(worldGenLevel, boundingBox, 1, 7, i, 1, 8, i, blockState3, blockState3, false);
                this.generateBox(worldGenLevel, boundingBox, 11, 7, i, 11, 8, i, blockState4, blockState4, false);
            }
            BlockState blockState5 = (BlockState)Blocks.NETHER_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            for (j = 0; j <= 6; ++j) {
                int k = j + 4;
                for (l = 5; l <= 7; ++l) {
                    this.placeBlock(worldGenLevel, blockState5, l, 5 + j, k, boundingBox);
                }
                if (k >= 5 && k <= 8) {
                    this.generateBox(worldGenLevel, boundingBox, 5, 5, k, 7, j + 4, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                } else if (k >= 9 && k <= 10) {
                    this.generateBox(worldGenLevel, boundingBox, 5, 8, k, 7, j + 4, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
                if (j < 1) continue;
                this.generateBox(worldGenLevel, boundingBox, 5, 6 + j, k, 7, 9 + j, k, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }
            for (j = 5; j <= 7; ++j) {
                this.placeBlock(worldGenLevel, blockState5, j, 12, 11, boundingBox);
            }
            this.generateBox(worldGenLevel, boundingBox, 5, 6, 7, 5, 7, 7, blockState4, blockState4, false);
            this.generateBox(worldGenLevel, boundingBox, 7, 6, 7, 7, 7, 7, blockState3, blockState3, false);
            this.generateBox(worldGenLevel, boundingBox, 5, 13, 12, 7, 13, 12, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 2, 3, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 9, 3, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 2, 5, 4, 2, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 9, 5, 2, 10, 5, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 9, 5, 9, 10, 5, 10, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 10, 5, 4, 10, 5, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            BlockState blockState6 = (BlockState)blockState5.setValue(StairBlock.FACING, Direction.EAST);
            BlockState blockState7 = (BlockState)blockState5.setValue(StairBlock.FACING, Direction.WEST);
            this.placeBlock(worldGenLevel, blockState7, 4, 5, 2, boundingBox);
            this.placeBlock(worldGenLevel, blockState7, 4, 5, 3, boundingBox);
            this.placeBlock(worldGenLevel, blockState7, 4, 5, 9, boundingBox);
            this.placeBlock(worldGenLevel, blockState7, 4, 5, 10, boundingBox);
            this.placeBlock(worldGenLevel, blockState6, 8, 5, 2, boundingBox);
            this.placeBlock(worldGenLevel, blockState6, 8, 5, 3, boundingBox);
            this.placeBlock(worldGenLevel, blockState6, 8, 5, 9, boundingBox);
            this.placeBlock(worldGenLevel, blockState6, 8, 5, 10, boundingBox);
            this.generateBox(worldGenLevel, boundingBox, 3, 4, 4, 4, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 4, 4, 9, 4, 8, Blocks.SOUL_SAND.defaultBlockState(), Blocks.SOUL_SAND.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 3, 5, 4, 4, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 8, 5, 4, 9, 5, 8, Blocks.NETHER_WART.defaultBlockState(), Blocks.NETHER_WART.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 2, 0, 8, 2, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 2, 4, 12, 2, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 0, 0, 8, 1, 3, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 4, 0, 9, 8, 1, 12, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 4, 3, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            this.generateBox(worldGenLevel, boundingBox, 9, 0, 4, 12, 1, 8, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (l = 4; l <= 8; ++l) {
                for (m = 0; m <= 2; ++m) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, m, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, 12 - m, boundingBox);
                }
            }
            for (l = 0; l <= 2; ++l) {
                for (m = 4; m <= 8; ++m) {
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), l, -1, m, boundingBox);
                    this.fillColumnDown(worldGenLevel, Blocks.NETHER_BRICKS.defaultBlockState(), 12 - l, -1, m, boundingBox);
                }
            }
        }
    }

    public static class BridgeEndFiller
    extends NetherBridgePiece {
        private static final int WIDTH = 5;
        private static final int HEIGHT = 10;
        private static final int DEPTH = 8;
        private final int selfSeed;

        public BridgeEndFiller(int i, Random random, BoundingBox boundingBox, Direction direction) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, i, boundingBox);
            this.setOrientation(direction);
            this.selfSeed = random.nextInt();
        }

        public BridgeEndFiller(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_BRIDGE_END_FILLER, compoundTag);
            this.selfSeed = compoundTag.getInt("Seed");
        }

        public static BridgeEndFiller createPiece(StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
            BoundingBox boundingBox = BoundingBox.orientBox(i, j, k, -1, -3, 0, 5, 10, 8, direction);
            if (!BridgeEndFiller.isOkBox(boundingBox) || structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return new BridgeEndFiller(l, random, boundingBox, direction);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putInt("Seed", this.selfSeed);
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int k;
            int j;
            int i;
            Random random2 = new Random(this.selfSeed);
            for (i = 0; i <= 4; ++i) {
                for (j = 3; j <= 4; ++j) {
                    k = random2.nextInt(8);
                    this.generateBox(worldGenLevel, boundingBox, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
            }
            i = random2.nextInt(8);
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 0, 5, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            i = random2.nextInt(8);
            this.generateBox(worldGenLevel, boundingBox, 4, 5, 0, 4, 5, i, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            for (i = 0; i <= 4; ++i) {
                j = random2.nextInt(5);
                this.generateBox(worldGenLevel, boundingBox, i, 2, 0, i, 2, j, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
            }
            for (i = 0; i <= 4; ++i) {
                for (j = 0; j <= 1; ++j) {
                    k = random2.nextInt(3);
                    this.generateBox(worldGenLevel, boundingBox, i, j, 0, i, j, k, Blocks.NETHER_BRICKS.defaultBlockState(), Blocks.NETHER_BRICKS.defaultBlockState(), false);
                }
            }
        }
    }

    public static class StartPiece
    extends BridgeCrossing {
        public PieceWeight previousPiece;
        public List<PieceWeight> availableBridgePieces;
        public List<PieceWeight> availableCastlePieces;
        public final List<StructurePiece> pendingChildren = Lists.newArrayList();

        public StartPiece(Random random, int i, int j) {
            super(i, j, StartPiece.getRandomHorizontalDirection(random));
            this.availableBridgePieces = Lists.newArrayList();
            for (PieceWeight pieceWeight : BRIDGE_PIECE_WEIGHTS) {
                pieceWeight.placeCount = 0;
                this.availableBridgePieces.add(pieceWeight);
            }
            this.availableCastlePieces = Lists.newArrayList();
            for (PieceWeight pieceWeight : CASTLE_PIECE_WEIGHTS) {
                pieceWeight.placeCount = 0;
                this.availableCastlePieces.add(pieceWeight);
            }
        }

        public StartPiece(CompoundTag compoundTag) {
            super(StructurePieceType.NETHER_FORTRESS_START, compoundTag);
        }
    }

    static abstract class NetherBridgePiece
    extends StructurePiece {
        protected NetherBridgePiece(StructurePieceType structurePieceType, int i, BoundingBox boundingBox) {
            super(structurePieceType, i, boundingBox);
        }

        public NetherBridgePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
        }

        private int updatePieceWeight(List<PieceWeight> list) {
            boolean bl = false;
            int i = 0;
            for (PieceWeight pieceWeight : list) {
                if (pieceWeight.maxPlaceCount > 0 && pieceWeight.placeCount < pieceWeight.maxPlaceCount) {
                    bl = true;
                }
                i += pieceWeight.weight;
            }
            return bl ? i : -1;
        }

        private NetherBridgePiece generatePiece(StartPiece startPiece, List<PieceWeight> list, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, Direction direction, int l) {
            int m = this.updatePieceWeight(list);
            boolean bl = m > 0 && l <= 30;
            int n = 0;
            block0: while (n < 5 && bl) {
                ++n;
                int o = random.nextInt(m);
                for (PieceWeight pieceWeight : list) {
                    if ((o -= pieceWeight.weight) >= 0) continue;
                    if (!pieceWeight.doPlace(l) || pieceWeight == startPiece.previousPiece && !pieceWeight.allowInRow) continue block0;
                    NetherBridgePiece netherBridgePiece = NetherBridgePieces.findAndCreateBridgePieceFactory(pieceWeight, structurePieceAccessor, random, i, j, k, direction, l);
                    if (netherBridgePiece == null) continue;
                    ++pieceWeight.placeCount;
                    startPiece.previousPiece = pieceWeight;
                    if (!pieceWeight.isValid()) {
                        list.remove(pieceWeight);
                    }
                    return netherBridgePiece;
                }
            }
            return BridgeEndFiller.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
        }

        private StructurePiece generateAndAddPiece(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, int k, @Nullable Direction direction, int l, boolean bl) {
            NetherBridgePiece structurePiece;
            if (Math.abs(i - startPiece.getBoundingBox().minX()) > 112 || Math.abs(k - startPiece.getBoundingBox().minZ()) > 112) {
                return BridgeEndFiller.createPiece(structurePieceAccessor, random, i, j, k, direction, l);
            }
            List<PieceWeight> list = startPiece.availableBridgePieces;
            if (bl) {
                list = startPiece.availableCastlePieces;
            }
            if ((structurePiece = this.generatePiece(startPiece, list, structurePieceAccessor, random, i, j, k, direction, l + 1)) != null) {
                structurePieceAccessor.addPiece(structurePiece);
                startPiece.pendingChildren.add(structurePiece);
            }
            return structurePiece;
        }

        @Nullable
        protected StructurePiece generateChildForward(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, boolean bl) {
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() + i, this.boundingBox.minY() + j, this.boundingBox.minZ() - 1, direction, this.getGenDepth(), bl);
                    }
                    case SOUTH: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() + i, this.boundingBox.minY() + j, this.boundingBox.maxZ() + 1, direction, this.getGenDepth(), bl);
                    }
                    case WEST: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() + j, this.boundingBox.minZ() + i, direction, this.getGenDepth(), bl);
                    }
                    case EAST: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() + j, this.boundingBox.minZ() + i, direction, this.getGenDepth(), bl);
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateChildLeft(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, boolean bl) {
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.WEST, this.getGenDepth(), bl);
                    }
                    case SOUTH: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() - 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.WEST, this.getGenDepth(), bl);
                    }
                    case WEST: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), bl);
                    }
                    case EAST: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.minZ() - 1, Direction.NORTH, this.getGenDepth(), bl);
                    }
                }
            }
            return null;
        }

        @Nullable
        protected StructurePiece generateChildRight(StartPiece startPiece, StructurePieceAccessor structurePieceAccessor, Random random, int i, int j, boolean bl) {
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    case NORTH: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.EAST, this.getGenDepth(), bl);
                    }
                    case SOUTH: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.maxX() + 1, this.boundingBox.minY() + i, this.boundingBox.minZ() + j, Direction.EAST, this.getGenDepth(), bl);
                    }
                    case WEST: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), bl);
                    }
                    case EAST: {
                        return this.generateAndAddPiece(startPiece, structurePieceAccessor, random, this.boundingBox.minX() + j, this.boundingBox.minY() + i, this.boundingBox.maxZ() + 1, Direction.SOUTH, this.getGenDepth(), bl);
                    }
                }
            }
            return null;
        }

        protected static boolean isOkBox(BoundingBox boundingBox) {
            return boundingBox != null && boundingBox.minY() > 10;
        }
    }
}

