/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import net.minecraft.Util;
import org.jetbrains.annotations.Nullable;

public class DataLayer {
    @Nullable
    protected byte[] data;

    public DataLayer() {
    }

    public DataLayer(byte[] bs) {
        this.data = bs;
        if (bs.length != 2048) {
            throw Util.pauseInIde(new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + bs.length));
        }
    }

    protected DataLayer(int i) {
        this.data = new byte[i];
    }

    public int get(int i, int j, int k) {
        return this.get(this.getIndex(i, j, k));
    }

    public void set(int i, int j, int k, int l) {
        this.set(this.getIndex(i, j, k), l);
    }

    protected int getIndex(int i, int j, int k) {
        return j << 8 | k << 4 | i;
    }

    private int get(int i) {
        if (this.data == null) {
            return 0;
        }
        int j = this.getPosition(i);
        if (this.isFirst(i)) {
            return this.data[j] & 0xF;
        }
        return this.data[j] >> 4 & 0xF;
    }

    private void set(int i, int j) {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        int k = this.getPosition(i);
        this.data[k] = this.isFirst(i) ? (byte)(this.data[k] & 0xF0 | j & 0xF) : (byte)(this.data[k] & 0xF | (j & 0xF) << 4);
    }

    private boolean isFirst(int i) {
        return (i & 1) == 0;
    }

    private int getPosition(int i) {
        return i >> 1;
    }

    public byte[] getData() {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        return this.data;
    }

    public DataLayer copy() {
        if (this.data == null) {
            return new DataLayer();
        }
        return new DataLayer((byte[])this.data.clone());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 4096; ++i) {
            stringBuilder.append(Integer.toHexString(this.get(i)));
            if ((i & 0xF) == 15) {
                stringBuilder.append("\n");
            }
            if ((i & 0xFF) != 255) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return this.data == null;
    }
}

