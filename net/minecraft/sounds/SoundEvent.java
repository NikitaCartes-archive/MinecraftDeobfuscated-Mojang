/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.sounds;

import net.minecraft.resources.ResourceLocation;

public class SoundEvent {
    private static final float DEFAULT_RANGE = 16.0f;
    private final ResourceLocation location;
    private final float range;
    private final boolean newSystem;

    static SoundEvent createVariableRangeEvent(ResourceLocation resourceLocation) {
        return new SoundEvent(resourceLocation, 16.0f, false);
    }

    static SoundEvent createFixedRangeEvent(ResourceLocation resourceLocation, float f) {
        return new SoundEvent(resourceLocation, f, true);
    }

    private SoundEvent(ResourceLocation resourceLocation, float f, boolean bl) {
        this.location = resourceLocation;
        this.range = f;
        this.newSystem = bl;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public float getRange(float f) {
        if (this.newSystem) {
            return this.range;
        }
        return SoundEvent.legacySoundRange(f);
    }

    public static float legacySoundRange(float f) {
        return f > 1.0f ? 16.0f * f : 16.0f;
    }
}

