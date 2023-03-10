/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;

public class NbtAccounter {
    public static final NbtAccounter UNLIMITED = new NbtAccounter(0L){

        @Override
        public void accountBytes(long l) {
        }
    };
    private final long quota;
    private long usage;

    public NbtAccounter(long l) {
        this.quota = l;
    }

    public void accountBytes(long l) {
        this.usage += l;
        if (this.usage > this.quota) {
            throw new RuntimeException("Tried to read NBT tag that was too big; tried to allocate: " + this.usage + "bytes where max allowed: " + this.quota);
        }
    }

    @VisibleForTesting
    public long getUsage() {
        return this.usage;
    }
}

