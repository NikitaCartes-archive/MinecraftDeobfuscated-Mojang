/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.nio.ByteBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class VertexBuffer {
    private int id;
    private final VertexFormat format;
    private int vertexCount;

    public VertexBuffer(VertexFormat vertexFormat) {
        this.format = vertexFormat;
        this.id = GlStateManager.glGenBuffers();
    }

    public void bind() {
        GlStateManager.glBindBuffer(34962, this.id);
    }

    public void upload(ByteBuffer byteBuffer) {
        this.bind();
        GlStateManager.glBufferData(34962, byteBuffer, 35044);
        VertexBuffer.unbind();
        this.vertexCount = byteBuffer.limit() / this.format.getVertexSize();
    }

    public void draw(int i) {
        RenderSystem.drawArrays(i, 0, this.vertexCount);
    }

    public static void unbind() {
        GlStateManager.glBindBuffer(34962, 0);
    }

    public void delete() {
        if (this.id >= 0) {
            GlStateManager.glDeleteBuffers(this.id);
            this.id = -1;
        }
    }
}

