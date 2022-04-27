/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
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

    public static void drawWithShader(BufferBuilder.RenderedBuffer renderedBuffer) {
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> BufferUploader._drawWithShader(renderedBuffer));
        } else {
            BufferUploader._drawWithShader(renderedBuffer);
        }
    }

    private static void _drawWithShader(BufferBuilder.RenderedBuffer renderedBuffer) {
        VertexBuffer vertexBuffer = BufferUploader.upload(renderedBuffer);
        if (vertexBuffer != null) {
            vertexBuffer.drawWithShader(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(), RenderSystem.getShader());
        }
    }

    public static void draw(BufferBuilder.RenderedBuffer renderedBuffer) {
        VertexBuffer vertexBuffer = BufferUploader.upload(renderedBuffer);
        if (vertexBuffer != null) {
            vertexBuffer.draw();
        }
    }

    @Nullable
    private static VertexBuffer upload(BufferBuilder.RenderedBuffer renderedBuffer) {
        RenderSystem.assertOnRenderThread();
        if (renderedBuffer.isEmpty()) {
            renderedBuffer.release();
            return null;
        }
        VertexBuffer vertexBuffer = BufferUploader.bindImmediateBuffer(renderedBuffer.drawState().format());
        vertexBuffer.upload(renderedBuffer);
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

