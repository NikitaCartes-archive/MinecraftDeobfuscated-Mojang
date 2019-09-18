/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.system.MemoryUtil;

@Environment(value=EnvType.CLIENT)
public class BufferUploader {
    public static void end(BufferBuilder bufferBuilder) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> {
                Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
                BufferBuilder.DrawState drawState = pair.getFirst();
                BufferUploader._end(pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount());
            });
        } else {
            Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
            BufferBuilder.DrawState drawState = pair.getFirst();
            BufferUploader._end(pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount());
        }
    }

    public static void end(ByteBuffer byteBuffer, int i, VertexFormat vertexFormat, int j) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> BufferUploader._end(byteBuffer, i, vertexFormat, j));
        } else {
            BufferUploader._end(byteBuffer, i, vertexFormat, j);
        }
    }

    private static void _end(ByteBuffer byteBuffer, int i, VertexFormat vertexFormat, int j) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        byteBuffer.clear();
        if (j <= 0) {
            return;
        }
        vertexFormat.setupBufferState(MemoryUtil.memAddress(byteBuffer));
        GlStateManager._drawArrays(i, 0, j);
        vertexFormat.clearBufferState();
    }
}

