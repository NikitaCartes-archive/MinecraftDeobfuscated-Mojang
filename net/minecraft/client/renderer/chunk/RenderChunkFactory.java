/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public interface RenderChunkFactory {
    public RenderChunk create(Level var1, LevelRenderer var2);
}

