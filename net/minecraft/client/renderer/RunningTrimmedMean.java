/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class RunningTrimmedMean {
    private final long[] values;
    private int count;
    private int cursor;

    public RunningTrimmedMean(int i) {
        this.values = new long[i];
    }

    public long registerValueAndGetMean(long l) {
        if (this.count < this.values.length) {
            ++this.count;
        }
        this.values[this.cursor] = l;
        this.cursor = (this.cursor + 1) % this.values.length;
        long m = Long.MAX_VALUE;
        long n = Long.MIN_VALUE;
        long o = 0L;
        for (int i = 0; i < this.count; ++i) {
            long p = this.values[i];
            o += p;
            m = Math.min(m, p);
            n = Math.max(n, p);
        }
        if (this.count > 2) {
            return (o -= m + n) / (long)(this.count - 2);
        }
        if (o > 0L) {
            return (long)this.count / o;
        }
        return 0L;
    }
}

