/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

public class ForcedChunksSavedData
extends SavedData {
    public static final String FILE_ID = "chunks";
    private static final String TAG_FORCED = "Forced";
    private final LongSet chunks;

    private ForcedChunksSavedData(LongSet longSet) {
        this.chunks = longSet;
    }

    public ForcedChunksSavedData() {
        this(new LongOpenHashSet());
    }

    public static ForcedChunksSavedData load(CompoundTag compoundTag) {
        return new ForcedChunksSavedData(new LongOpenHashSet(compoundTag.getLongArray(TAG_FORCED)));
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.putLongArray(TAG_FORCED, this.chunks.toLongArray());
        return compoundTag;
    }

    public LongSet getChunks() {
        return this.chunks;
    }
}

