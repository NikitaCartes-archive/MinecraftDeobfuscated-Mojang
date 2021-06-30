/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import net.minecraft.Util;
import net.minecraft.util.VisibleForDebug;
import org.jetbrains.annotations.Nullable;

public final class DataLayer {
    public static final int LAYER_COUNT = 16;
    public static final int LAYER_SIZE = 128;
    public static final int SIZE = 2048;
    private static final int NIBBLE_SIZE = 4;
    @Nullable
    protected byte[] data;

    public DataLayer() {
    }

    public DataLayer(byte[] bs) {
        this.data = bs;
        if (bs.length != 2048) {
            throw Util.pauseInIde(new IllegalArgumentException("DataLayer should be 2048 bytes not: " + bs.length));
        }
    }

    protected DataLayer(int i) {
        this.data = new byte[i];
    }

    public int get(int i, int j, int k) {
        return this.get(DataLayer.getIndex(i, j, k));
    }

    public void set(int i, int j, int k, int l) {
        this.set(DataLayer.getIndex(i, j, k), l);
    }

    private static int getIndex(int i, int j, int k) {
        return j << 8 | k << 4 | i;
    }

    private int get(int i) {
        if (this.data == null) {
            return 0;
        }
        int j = DataLayer.getByteIndex(i);
        int k = DataLayer.getNibbleIndex(i);
        return this.data[j] >> 4 * k & 0xF;
    }

    private void set(int i, int j) {
        if (this.data == null) {
            this.data = new byte[2048];
        }
        int k = DataLayer.getByteIndex(i);
        int l = DataLayer.getNibbleIndex(i);
        int m = ~(15 << 4 * l);
        int n = (j & 0xF) << 4 * l;
        this.data[k] = (byte)(this.data[k] & m | n);
    }

    private static int getNibbleIndex(int i) {
        return i & 1;
    }

    private static int getByteIndex(int i) {
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

    @VisibleForDebug
    public String layerToString(int i) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int j = 0; j < 256; ++j) {
            stringBuilder.append(Integer.toHexString(this.get(j)));
            if ((j & 0xF) != 15) continue;
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public boolean isEmpty() {
        return this.data == null;
    }
}

