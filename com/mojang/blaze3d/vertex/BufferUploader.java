/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BufferUploader {
    @Nullable
    private static VertexBuffer lastImmediateBuffer;

    public static void reset() {
        if (lastImmediateBuffer != null) {
            BufferUploader.invalidate();
            VertexBuffer.unbind();
        }
    }

    public static void invalidate() {
        lastImmediateBuffer = null;
    }

    public static void drawWithShader(BufferBuilder bufferBuilder) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> BufferUploader._drawWithShader(bufferBuilder));
        } else {
            BufferUploader._drawWithShader(bufferBuilder);
        }
    }

    private static void _drawWithShader(BufferBuilder bufferBuilder) {
        VertexBuffer vertexBuffer = BufferUploader.upload(bufferBuilder);
        if (vertexBuffer != null) {
            vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
    }

    public static void draw(BufferBuilder bufferBuilder) {
        VertexBuffer vertexBuffer = BufferUploader.upload(bufferBuilder);
        if (vertexBuffer != null) {
            vertexBuffer.draw();
        }
    }

    @Nullable
    private static VertexBuffer upload(BufferBuilder bufferBuilder) {
        RenderSystem.assertOnRenderThread();
        Pair<BufferBuilder.DrawState, ByteBuffer> pair = bufferBuilder.popNextBuffer();
        BufferBuilder.DrawState drawState = pair.getFirst();
        ByteBuffer byteBuffer = pair.getSecond();
        byteBuffer.clear();
        if (drawState.vertexCount() <= 0) {
            return null;
        }
        VertexBuffer vertexBuffer = BufferUploader.bindImmediateBuffer(drawState.format());
        vertexBuffer.upload(drawState, byteBuffer);
        return vertexBuffer;
    }

    private static VertexBuffer bindImmediateBuffer(VertexFormat vertexFormat) {
        VertexBuffer vertexBuffer = vertexFormat.getImmediateDrawVertexBuffer();
        BufferUploader.bindImmediateBuffer(vertexBuffer);
        return vertexBuffer;
    }

    private static void bindImmediateBuffer(VertexBuffer vertexBuffer) {
        if (vertexBuffer != lastImmediateBuffer) {
            vertexBuffer.bind();
            lastImmediateBuffer = vertexBuffer;
        }
    }
}

