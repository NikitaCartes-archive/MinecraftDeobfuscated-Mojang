/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.entity;

import net.minecraft.server.level.ChunkHolder;

public enum Visibility {
    HIDDEN(false, false),
    TRACKED(true, false),
    TICKING(true, true);

    private final boolean accessible;
    private final boolean ticking;

    private Visibility(boolean bl, boolean bl2) {
        this.accessible = bl;
        this.ticking = bl2;
    }

    public boolean isTicking() {
        return this.ticking;
    }

    public boolean isAccessible() {
        return this.accessible;
    }

    public static Visibility fromFullChunkStatus(ChunkHolder.FullChunkStatus fullChunkStatus) {
        if (fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.ENTITY_TICKING)) {
            return TICKING;
        }
        if (fullChunkStatus.isOrAfter(ChunkHolder.FullChunkStatus.BORDER)) {
            return TRACKED;
        }
        return HIDDEN;
    }
}

