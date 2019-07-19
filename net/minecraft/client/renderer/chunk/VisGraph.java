/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.chunk.VisibilitySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

@Environment(value=EnvType.CLIENT)
public class VisGraph {
    private static final int DX = (int)Math.pow(16.0, 0.0);
    private static final int DZ = (int)Math.pow(16.0, 1.0);
    private static final int DY = (int)Math.pow(16.0, 2.0);
    private static final Direction[] DIRECTIONS = Direction.values();
    private final BitSet bitSet = new BitSet(4096);
    private static final int[] INDEX_OF_EDGES = Util.make(new int[1352], is -> {
        boolean i = false;
        int j = 15;
        int k = 0;
        for (int l = 0; l < 16; ++l) {
            for (int m = 0; m < 16; ++m) {
                for (int n = 0; n < 16; ++n) {
                    if (l != 0 && l != 15 && m != 0 && m != 15 && n != 0 && n != 15) continue;
                    is[k++] = VisGraph.getIndex(l, m, n);
                }
            }
        }
    });
    private int empty = 4096;

    public void setOpaque(BlockPos blockPos) {
        this.bitSet.set(VisGraph.getIndex(blockPos), true);
        --this.empty;
    }

    private static int getIndex(BlockPos blockPos) {
        return VisGraph.getIndex(blockPos.getX() & 0xF, blockPos.getY() & 0xF, blockPos.getZ() & 0xF);
    }

    private static int getIndex(int i, int j, int k) {
        return i << 0 | j << 8 | k << 4;
    }

    public VisibilitySet resolve() {
        VisibilitySet visibilitySet = new VisibilitySet();
        if (4096 - this.empty < 256) {
            visibilitySet.setAll(true);
        } else if (this.empty == 0) {
            visibilitySet.setAll(false);
        } else {
            for (int i : INDEX_OF_EDGES) {
                if (this.bitSet.get(i)) continue;
                visibilitySet.add(this.floodFill(i));
            }
        }
        return visibilitySet;
    }

    public Set<Direction> floodFill(BlockPos blockPos) {
        return this.floodFill(VisGraph.getIndex(blockPos));
    }

    private Set<Direction> floodFill(int i) {
        EnumSet<Direction> set = EnumSet.noneOf(Direction.class);
        IntArrayFIFOQueue intPriorityQueue = new IntArrayFIFOQueue();
        intPriorityQueue.enqueue(i);
        this.bitSet.set(i, true);
        while (!intPriorityQueue.isEmpty()) {
            int j = intPriorityQueue.dequeueInt();
            this.addEdges(j, set);
            for (Direction direction : DIRECTIONS) {
                int k = this.getNeighborIndexAtFace(j, direction);
                if (k < 0 || this.bitSet.get(k)) continue;
                this.bitSet.set(k, true);
                intPriorityQueue.enqueue(k);
            }
        }
        return set;
    }

    private void addEdges(int i, Set<Direction> set) {
        int j = i >> 0 & 0xF;
        if (j == 0) {
            set.add(Direction.WEST);
        } else if (j == 15) {
            set.add(Direction.EAST);
        }
        int k = i >> 8 & 0xF;
        if (k == 0) {
            set.add(Direction.DOWN);
        } else if (k == 15) {
            set.add(Direction.UP);
        }
        int l = i >> 4 & 0xF;
        if (l == 0) {
            set.add(Direction.NORTH);
        } else if (l == 15) {
            set.add(Direction.SOUTH);
        }
    }

    private int getNeighborIndexAtFace(int i, Direction direction) {
        switch (direction) {
            case DOWN: {
                if ((i >> 8 & 0xF) == 0) {
                    return -1;
                }
                return i - DY;
            }
            case UP: {
                if ((i >> 8 & 0xF) == 15) {
                    return -1;
                }
                return i + DY;
            }
            case NORTH: {
                if ((i >> 4 & 0xF) == 0) {
                    return -1;
                }
                return i - DZ;
            }
            case SOUTH: {
                if ((i >> 4 & 0xF) == 15) {
                    return -1;
                }
                return i + DZ;
            }
            case WEST: {
                if ((i >> 0 & 0xF) == 0) {
                    return -1;
                }
                return i - DX;
            }
            case EAST: {
                if ((i >> 0 & 0xF) == 15) {
                    return -1;
                }
                return i + DX;
            }
        }
        return -1;
    }
}

