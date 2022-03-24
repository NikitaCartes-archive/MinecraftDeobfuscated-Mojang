/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.level;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public record ColumnPos(int x, int z) {
    private static final long COORD_BITS = 32L;
    private static final long COORD_MASK = 0xFFFFFFFFL;

    public ChunkPos toChunkPos() {
        return new ChunkPos(SectionPos.blockToSectionCoord(this.x), SectionPos.blockToSectionCoord(this.z));
    }

    public long toLong() {
        return ColumnPos.asLong(this.x, this.z);
    }

    public static long asLong(int i, int j) {
        return (long)i & 0xFFFFFFFFL | ((long)j & 0xFFFFFFFFL) << 32;
    }

    public static int getX(long l) {
        return (int)(l & 0xFFFFFFFFL);
    }

    public static int getZ(long l) {
        return (int)(l >>> 32 & 0xFFFFFFFFL);
    }

    @Override
    public String toString() {
        return "[" + this.x + ", " + this.z + "]";
    }

    @Override
    public int hashCode() {
        return ChunkPos.hash(this.x, this.z);
    }
}

