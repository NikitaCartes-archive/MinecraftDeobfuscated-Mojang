/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import java.util.function.IntConsumer;
import net.minecraft.util.BitStorage;
import org.apache.commons.lang3.Validate;

public class ZeroBitStorage
implements BitStorage {
    public static final long[] RAW = new long[0];
    private final int size;

    public ZeroBitStorage(int i) {
        this.size = i;
    }

    @Override
    public int getAndSet(int i, int j) {
        Validate.inclusiveBetween(0L, this.size - 1, i);
        Validate.inclusiveBetween(0L, 0L, j);
        return 0;
    }

    @Override
    public void set(int i, int j) {
        Validate.inclusiveBetween(0L, this.size - 1, i);
        Validate.inclusiveBetween(0L, 0L, j);
    }

    @Override
    public int get(int i) {
        Validate.inclusiveBetween(0L, this.size - 1, i);
        return 0;
    }

    @Override
    public long[] getRaw() {
        return RAW;
    }

    @Override
    public int getSize() {
        return this.size;
    }

    @Override
    public int getBits() {
        return 0;
    }

    @Override
    public void getAll(IntConsumer intConsumer) {
        for (int i = 0; i < this.size; ++i) {
            intConsumer.accept(0);
        }
    }
}

