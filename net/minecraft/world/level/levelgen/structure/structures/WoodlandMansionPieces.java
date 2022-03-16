/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import org.jetbrains.annotations.Nullable;

public class WoodlandMansionPieces {
    public static void generateMansion(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, List<WoodlandMansionPiece> list, Random random) {
        MansionGrid mansionGrid = new MansionGrid(random);
        MansionPiecePlacer mansionPiecePlacer = new MansionPiecePlacer(structureTemplateManager, random);
        mansionPiecePlacer.createMansion(blockPos, rotation, list, mansionGrid);
    }

    public static void main(String[] strings) {
        Random random = new Random();
        long l = random.nextLong();
        System.out.println("Seed: " + l);
        random.setSeed(l);
        MansionGrid mansionGrid = new MansionGrid(random);
        mansionGrid.print();
    }

    static class MansionGrid {
        private static final int DEFAULT_SIZE = 11;
        private static final int CLEAR = 0;
        private static final int CORRIDOR = 1;
        private static final int ROOM = 2;
        private static final int START_ROOM = 3;
        private static final int TEST_ROOM = 4;
        private static final int BLOCKED = 5;
        private static final int ROOM_1x1 = 65536;
        private static final int ROOM_1x2 = 131072;
        private static final int ROOM_2x2 = 262144;
        private static final int ROOM_ORIGIN_FLAG = 0x100000;
        private static final int ROOM_DOOR_FLAG = 0x200000;
        private static final int ROOM_STAIRS_FLAG = 0x400000;
        private static final int ROOM_CORRIDOR_FLAG = 0x800000;
        private static final int ROOM_TYPE_MASK = 983040;
        private static final int ROOM_ID_MASK = 65535;
        private final Random random;
        final SimpleGrid baseGrid;
        final SimpleGrid thirdFloorGrid;
        final SimpleGrid[] floorRooms;
        final int entranceX;
        final int entranceY;

        public MansionGrid(Random random) {
            this.random = random;
            int i = 11;
            this.entranceX = 7;
            this.entranceY = 4;
            this.baseGrid = new SimpleGrid(11, 11, 5);
            this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
            this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
            this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
            this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
            this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
            this.baseGrid.set(0, 0, 11, 1, 5);
            this.baseGrid.set(0, 9, 11, 11, 5);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
            this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);
            while (this.cleanEdges(this.baseGrid)) {
            }
            this.floorRooms = new SimpleGrid[3];
            this.floorRooms[0] = new SimpleGrid(11, 11, 5);
            this.floorRooms[1] = new SimpleGrid(11, 11, 5);
            this.floorRooms[2] = new SimpleGrid(11, 11, 5);
            this.identifyRooms(this.baseGrid, this.floorRooms[0]);
            this.identifyRooms(this.baseGrid, this.floorRooms[1]);
            this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 0x800000);
            this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 0x800000);
            this.thirdFloorGrid = new SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
            this.setupThirdFloor();
            this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
        }

        public static boolean isHouse(SimpleGrid simpleGrid, int i, int j) {
            int k = simpleGrid.get(i, j);
            return k == 1 || k == 2 || k == 3 || k == 4;
        }

        public boolean isRoomId(SimpleGrid simpleGrid, int i, int j, int k, int l) {
            return (this.floorRooms[k].get(i, j) & 0xFFFF) == l;
        }

        @Nullable
        public Direction get1x2RoomDirection(SimpleGrid simpleGrid, int i, int j, int k, int l) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (!this.isRoomId(simpleGrid, i + direction.getStepX(), j + direction.getStepZ(), k, l)) continue;
                return direction;
            }
            return null;
        }

        private void recursiveCorridor(SimpleGrid simpleGrid, int i, int j, Direction direction, int k) {
            Direction direction2;
            if (k <= 0) {
                return;
            }
            simpleGrid.set(i, j, 1);
            simpleGrid.setif(i + direction.getStepX(), j + direction.getStepZ(), 0, 1);
            for (int l = 0; l < 8; ++l) {
                direction2 = Direction.from2DDataValue(this.random.nextInt(4));
                if (direction2 == direction.getOpposite() || direction2 == Direction.EAST && this.random.nextBoolean()) continue;
                int m = i + direction.getStepX();
                int n = j + direction.getStepZ();
                if (simpleGrid.get(m + direction2.getStepX(), n + direction2.getStepZ()) != 0 || simpleGrid.get(m + direction2.getStepX() * 2, n + direction2.getStepZ() * 2) != 0) continue;
                this.recursiveCorridor(simpleGrid, i + direction.getStepX() + direction2.getStepX(), j + direction.getStepZ() + direction2.getStepZ(), direction2, k - 1);
                break;
            }
            Direction direction3 = direction.getClockWise();
            direction2 = direction.getCounterClockWise();
            simpleGrid.setif(i + direction3.getStepX(), j + direction3.getStepZ(), 0, 2);
            simpleGrid.setif(i + direction2.getStepX(), j + direction2.getStepZ(), 0, 2);
            simpleGrid.setif(i + direction.getStepX() + direction3.getStepX(), j + direction.getStepZ() + direction3.getStepZ(), 0, 2);
            simpleGrid.setif(i + direction.getStepX() + direction2.getStepX(), j + direction.getStepZ() + direction2.getStepZ(), 0, 2);
            simpleGrid.setif(i + direction.getStepX() * 2, j + direction.getStepZ() * 2, 0, 2);
            simpleGrid.setif(i + direction3.getStepX() * 2, j + direction3.getStepZ() * 2, 0, 2);
            simpleGrid.setif(i + direction2.getStepX() * 2, j + direction2.getStepZ() * 2, 0, 2);
        }

        private boolean cleanEdges(SimpleGrid simpleGrid) {
            boolean bl = false;
            for (int i = 0; i < simpleGrid.height; ++i) {
                for (int j = 0; j < simpleGrid.width; ++j) {
                    if (simpleGrid.get(j, i) != 0) continue;
                    int k = 0;
                    k += MansionGrid.isHouse(simpleGrid, j + 1, i) ? 1 : 0;
                    k += MansionGrid.isHouse(simpleGrid, j - 1, i) ? 1 : 0;
                    k += MansionGrid.isHouse(simpleGrid, j, i + 1) ? 1 : 0;
                    if ((k += MansionGrid.isHouse(simpleGrid, j, i - 1) ? 1 : 0) >= 3) {
                        simpleGrid.set(j, i, 2);
                        bl = true;
                        continue;
                    }
                    if (k != 2) continue;
                    int l = 0;
                    l += MansionGrid.isHouse(simpleGrid, j + 1, i + 1) ? 1 : 0;
                    l += MansionGrid.isHouse(simpleGrid, j - 1, i + 1) ? 1 : 0;
                    l += MansionGrid.isHouse(simpleGrid, j + 1, i - 1) ? 1 : 0;
                    if ((l += MansionGrid.isHouse(simpleGrid, j - 1, i - 1) ? 1 : 0) > 1) continue;
                    simpleGrid.set(j, i, 2);
                    bl = true;
                }
            }
            return bl;
        }

        private void setupThirdFloor() {
            int l;
            int j;
            ArrayList<Tuple<Integer, Integer>> list = Lists.newArrayList();
            SimpleGrid simpleGrid = this.floorRooms[1];
            for (int i = 0; i < this.thirdFloorGrid.height; ++i) {
                for (j = 0; j < this.thirdFloorGrid.width; ++j) {
                    int k = simpleGrid.get(j, i);
                    l = k & 0xF0000;
                    if (l != 131072 || (k & 0x200000) != 0x200000) continue;
                    list.add(new Tuple<Integer, Integer>(j, i));
                }
            }
            if (list.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                return;
            }
            Tuple tuple = (Tuple)list.get(this.random.nextInt(list.size()));
            j = simpleGrid.get((Integer)tuple.getA(), (Integer)tuple.getB());
            simpleGrid.set((Integer)tuple.getA(), (Integer)tuple.getB(), j | 0x400000);
            Direction direction = this.get1x2RoomDirection(this.baseGrid, (Integer)tuple.getA(), (Integer)tuple.getB(), 1, j & 0xFFFF);
            l = (Integer)tuple.getA() + direction.getStepX();
            int m = (Integer)tuple.getB() + direction.getStepZ();
            for (int n = 0; n < this.thirdFloorGrid.height; ++n) {
                for (int o = 0; o < this.thirdFloorGrid.width; ++o) {
                    if (!MansionGrid.isHouse(this.baseGrid, o, n)) {
                        this.thirdFloorGrid.set(o, n, 5);
                        continue;
                    }
                    if (o == (Integer)tuple.getA() && n == (Integer)tuple.getB()) {
                        this.thirdFloorGrid.set(o, n, 3);
                        continue;
                    }
                    if (o != l || n != m) continue;
                    this.thirdFloorGrid.set(o, n, 3);
                    this.floorRooms[2].set(o, n, 0x800000);
                }
            }
            ArrayList<Direction> list2 = Lists.newArrayList();
            for (Direction direction2 : Direction.Plane.HORIZONTAL) {
                if (this.thirdFloorGrid.get(l + direction2.getStepX(), m + direction2.getStepZ()) != 0) continue;
                list2.add(direction2);
            }
            if (list2.isEmpty()) {
                this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
                simpleGrid.set((Integer)tuple.getA(), (Integer)tuple.getB(), j);
                return;
            }
            Direction direction3 = (Direction)list2.get(this.random.nextInt(list2.size()));
            this.recursiveCorridor(this.thirdFloorGrid, l + direction3.getStepX(), m + direction3.getStepZ(), direction3, 4);
            while (this.cleanEdges(this.thirdFloorGrid)) {
            }
        }

        private void identifyRooms(SimpleGrid simpleGrid, SimpleGrid simpleGrid2) {
            int i;
            ArrayList<Tuple<Integer, Integer>> list = Lists.newArrayList();
            for (i = 0; i < simpleGrid.height; ++i) {
                for (int j = 0; j < simpleGrid.width; ++j) {
                    if (simpleGrid.get(j, i) != 2) continue;
                    list.add(new Tuple<Integer, Integer>(j, i));
                }
            }
            Collections.shuffle(list, this.random);
            i = 10;
            for (Tuple tuple : list) {
                int l;
                int k = (Integer)tuple.getA();
                if (simpleGrid2.get(k, l = ((Integer)tuple.getB()).intValue()) != 0) continue;
                int m = k;
                int n = k;
                int o = l;
                int p = l;
                int q = 65536;
                if (simpleGrid2.get(k + 1, l) == 0 && simpleGrid2.get(k, l + 1) == 0 && simpleGrid2.get(k + 1, l + 1) == 0 && simpleGrid.get(k + 1, l) == 2 && simpleGrid.get(k, l + 1) == 2 && simpleGrid.get(k + 1, l + 1) == 2) {
                    ++n;
                    ++p;
                    q = 262144;
                } else if (simpleGrid2.get(k - 1, l) == 0 && simpleGrid2.get(k, l + 1) == 0 && simpleGrid2.get(k - 1, l + 1) == 0 && simpleGrid.get(k - 1, l) == 2 && simpleGrid.get(k, l + 1) == 2 && simpleGrid.get(k - 1, l + 1) == 2) {
                    --m;
                    ++p;
                    q = 262144;
                } else if (simpleGrid2.get(k - 1, l) == 0 && simpleGrid2.get(k, l - 1) == 0 && simpleGrid2.get(k - 1, l - 1) == 0 && simpleGrid.get(k - 1, l) == 2 && simpleGrid.get(k, l - 1) == 2 && simpleGrid.get(k - 1, l - 1) == 2) {
                    --m;
                    --o;
                    q = 262144;
                } else if (simpleGrid2.get(k + 1, l) == 0 && simpleGrid.get(k + 1, l) == 2) {
                    ++n;
                    q = 131072;
                } else if (simpleGrid2.get(k, l + 1) == 0 && simpleGrid.get(k, l + 1) == 2) {
                    ++p;
                    q = 131072;
                } else if (simpleGrid2.get(k - 1, l) == 0 && simpleGrid.get(k - 1, l) == 2) {
                    --m;
                    q = 131072;
                } else if (simpleGrid2.get(k, l - 1) == 0 && simpleGrid.get(k, l - 1) == 2) {
                    --o;
                    q = 131072;
                }
                int r = this.random.nextBoolean() ? m : n;
                int s = this.random.nextBoolean() ? o : p;
                int t = 0x200000;
                if (!simpleGrid.edgesTo(r, s, 1)) {
                    r = r == m ? n : m;
                    int n2 = s = s == o ? p : o;
                    if (!simpleGrid.edgesTo(r, s, 1)) {
                        int n3 = s = s == o ? p : o;
                        if (!simpleGrid.edgesTo(r, s, 1)) {
                            r = r == m ? n : m;
                            int n4 = s = s == o ? p : o;
                            if (!simpleGrid.edgesTo(r, s, 1)) {
                                t = 0;
                                r = m;
                                s = o;
                            }
                        }
                    }
                }
                for (int u = o; u <= p; ++u) {
                    for (int v = m; v <= n; ++v) {
                        if (v == r && u == s) {
                            simpleGrid2.set(v, u, 0x100000 | t | q | i);
                            continue;
                        }
                        simpleGrid2.set(v, u, q | i);
                    }
                }
                ++i;
            }
        }

        public void print() {
            for (int i = 0; i < 2; ++i) {
                SimpleGrid simpleGrid = i == 0 ? this.baseGrid : this.thirdFloorGrid;
                for (int j = 0; j < simpleGrid.height; ++j) {
                    for (int k = 0; k < simpleGrid.width; ++k) {
                        int l = simpleGrid.get(k, j);
                        if (l == 1) {
                            System.out.print("+");
                            continue;
                        }
                        if (l == 4) {
                            System.out.print("x");
                            continue;
                        }
                        if (l == 2) {
                            System.out.print("X");
                            continue;
                        }
                        if (l == 3) {
                            System.out.print("O");
                            continue;
                        }
                        if (l == 5) {
                            System.out.print("#");
                            continue;
                        }
                        System.out.print(" ");
                    }
                    System.out.println("");
                }
                System.out.println("");
            }
        }
    }

    static class MansionPiecePlacer {
        private final StructureTemplateManager structureTemplateManager;
        private final Random random;
        private int startX;
        private int startY;

        public MansionPiecePlacer(StructureTemplateManager structureTemplateManager, Random random) {
            this.structureTemplateManager = structureTemplateManager;
            this.random = random;
        }

        public void createMansion(BlockPos blockPos, Rotation rotation, List<WoodlandMansionPiece> list, MansionGrid mansionGrid) {
            int l;
            PlacementData placementData = new PlacementData();
            placementData.position = blockPos;
            placementData.rotation = rotation;
            placementData.wallType = "wall_flat";
            PlacementData placementData2 = new PlacementData();
            this.entrance(list, placementData);
            placementData2.position = placementData.position.above(8);
            placementData2.rotation = placementData.rotation;
            placementData2.wallType = "wall_window";
            if (!list.isEmpty()) {
                // empty if block
            }
            SimpleGrid simpleGrid = mansionGrid.baseGrid;
            SimpleGrid simpleGrid2 = mansionGrid.thirdFloorGrid;
            this.startX = mansionGrid.entranceX + 1;
            this.startY = mansionGrid.entranceY + 1;
            int i = mansionGrid.entranceX + 1;
            int j = mansionGrid.entranceY;
            this.traverseOuterWalls(list, placementData, simpleGrid, Direction.SOUTH, this.startX, this.startY, i, j);
            this.traverseOuterWalls(list, placementData2, simpleGrid, Direction.SOUTH, this.startX, this.startY, i, j);
            PlacementData placementData3 = new PlacementData();
            placementData3.position = placementData.position.above(19);
            placementData3.rotation = placementData.rotation;
            placementData3.wallType = "wall_window";
            boolean bl = false;
            for (int k = 0; k < simpleGrid2.height && !bl; ++k) {
                for (l = simpleGrid2.width - 1; l >= 0 && !bl; --l) {
                    if (!MansionGrid.isHouse(simpleGrid2, l, k)) continue;
                    placementData3.position = placementData3.position.relative(rotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                    placementData3.position = placementData3.position.relative(rotation.rotate(Direction.EAST), (l - this.startX) * 8);
                    this.traverseWallPiece(list, placementData3);
                    this.traverseOuterWalls(list, placementData3, simpleGrid2, Direction.SOUTH, l, k, l, k);
                    bl = true;
                }
            }
            this.createRoof(list, blockPos.above(16), rotation, simpleGrid, simpleGrid2);
            this.createRoof(list, blockPos.above(27), rotation, simpleGrid2, null);
            if (!list.isEmpty()) {
                // empty if block
            }
            FloorRoomCollection[] floorRoomCollections = new FloorRoomCollection[]{new FirstFloorRoomCollection(), new SecondFloorRoomCollection(), new ThirdFloorRoomCollection()};
            for (l = 0; l < 3; ++l) {
                BlockPos blockPos2 = blockPos.above(8 * l + (l == 2 ? 3 : 0));
                SimpleGrid simpleGrid3 = mansionGrid.floorRooms[l];
                SimpleGrid simpleGrid4 = l == 2 ? simpleGrid2 : simpleGrid;
                String string = l == 0 ? "carpet_south_1" : "carpet_south_2";
                String string2 = l == 0 ? "carpet_west_1" : "carpet_west_2";
                for (int m = 0; m < simpleGrid4.height; ++m) {
                    for (int n = 0; n < simpleGrid4.width; ++n) {
                        if (simpleGrid4.get(n, m) != 1) continue;
                        BlockPos blockPos3 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (m - this.startY) * 8);
                        blockPos3 = blockPos3.relative(rotation.rotate(Direction.EAST), (n - this.startX) * 8);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "corridor_floor", blockPos3, rotation));
                        if (simpleGrid4.get(n, m - 1) == 1 || (simpleGrid3.get(n, m - 1) & 0x800000) == 0x800000) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "carpet_north", blockPos3.relative(rotation.rotate(Direction.EAST), 1).above(), rotation));
                        }
                        if (simpleGrid4.get(n + 1, m) == 1 || (simpleGrid3.get(n + 1, m) & 0x800000) == 0x800000) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "carpet_east", blockPos3.relative(rotation.rotate(Direction.SOUTH), 1).relative(rotation.rotate(Direction.EAST), 5).above(), rotation));
                        }
                        if (simpleGrid4.get(n, m + 1) == 1 || (simpleGrid3.get(n, m + 1) & 0x800000) == 0x800000) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, string, blockPos3.relative(rotation.rotate(Direction.SOUTH), 5).relative(rotation.rotate(Direction.WEST), 1), rotation));
                        }
                        if (simpleGrid4.get(n - 1, m) != 1 && (simpleGrid3.get(n - 1, m) & 0x800000) != 0x800000) continue;
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, string2, blockPos3.relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.NORTH), 1), rotation));
                    }
                }
                String string3 = l == 0 ? "indoors_wall_1" : "indoors_wall_2";
                String string4 = l == 0 ? "indoors_door_1" : "indoors_door_2";
                ArrayList<Direction> list2 = Lists.newArrayList();
                for (int o = 0; o < simpleGrid4.height; ++o) {
                    for (int p = 0; p < simpleGrid4.width; ++p) {
                        Direction direction3;
                        BlockPos blockPos5;
                        boolean bl2;
                        boolean bl3 = bl2 = l == 2 && simpleGrid4.get(p, o) == 3;
                        if (simpleGrid4.get(p, o) != 2 && !bl2) continue;
                        int q = simpleGrid3.get(p, o);
                        int r = q & 0xF0000;
                        int s = q & 0xFFFF;
                        bl2 = bl2 && (q & 0x800000) == 0x800000;
                        list2.clear();
                        if ((q & 0x200000) == 0x200000) {
                            for (Direction direction : Direction.Plane.HORIZONTAL) {
                                if (simpleGrid4.get(p + direction.getStepX(), o + direction.getStepZ()) != 1) continue;
                                list2.add(direction);
                            }
                        }
                        Direction direction2 = null;
                        if (!list2.isEmpty()) {
                            direction2 = (Direction)list2.get(this.random.nextInt(list2.size()));
                        } else if ((q & 0x100000) == 0x100000) {
                            direction2 = Direction.UP;
                        }
                        BlockPos blockPos4 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (o - this.startY) * 8);
                        blockPos4 = blockPos4.relative(rotation.rotate(Direction.EAST), -1 + (p - this.startX) * 8);
                        if (MansionGrid.isHouse(simpleGrid4, p - 1, o) && !mansionGrid.isRoomId(simpleGrid4, p - 1, o, l, s)) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, direction2 == Direction.WEST ? string4 : string3, blockPos4, rotation));
                        }
                        if (simpleGrid4.get(p + 1, o) == 1 && !bl2) {
                            blockPos5 = blockPos4.relative(rotation.rotate(Direction.EAST), 8);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, direction2 == Direction.EAST ? string4 : string3, blockPos5, rotation));
                        }
                        if (MansionGrid.isHouse(simpleGrid4, p, o + 1) && !mansionGrid.isRoomId(simpleGrid4, p, o + 1, l, s)) {
                            blockPos5 = blockPos4.relative(rotation.rotate(Direction.SOUTH), 7);
                            blockPos5 = blockPos5.relative(rotation.rotate(Direction.EAST), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, direction2 == Direction.SOUTH ? string4 : string3, blockPos5, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                        if (simpleGrid4.get(p, o - 1) == 1 && !bl2) {
                            blockPos5 = blockPos4.relative(rotation.rotate(Direction.NORTH), 1);
                            blockPos5 = blockPos5.relative(rotation.rotate(Direction.EAST), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, direction2 == Direction.NORTH ? string4 : string3, blockPos5, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                        if (r == 65536) {
                            this.addRoom1x1(list, blockPos4, rotation, direction2, floorRoomCollections[l]);
                            continue;
                        }
                        if (r == 131072 && direction2 != null) {
                            direction3 = mansionGrid.get1x2RoomDirection(simpleGrid4, p, o, l, s);
                            boolean bl32 = (q & 0x400000) == 0x400000;
                            this.addRoom1x2(list, blockPos4, rotation, direction3, direction2, floorRoomCollections[l], bl32);
                            continue;
                        }
                        if (r == 262144 && direction2 != null && direction2 != Direction.UP) {
                            direction3 = direction2.getClockWise();
                            if (!mansionGrid.isRoomId(simpleGrid4, p + direction3.getStepX(), o + direction3.getStepZ(), l, s)) {
                                direction3 = direction3.getOpposite();
                            }
                            this.addRoom2x2(list, blockPos4, rotation, direction3, direction2, floorRoomCollections[l]);
                            continue;
                        }
                        if (r != 262144 || direction2 != Direction.UP) continue;
                        this.addRoom2x2Secret(list, blockPos4, rotation, floorRoomCollections[l]);
                    }
                }
            }
        }

        private void traverseOuterWalls(List<WoodlandMansionPiece> list, PlacementData placementData, SimpleGrid simpleGrid, Direction direction, int i, int j, int k, int l) {
            int m = i;
            int n = j;
            Direction direction2 = direction;
            do {
                if (!MansionGrid.isHouse(simpleGrid, m + direction.getStepX(), n + direction.getStepZ())) {
                    this.traverseTurn(list, placementData);
                    direction = direction.getClockWise();
                    if (m == k && n == l && direction2 == direction) continue;
                    this.traverseWallPiece(list, placementData);
                    continue;
                }
                if (MansionGrid.isHouse(simpleGrid, m + direction.getStepX(), n + direction.getStepZ()) && MansionGrid.isHouse(simpleGrid, m + direction.getStepX() + direction.getCounterClockWise().getStepX(), n + direction.getStepZ() + direction.getCounterClockWise().getStepZ())) {
                    this.traverseInnerTurn(list, placementData);
                    m += direction.getStepX();
                    n += direction.getStepZ();
                    direction = direction.getCounterClockWise();
                    continue;
                }
                if ((m += direction.getStepX()) == k && (n += direction.getStepZ()) == l && direction2 == direction) continue;
                this.traverseWallPiece(list, placementData);
            } while (m != k || n != l || direction2 != direction);
        }

        private void createRoof(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, SimpleGrid simpleGrid, @Nullable SimpleGrid simpleGrid2) {
            BlockPos blockPos3;
            boolean bl;
            BlockPos blockPos2;
            int j;
            int i;
            for (i = 0; i < simpleGrid.height; ++i) {
                for (j = 0; j < simpleGrid.width; ++j) {
                    blockPos2 = blockPos;
                    blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                    blockPos2 = blockPos2.relative(rotation.rotate(Direction.EAST), (j - this.startX) * 8);
                    boolean bl2 = bl = simpleGrid2 != null && MansionGrid.isHouse(simpleGrid2, j, i);
                    if (!MansionGrid.isHouse(simpleGrid, j, i) || bl) continue;
                    list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof", blockPos2.above(3), rotation));
                    if (!MansionGrid.isHouse(simpleGrid, j + 1, i)) {
                        blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 6);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos3, rotation));
                    }
                    if (!MansionGrid.isHouse(simpleGrid, j - 1, i)) {
                        blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 0);
                        blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 7);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_180)));
                    }
                    if (!MansionGrid.isHouse(simpleGrid, j, i - 1)) {
                        blockPos3 = blockPos2.relative(rotation.rotate(Direction.WEST), 1);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos3, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                    }
                    if (MansionGrid.isHouse(simpleGrid, j, i + 1)) continue;
                    blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 6);
                    blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 6);
                    list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_front", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_90)));
                }
            }
            if (simpleGrid2 != null) {
                for (i = 0; i < simpleGrid.height; ++i) {
                    for (j = 0; j < simpleGrid.width; ++j) {
                        blockPos2 = blockPos;
                        blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                        blockPos2 = blockPos2.relative(rotation.rotate(Direction.EAST), (j - this.startX) * 8);
                        bl = MansionGrid.isHouse(simpleGrid2, j, i);
                        if (!MansionGrid.isHouse(simpleGrid, j, i) || !bl) continue;
                        if (!MansionGrid.isHouse(simpleGrid, j + 1, i)) {
                            blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos3, rotation));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, j - 1, i)) {
                            blockPos3 = blockPos2.relative(rotation.rotate(Direction.WEST), 1);
                            blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 6);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_180)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, j, i - 1)) {
                            blockPos3 = blockPos2.relative(rotation.rotate(Direction.WEST), 0);
                            blockPos3 = blockPos3.relative(rotation.rotate(Direction.NORTH), 1);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos3, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, j, i + 1)) {
                            blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 6);
                            blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 7);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, j + 1, i)) {
                            if (!MansionGrid.isHouse(simpleGrid, j, i - 1)) {
                                blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 7);
                                blockPos3 = blockPos3.relative(rotation.rotate(Direction.NORTH), 2);
                                list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos3, rotation));
                            }
                            if (!MansionGrid.isHouse(simpleGrid, j, i + 1)) {
                                blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 8);
                                blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 7);
                                list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_90)));
                            }
                        }
                        if (MansionGrid.isHouse(simpleGrid, j - 1, i)) continue;
                        if (!MansionGrid.isHouse(simpleGrid, j, i - 1)) {
                            blockPos3 = blockPos2.relative(rotation.rotate(Direction.WEST), 2);
                            blockPos3 = blockPos3.relative(rotation.rotate(Direction.NORTH), 1);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos3, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        }
                        if (MansionGrid.isHouse(simpleGrid, j, i + 1)) continue;
                        blockPos3 = blockPos2.relative(rotation.rotate(Direction.WEST), 1);
                        blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 8);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "small_wall_corner", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_180)));
                    }
                }
            }
            for (i = 0; i < simpleGrid.height; ++i) {
                for (j = 0; j < simpleGrid.width; ++j) {
                    BlockPos blockPos4;
                    blockPos2 = blockPos;
                    blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
                    blockPos2 = blockPos2.relative(rotation.rotate(Direction.EAST), (j - this.startX) * 8);
                    boolean bl3 = bl = simpleGrid2 != null && MansionGrid.isHouse(simpleGrid2, j, i);
                    if (!MansionGrid.isHouse(simpleGrid, j, i) || bl) continue;
                    if (!MansionGrid.isHouse(simpleGrid, j + 1, i)) {
                        blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 6);
                        if (!MansionGrid.isHouse(simpleGrid, j, i + 1)) {
                            blockPos4 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 6);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos4, rotation));
                        } else if (MansionGrid.isHouse(simpleGrid, j + 1, i + 1)) {
                            blockPos4 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 5);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation));
                        }
                        if (!MansionGrid.isHouse(simpleGrid, j, i - 1)) {
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos3, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                        } else if (MansionGrid.isHouse(simpleGrid, j + 1, i - 1)) {
                            blockPos4 = blockPos2.relative(rotation.rotate(Direction.EAST), 9);
                            blockPos4 = blockPos4.relative(rotation.rotate(Direction.NORTH), 2);
                            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation.getRotated(Rotation.CLOCKWISE_90)));
                        }
                    }
                    if (MansionGrid.isHouse(simpleGrid, j - 1, i)) continue;
                    blockPos3 = blockPos2.relative(rotation.rotate(Direction.EAST), 0);
                    blockPos3 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 0);
                    if (!MansionGrid.isHouse(simpleGrid, j, i + 1)) {
                        blockPos4 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos4, rotation.getRotated(Rotation.CLOCKWISE_90)));
                    } else if (MansionGrid.isHouse(simpleGrid, j - 1, i + 1)) {
                        blockPos4 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 8);
                        blockPos4 = blockPos4.relative(rotation.rotate(Direction.WEST), 3);
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
                    }
                    if (!MansionGrid.isHouse(simpleGrid, j, i - 1)) {
                        list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_corner", blockPos3, rotation.getRotated(Rotation.CLOCKWISE_180)));
                        continue;
                    }
                    if (!MansionGrid.isHouse(simpleGrid, j - 1, i - 1)) continue;
                    blockPos4 = blockPos3.relative(rotation.rotate(Direction.SOUTH), 1);
                    list.add(new WoodlandMansionPiece(this.structureTemplateManager, "roof_inner_corner", blockPos4, rotation.getRotated(Rotation.CLOCKWISE_180)));
                }
            }
        }

        private void entrance(List<WoodlandMansionPiece> list, PlacementData placementData) {
            Direction direction = placementData.rotation.rotate(Direction.WEST);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "entrance", placementData.position.relative(direction, 9), placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 16);
        }

        private void traverseWallPiece(List<WoodlandMansionPiece> list, PlacementData placementData) {
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, placementData.wallType, placementData.position.relative(placementData.rotation.rotate(Direction.EAST), 7), placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 8);
        }

        private void traverseTurn(List<WoodlandMansionPiece> list, PlacementData placementData) {
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), -1);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, "wall_corner", placementData.position, placementData.rotation));
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), -7);
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.WEST), -6);
            placementData.rotation = placementData.rotation.getRotated(Rotation.CLOCKWISE_90);
        }

        private void traverseInnerTurn(List<WoodlandMansionPiece> list, PlacementData placementData) {
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.SOUTH), 6);
            placementData.position = placementData.position.relative(placementData.rotation.rotate(Direction.EAST), 8);
            placementData.rotation = placementData.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
        }

        private void addRoom1x1(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, FloorRoomCollection floorRoomCollection) {
            Rotation rotation2 = Rotation.NONE;
            String string = floorRoomCollection.get1x1(this.random);
            if (direction != Direction.EAST) {
                if (direction == Direction.NORTH) {
                    rotation2 = rotation2.getRotated(Rotation.COUNTERCLOCKWISE_90);
                } else if (direction == Direction.WEST) {
                    rotation2 = rotation2.getRotated(Rotation.CLOCKWISE_180);
                } else if (direction == Direction.SOUTH) {
                    rotation2 = rotation2.getRotated(Rotation.CLOCKWISE_90);
                } else {
                    string = floorRoomCollection.get1x1Secret(this.random);
                }
            }
            BlockPos blockPos2 = StructureTemplate.getZeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation2, 7, 7);
            rotation2 = rotation2.getRotated(rotation);
            blockPos2 = blockPos2.rotate(rotation);
            BlockPos blockPos3 = blockPos.offset(blockPos2.getX(), 0, blockPos2.getZ());
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, string, blockPos3, rotation2));
        }

        private void addRoom1x2(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, Direction direction2, FloorRoomCollection floorRoomCollection, boolean bl) {
            if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation));
            } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation, Mirror.LEFT_RIGHT));
            } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_180)));
            } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation, Mirror.FRONT_BACK));
            } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.LEFT_RIGHT));
            } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90), Mirror.FRONT_BACK));
            } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2SideEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
            } else if (direction2 == Direction.SOUTH && direction == Direction.NORTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.NORTH), 8);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos2, rotation));
            } else if (direction2 == Direction.NORTH && direction == Direction.SOUTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 7);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 14);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_180)));
            } else if (direction2 == Direction.WEST && direction == Direction.EAST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 15);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.EAST && direction == Direction.WEST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.WEST), 7);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), 6);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2FrontEntrance(this.random, bl), blockPos2, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)));
            } else if (direction2 == Direction.UP && direction == Direction.EAST) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 15);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2Secret(this.random), blockPos2, rotation.getRotated(Rotation.CLOCKWISE_90)));
            } else if (direction2 == Direction.UP && direction == Direction.SOUTH) {
                BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
                blockPos2 = blockPos2.relative(rotation.rotate(Direction.NORTH), 0);
                list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get1x2Secret(this.random), blockPos2, rotation));
            }
        }

        private void addRoom2x2(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, Direction direction, Direction direction2, FloorRoomCollection floorRoomCollection) {
            int i = 0;
            int j = 0;
            Rotation rotation2 = rotation;
            Mirror mirror = Mirror.NONE;
            if (direction2 == Direction.EAST && direction == Direction.SOUTH) {
                i = -7;
            } else if (direction2 == Direction.EAST && direction == Direction.NORTH) {
                i = -7;
                j = 6;
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.NORTH && direction == Direction.EAST) {
                i = 1;
                j = 14;
                rotation2 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (direction2 == Direction.NORTH && direction == Direction.WEST) {
                i = 7;
                j = 14;
                rotation2 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.SOUTH && direction == Direction.WEST) {
                i = 7;
                j = -8;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_90);
            } else if (direction2 == Direction.SOUTH && direction == Direction.EAST) {
                i = 1;
                j = -8;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_90);
                mirror = Mirror.LEFT_RIGHT;
            } else if (direction2 == Direction.WEST && direction == Direction.NORTH) {
                i = 15;
                j = 6;
                rotation2 = rotation.getRotated(Rotation.CLOCKWISE_180);
            } else if (direction2 == Direction.WEST && direction == Direction.SOUTH) {
                i = 15;
                mirror = Mirror.FRONT_BACK;
            }
            BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), i);
            blockPos2 = blockPos2.relative(rotation.rotate(Direction.SOUTH), j);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get2x2(this.random), blockPos2, rotation2, mirror));
        }

        private void addRoom2x2Secret(List<WoodlandMansionPiece> list, BlockPos blockPos, Rotation rotation, FloorRoomCollection floorRoomCollection) {
            BlockPos blockPos2 = blockPos.relative(rotation.rotate(Direction.EAST), 1);
            list.add(new WoodlandMansionPiece(this.structureTemplateManager, floorRoomCollection.get2x2Secret(this.random), blockPos2, rotation, Mirror.NONE));
        }
    }

    static class ThirdFloorRoomCollection
    extends SecondFloorRoomCollection {
        ThirdFloorRoomCollection() {
        }
    }

    static class SecondFloorRoomCollection
    extends FloorRoomCollection {
        SecondFloorRoomCollection() {
        }

        @Override
        public String get1x1(Random random) {
            return "1x1_b" + (random.nextInt(4) + 1);
        }

        @Override
        public String get1x1Secret(Random random) {
            return "1x1_as" + (random.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(Random random, boolean bl) {
            if (bl) {
                return "1x2_c_stairs";
            }
            return "1x2_c" + (random.nextInt(4) + 1);
        }

        @Override
        public String get1x2FrontEntrance(Random random, boolean bl) {
            if (bl) {
                return "1x2_d_stairs";
            }
            return "1x2_d" + (random.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(Random random) {
            return "1x2_se" + (random.nextInt(1) + 1);
        }

        @Override
        public String get2x2(Random random) {
            return "2x2_b" + (random.nextInt(5) + 1);
        }

        @Override
        public String get2x2Secret(Random random) {
            return "2x2_s1";
        }
    }

    static class FirstFloorRoomCollection
    extends FloorRoomCollection {
        FirstFloorRoomCollection() {
        }

        @Override
        public String get1x1(Random random) {
            return "1x1_a" + (random.nextInt(5) + 1);
        }

        @Override
        public String get1x1Secret(Random random) {
            return "1x1_as" + (random.nextInt(4) + 1);
        }

        @Override
        public String get1x2SideEntrance(Random random, boolean bl) {
            return "1x2_a" + (random.nextInt(9) + 1);
        }

        @Override
        public String get1x2FrontEntrance(Random random, boolean bl) {
            return "1x2_b" + (random.nextInt(5) + 1);
        }

        @Override
        public String get1x2Secret(Random random) {
            return "1x2_s" + (random.nextInt(2) + 1);
        }

        @Override
        public String get2x2(Random random) {
            return "2x2_a" + (random.nextInt(4) + 1);
        }

        @Override
        public String get2x2Secret(Random random) {
            return "2x2_s1";
        }
    }

    static abstract class FloorRoomCollection {
        FloorRoomCollection() {
        }

        public abstract String get1x1(Random var1);

        public abstract String get1x1Secret(Random var1);

        public abstract String get1x2SideEntrance(Random var1, boolean var2);

        public abstract String get1x2FrontEntrance(Random var1, boolean var2);

        public abstract String get1x2Secret(Random var1);

        public abstract String get2x2(Random var1);

        public abstract String get2x2Secret(Random var1);
    }

    static class SimpleGrid {
        private final int[][] grid;
        final int width;
        final int height;
        private final int valueIfOutside;

        public SimpleGrid(int i, int j, int k) {
            this.width = i;
            this.height = j;
            this.valueIfOutside = k;
            this.grid = new int[i][j];
        }

        public void set(int i, int j, int k) {
            if (i >= 0 && i < this.width && j >= 0 && j < this.height) {
                this.grid[i][j] = k;
            }
        }

        public void set(int i, int j, int k, int l, int m) {
            for (int n = j; n <= l; ++n) {
                for (int o = i; o <= k; ++o) {
                    this.set(o, n, m);
                }
            }
        }

        public int get(int i, int j) {
            if (i >= 0 && i < this.width && j >= 0 && j < this.height) {
                return this.grid[i][j];
            }
            return this.valueIfOutside;
        }

        public void setif(int i, int j, int k, int l) {
            if (this.get(i, j) == k) {
                this.set(i, j, l);
            }
        }

        public boolean edgesTo(int i, int j, int k) {
            return this.get(i - 1, j) == k || this.get(i + 1, j) == k || this.get(i, j + 1) == k || this.get(i, j - 1) == k;
        }
    }

    static class PlacementData {
        public Rotation rotation;
        public BlockPos position;
        public String wallType;

        PlacementData() {
        }
    }

    public static class WoodlandMansionPiece
    extends TemplateStructurePiece {
        public WoodlandMansionPiece(StructureTemplateManager structureTemplateManager, String string, BlockPos blockPos, Rotation rotation) {
            this(structureTemplateManager, string, blockPos, rotation, Mirror.NONE);
        }

        public WoodlandMansionPiece(StructureTemplateManager structureTemplateManager, String string, BlockPos blockPos, Rotation rotation, Mirror mirror) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, 0, structureTemplateManager, WoodlandMansionPiece.makeLocation(string), string, WoodlandMansionPiece.makeSettings(mirror, rotation), blockPos);
        }

        public WoodlandMansionPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
            super(StructurePieceType.WOODLAND_MANSION_PIECE, compoundTag, structureTemplateManager, (ResourceLocation resourceLocation) -> WoodlandMansionPiece.makeSettings(Mirror.valueOf(compoundTag.getString("Mi")), Rotation.valueOf(compoundTag.getString("Rot"))));
        }

        @Override
        protected ResourceLocation makeTemplateLocation() {
            return WoodlandMansionPiece.makeLocation(this.templateName);
        }

        private static ResourceLocation makeLocation(String string) {
            return new ResourceLocation("woodland_mansion/" + string);
        }

        private static StructurePlaceSettings makeSettings(Mirror mirror, Rotation rotation) {
            return new StructurePlaceSettings().setIgnoreEntities(true).setRotation(rotation).setMirror(mirror).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putString("Rot", this.placeSettings.getRotation().name());
            compoundTag.putString("Mi", this.placeSettings.getMirror().name());
        }

        @Override
        protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, Random random, BoundingBox boundingBox) {
            if (string.startsWith("Chest")) {
                Rotation rotation = this.placeSettings.getRotation();
                BlockState blockState = Blocks.CHEST.defaultBlockState();
                if ("ChestWest".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.WEST));
                } else if ("ChestEast".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.EAST));
                } else if ("ChestSouth".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.SOUTH));
                } else if ("ChestNorth".equals(string)) {
                    blockState = (BlockState)blockState.setValue(ChestBlock.FACING, rotation.rotate(Direction.NORTH));
                }
                this.createChest(serverLevelAccessor, boundingBox, random, blockPos, BuiltInLootTables.WOODLAND_MANSION, blockState);
            } else {
                AbstractIllager abstractIllager;
                switch (string) {
                    case "Mage": {
                        abstractIllager = EntityType.EVOKER.create(serverLevelAccessor.getLevel());
                        break;
                    }
                    case "Warrior": {
                        abstractIllager = EntityType.VINDICATOR.create(serverLevelAccessor.getLevel());
                        break;
                    }
                    default: {
                        return;
                    }
                }
                abstractIllager.setPersistenceRequired();
                abstractIllager.moveTo(blockPos, 0.0f, 0.0f);
                abstractIllager.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(abstractIllager.blockPosition()), MobSpawnType.STRUCTURE, null, null);
                serverLevelAccessor.addFreshEntityWithPassengers(abstractIllager);
                serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }
}

