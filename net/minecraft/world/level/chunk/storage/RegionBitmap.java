/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk.storage;

import java.util.BitSet;

public class RegionBitmap {
    private final BitSet used = new BitSet();

    public void force(int i, int j) {
        this.used.set(i, i + j);
    }

    public void free(int i, int j) {
        this.used.clear(i, i + j);
    }

    public int allocate(int i) {
        int j = 0;
        while (true) {
            int k;
            int l;
            if ((l = this.used.nextSetBit(k = this.used.nextClearBit(j))) == -1 || l - k >= i) {
                this.force(k, i);
                return k;
            }
            j = l;
        }
    }
}

