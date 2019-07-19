/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.platform.MemoryTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.CompiledChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.Level;

@Environment(value=EnvType.CLIENT)
public class ListedRenderChunk
extends RenderChunk {
    private final int listId = MemoryTracker.genLists(BlockLayer.values().length);

    public ListedRenderChunk(Level level, LevelRenderer levelRenderer) {
        super(level, levelRenderer);
    }

    public int getGlListId(BlockLayer blockLayer, CompiledChunk compiledChunk) {
        if (!compiledChunk.isEmpty(blockLayer)) {
            return this.listId + blockLayer.ordinal();
        }
        return -1;
    }

    @Override
    public void releaseBuffers() {
        super.releaseBuffers();
        MemoryTracker.releaseLists(this.listId, BlockLayer.values().length);
    }
}

