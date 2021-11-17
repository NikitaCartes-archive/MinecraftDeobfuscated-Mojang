/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import java.util.BitSet;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class CarvingMask {
    private final int minY;
    private final BitSet mask;
    private Mask additionalMask = (i, j, k) -> false;

    public CarvingMask(int i2, int j2) {
        this.minY = j2;
        this.mask = new BitSet(256 * i2);
    }

    public void setAdditionalMask(Mask mask) {
        this.additionalMask = mask;
    }

    public CarvingMask(long[] ls, int i2) {
        this.minY = i2;
        this.mask = BitSet.valueOf(ls);
    }

    private int getIndex(int i, int j, int k) {
        return i & 0xF | (k & 0xF) << 4 | j - this.minY << 8;
    }

    public void set(int i, int j, int k) {
        this.mask.set(this.getIndex(i, j, k));
    }

    public boolean get(int i, int j, int k) {
        return this.additionalMask.test(i, j, k) || this.mask.get(this.getIndex(i, j, k));
    }

    public Stream<BlockPos> stream(ChunkPos chunkPos) {
        return this.mask.stream().mapToObj(i -> {
            int j = i & 0xF;
            int k = i >> 4 & 0xF;
            int l = i >> 8;
            return chunkPos.getBlockAt(j, l + this.minY, k);
        });
    }

    public long[] toArray() {
        return this.mask.toLongArray();
    }

    public static interface Mask {
        public boolean test(int var1, int var2, int var3);
    }
}

