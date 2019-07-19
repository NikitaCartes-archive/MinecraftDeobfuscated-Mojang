/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class VertexBufferUploader
extends BufferUploader {
    private VertexBuffer buffer;

    @Override
    public void end(BufferBuilder bufferBuilder) {
        bufferBuilder.clear();
        this.buffer.upload(bufferBuilder.getBuffer());
    }

    public void setBuffer(VertexBuffer vertexBuffer) {
        this.buffer = vertexBuffer;
    }
}

