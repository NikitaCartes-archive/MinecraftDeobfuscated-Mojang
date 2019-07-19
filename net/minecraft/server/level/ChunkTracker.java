/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.lighting.DynamicGraphMinFixedPoint;

public abstract class ChunkTracker
extends DynamicGraphMinFixedPoint {
    protected ChunkTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @Override
    protected boolean isSource(long l) {
        return l == ChunkPos.INVALID_CHUNK_POS;
    }

    @Override
    protected void checkNeighborsAfterUpdate(long l, int i, boolean bl) {
        ChunkPos chunkPos = new ChunkPos(l);
        int j = chunkPos.x;
        int k = chunkPos.z;
        for (int m = -1; m <= 1; ++m) {
            for (int n = -1; n <= 1; ++n) {
                long o = ChunkPos.asLong(j + m, k + n);
                if (o == l) continue;
                this.checkNeighbor(l, o, i, bl);
            }
        }
    }

    @Override
    protected int getComputedLevel(long l, long m, int i) {
        int j = i;
        ChunkPos chunkPos = new ChunkPos(l);
        int k = chunkPos.x;
        int n = chunkPos.z;
        for (int o = -1; o <= 1; ++o) {
            for (int p = -1; p <= 1; ++p) {
                long q = ChunkPos.asLong(k + o, n + p);
                if (q == l) {
                    q = ChunkPos.INVALID_CHUNK_POS;
                }
                if (q == m) continue;
                int r = this.computeLevelFromNeighbor(q, l, this.getLevel(q));
                if (j > r) {
                    j = r;
                }
                if (j != 0) continue;
                return j;
            }
        }
        return j;
    }

    @Override
    protected int computeLevelFromNeighbor(long l, long m, int i) {
        if (l == ChunkPos.INVALID_CHUNK_POS) {
            return this.getLevelFromSource(m);
        }
        return i + 1;
    }

    protected abstract int getLevelFromSource(long var1);

    public void update(long l, int i, boolean bl) {
        this.checkEdge(ChunkPos.INVALID_CHUNK_POS, l, i, bl);
    }
}

