/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class DebugBuffer<T> {
    private final AtomicReferenceArray<T> data;
    private final AtomicInteger index;

    public DebugBuffer(int i) {
        this.data = new AtomicReferenceArray(i);
        this.index = new AtomicInteger(0);
    }

    public void push(T object) {
        int k;
        int j;
        int i = this.data.length();
        while (!this.index.compareAndSet(j = this.index.get(), k = (j + 1) % i)) {
        }
        this.data.set(k, object);
    }

    public List<T> dump() {
        int i = this.index.get();
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int j = 0; j < this.data.length(); ++j) {
            int k = Math.floorMod(i - j, this.data.length());
            T object = this.data.get(k);
            if (object == null) continue;
            builder.add(object);
        }
        return builder.build();
    }
}

