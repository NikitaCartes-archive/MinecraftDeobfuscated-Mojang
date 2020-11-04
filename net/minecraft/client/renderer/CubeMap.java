/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Environment(value=EnvType.CLIENT)
public class CubeMap {
    private final ResourceLocation[] images = new ResourceLocation[6];

    public CubeMap(ResourceLocation resourceLocation) {
        for (int i = 0; i < 6; ++i) {
            this.images[i] = new ResourceLocation(resourceLocation.getNamespace(), resourceLocation.getPath() + '_' + i + ".png");
        }
    }

    public void render(Minecraft minecraft, float f, float g, float h) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.matrixMode(5889);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.multMatrix(Matrix4f.perspective(85.0, (float)minecraft.getWindow().getWidth() / (float)minecraft.getWindow().getHeight(), 0.05f, 10.0f));
        RenderSystem.matrixMode(5888);
        RenderSystem.pushMatrix();
        RenderSystem.loadIdentity();
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.rotatef(180.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        int i = 2;
        for (int j = 0; j < 4; ++j) {
            RenderSystem.pushMatrix();
            float k = ((float)(j % 2) / 2.0f - 0.5f) / 256.0f;
            float l = ((float)(j / 2) / 2.0f - 0.5f) / 256.0f;
            float m = 0.0f;
            RenderSystem.translatef(k, l, 0.0f);
            RenderSystem.rotatef(f, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(g, 0.0f, 1.0f, 0.0f);
            for (int n = 0; n < 6; ++n) {
                minecraft.getTextureManager().bind(this.images[n]);
                bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                int o = Math.round(255.0f * h) / (j + 1);
                if (n == 0) {
                    bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0f, 0.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0f, 0.0f).color(255, 255, 255, o).endVertex();
                }
                if (n == 1) {
                    bufferBuilder.vertex(1.0, -1.0, 1.0).uv(0.0f, 0.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, 1.0).uv(0.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0f, 0.0f).color(255, 255, 255, o).endVertex();
                }
                if (n == 2) {
                    bufferBuilder.vertex(1.0, -1.0, -1.0).uv(0.0f, 0.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, -1.0).uv(0.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(1.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(1.0f, 0.0f).color(255, 255, 255, o).endVertex();
                }
                if (n == 3) {
                    bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0f, 0.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(1.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(1.0f, 0.0f).color(255, 255, 255, o).endVertex();
                }
                if (n == 4) {
                    bufferBuilder.vertex(-1.0, -1.0, -1.0).uv(0.0f, 0.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, -1.0, 1.0).uv(0.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, 1.0).uv(1.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, -1.0, -1.0).uv(1.0f, 0.0f).color(255, 255, 255, o).endVertex();
                }
                if (n == 5) {
                    bufferBuilder.vertex(-1.0, 1.0, 1.0).uv(0.0f, 0.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(-1.0, 1.0, -1.0).uv(0.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, -1.0).uv(1.0f, 1.0f).color(255, 255, 255, o).endVertex();
                    bufferBuilder.vertex(1.0, 1.0, 1.0).uv(1.0f, 0.0f).color(255, 255, 255, o).endVertex();
                }
                tesselator.end();
            }
            RenderSystem.popMatrix();
            RenderSystem.colorMask(true, true, true, false);
        }
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.matrixMode(5889);
        RenderSystem.popMatrix();
        RenderSystem.matrixMode(5888);
        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    public CompletableFuture<Void> preload(TextureManager textureManager, Executor executor) {
        CompletableFuture[] completableFutures = new CompletableFuture[6];
        for (int i = 0; i < completableFutures.length; ++i) {
            completableFutures[i] = textureManager.preload(this.images[i], executor);
        }
        return CompletableFuture.allOf(completableFutures);
    }
}

