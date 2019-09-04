/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class BufferUploader {
    public void end(BufferBuilder bufferBuilder) {
        if (bufferBuilder.getVertexCount() > 0) {
            int l;
            int j;
            VertexFormat vertexFormat = bufferBuilder.getVertexFormat();
            int i = vertexFormat.getVertexSize();
            ByteBuffer byteBuffer = bufferBuilder.getBuffer();
            List<VertexFormatElement> list = vertexFormat.getElements();
            block12: for (j = 0; j < list.size(); ++j) {
                VertexFormatElement vertexFormatElement = list.get(j);
                VertexFormatElement.Usage usage = vertexFormatElement.getUsage();
                int k = vertexFormatElement.getType().getGlType();
                l = vertexFormatElement.getIndex();
                byteBuffer.position(vertexFormat.getOffset(j));
                switch (usage) {
                    case POSITION: {
                        RenderSystem.vertexPointer(vertexFormatElement.getCount(), k, i, byteBuffer);
                        RenderSystem.enableClientState(32884);
                        continue block12;
                    }
                    case UV: {
                        RenderSystem.glClientActiveTexture(33984 + l);
                        RenderSystem.texCoordPointer(vertexFormatElement.getCount(), k, i, byteBuffer);
                        RenderSystem.enableClientState(32888);
                        RenderSystem.glClientActiveTexture(33984);
                        continue block12;
                    }
                    case COLOR: {
                        RenderSystem.colorPointer(vertexFormatElement.getCount(), k, i, byteBuffer);
                        RenderSystem.enableClientState(32886);
                        continue block12;
                    }
                    case NORMAL: {
                        RenderSystem.normalPointer(k, i, byteBuffer);
                        RenderSystem.enableClientState(32885);
                    }
                }
            }
            RenderSystem.drawArrays(bufferBuilder.getDrawMode(), 0, bufferBuilder.getVertexCount());
            int m = list.size();
            block13: for (j = 0; j < m; ++j) {
                VertexFormatElement vertexFormatElement2 = list.get(j);
                VertexFormatElement.Usage usage2 = vertexFormatElement2.getUsage();
                l = vertexFormatElement2.getIndex();
                switch (usage2) {
                    case POSITION: {
                        RenderSystem.disableClientState(32884);
                        continue block13;
                    }
                    case UV: {
                        RenderSystem.glClientActiveTexture(33984 + l);
                        RenderSystem.disableClientState(32888);
                        RenderSystem.glClientActiveTexture(33984);
                        continue block13;
                    }
                    case COLOR: {
                        RenderSystem.disableClientState(32886);
                        RenderSystem.clearCurrentColor();
                        continue block13;
                    }
                    case NORMAL: {
                        RenderSystem.disableClientState(32885);
                    }
                }
            }
        }
        bufferBuilder.clear();
    }
}

