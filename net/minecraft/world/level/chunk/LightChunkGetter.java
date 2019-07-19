/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.chunk;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.Nullable;

public interface LightChunkGetter {
    @Nullable
    public BlockGetter getChunkForLighting(int var1, int var2);

    default public void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
    }

    public BlockGetter getLevel();
}

