/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

@Environment(value=EnvType.CLIENT)
public class ChunkBorderRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;

    public ChunkBorderRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        int k;
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        double g = (double)this.minecraft.level.getMinBuildHeight() - e;
        double h = (double)this.minecraft.level.getMaxBuildHeight() - e;
        RenderSystem.disableTexture();
        RenderSystem.disableBlend();
        ChunkPos chunkPos = entity.chunkPosition();
        double i = (double)chunkPos.getMinBlockX() - d;
        double j = (double)chunkPos.getMinBlockZ() - f;
        RenderSystem.lineWidth(1.0f);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (k = -16; k <= 32; k += 16) {
            for (int l = -16; l <= 32; l += 16) {
                bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
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
        for (k = this.minecraft.level.getMinBuildHeight(); k <= this.minecraft.level.getMaxBuildHeight(); k += 2) {
            double m = (double)k - e;
            bufferBuilder.vertex(i, m, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, m, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, m, j + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, m, j + 16.0).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, m, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, m, j).color(1.0f, 1.0f, 0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, m, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(2.0f);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (k = 0; k <= 16; k += 16) {
            for (int l = 0; l <= 16; l += 16) {
                bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, g, j + (double)l).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                bufferBuilder.vertex(i + (double)k, h, j + (double)l).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            }
        }
        for (k = this.minecraft.level.getMinBuildHeight(); k <= this.minecraft.level.getMaxBuildHeight(); k += 16) {
            double m = (double)k - e;
            bufferBuilder.vertex(i, m, j).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(i, m, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, m, j + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, m, j + 16.0).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i + 16.0, m, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, m, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            bufferBuilder.vertex(i, m, j).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
        }
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableBlend();
        RenderSystem.enableTexture();
    }
}

