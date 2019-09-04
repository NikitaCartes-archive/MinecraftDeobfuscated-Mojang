/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

@Environment(value=EnvType.CLIENT)
public class ChunkBorderRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public ChunkBorderRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(long l) {
        int k;
        Camera camera = this.minecraft.gameRenderer.getMainCamera();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double d = camera.getPosition().x;
        double e = camera.getPosition().y;
        double f = camera.getPosition().z;
        double g = 0.0 - e;
        double h = 256.0 - e;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        double i = (double)(camera.getEntity().xChunk << 4) - d;
        double j = (double)(camera.getEntity().zChunk << 4) - f;
        RenderSystem.lineWidth(1.0f);
        bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
        for (k = -16; k <= 32; k += 16) {
            for (int m = -16; m <= 32; m += 16) {
                bufferBuilder.vertex(i + (double)k, g, j + (double)m).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, g, j + (double)m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)m).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)m).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
            }
        }
        for (k = 2; k < 16; k += 2) {
            bufferBuilder.vertex(i + (double)k, g, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, g, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, h, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, h, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, g, j + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, g, j + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, h, j + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + (double)k, h, j + 16.0).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (k = 2; k < 16; k += 2) {
            bufferBuilder.vertex(i, g, j + (double)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, g, j + (double)k).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, h, j + (double)k).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, h, j + (double)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, g, j + (double)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, g, j + (double)k).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, h, j + (double)k).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, h, j + (double)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (k = 0; k <= 256; k += 2) {
            double n = (double)k - e;
            bufferBuilder.vertex(i, n, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, n, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, n, j + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, n, j + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, n, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, n, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, n, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(2.0f);
        bufferBuilder.begin(3, DefaultVertexFormat.POSITION_COLOR);
        for (k = 0; k <= 16; k += 16) {
            for (int m = 0; m <= 16; m += 16) {
                bufferBuilder.vertex(i + (double)k, g, j + (double)m).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, g, j + (double)m).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)m).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)m).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            }
        }
        for (k = 0; k <= 256; k += 16) {
            double n = (double)k - e;
            bufferBuilder.vertex(i, n, j).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, n, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, n, j + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, n, j + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, n, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, n, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, n, j).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }
}

