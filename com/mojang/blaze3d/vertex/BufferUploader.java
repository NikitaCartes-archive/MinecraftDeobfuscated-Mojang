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

@Environment(value=EnvType.CLIENT)
public class BufferUploader {
    private static int vertexBufferObject;
    private static int indexBufferObject;

    public static void end(BufferBuilder bufferBuilder) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
                BufferBuilder.DrawState drawState = pair.getFirst();
                BufferUploader._end(pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount(), drawState.indexType(), drawState.indexCount(), drawState.sequentialIndex());
            });
        } else {
            Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
            BufferBuilder.DrawState drawState = pair.getFirst();
            BufferUploader._end(pair.getSecond(), drawState.mode(), drawState.format(), drawState.vertexCount(), drawState.indexType(), drawState.indexCount(), drawState.sequentialIndex());
        }
    }

    private static void _end(ByteBuffer byteBuffer, VertexFormat.Mode mode, VertexFormat vertexFormat, int i, VertexFormat.IndexType indexType, int j, boolean bl) {
        int l;
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        byteBuffer.clear();
        if (i <= 0) {
            return;
        }
        if (vertexBufferObject == 0) {
            vertexBufferObject = GlStateManager._glGenBuffers();
        }
        int k = i * vertexFormat.getVertexSize();
        GlStateManager._glBindBuffer(34962, vertexBufferObject);
        byteBuffer.position(0);
        byteBuffer.limit(k);
        GlStateManager._glBufferData(34962, byteBuffer, 35044);
        if (bl) {
            RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(mode, j);
            GlStateManager._glBindBuffer(34963, autoStorageIndexBuffer.name());
            l = autoStorageIndexBuffer.type().asGLType;
        } else {
            if (indexBufferObject == 0) {
                indexBufferObject = GlStateManager._glGenBuffers();
            }
            GlStateManager._glBindBuffer(34963, indexBufferObject);
            byteBuffer.position(k);
            byteBuffer.limit(k + j * indexType.bytes);
            GlStateManager._glBufferData(34963, byteBuffer, 35044);
            l = indexType.asGLType;
        }
        vertexFormat.setupBufferState(0L);
        GlStateManager._drawElements(mode.asGLMode, j, l, 0L);
        vertexFormat.clearBufferState();
        byteBuffer.position(0);
        GlStateManager._glBindBuffer(34963, 0);
        GlStateManager._glBindBuffer(34962, 0);
    }
}

