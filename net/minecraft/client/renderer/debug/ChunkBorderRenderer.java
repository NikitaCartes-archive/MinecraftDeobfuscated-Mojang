/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import org.joml.Matrix4f;

@Environment(value=EnvType.CLIENT)
public class ChunkBorderRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private final Minecraft minecraft;
    private static final int CELL_BORDER = FastColor.ARGB32.color(255, 0, 155, 155);
    private static final int YELLOW = FastColor.ARGB32.color(255, 255, 255, 0);

    public ChunkBorderRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
        int l;
        int k;
        Entity entity = this.minecraft.gameRenderer.getMainCamera().getEntity();
        float g = (float)((double)this.minecraft.level.getMinBuildHeight() - e);
        float h = (float)((double)this.minecraft.level.getMaxBuildHeight() - e);
        ChunkPos chunkPos = entity.chunkPosition();
        float i = (float)((double)chunkPos.getMinBlockX() - d);
        float j = (float)((double)chunkPos.getMinBlockZ() - f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(1.0));
        Matrix4f matrix4f = poseStack.last().pose();
        for (k = -16; k <= 32; k += 16) {
            for (l = -16; l <= 32; l += 16) {
                vertexConsumer.vertex(matrix4f, i + (float)k, g, j + (float)l).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
                vertexConsumer.vertex(matrix4f, i + (float)k, g, j + (float)l).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                vertexConsumer.vertex(matrix4f, i + (float)k, h, j + (float)l).color(1.0f, 0.0f, 0.0f, 0.5f).endVertex();
                vertexConsumer.vertex(matrix4f, i + (float)k, h, j + (float)l).color(1.0f, 0.0f, 0.0f, 0.0f).endVertex();
            }
        }
        for (k = 2; k < 16; k += 2) {
            l = k % 4 == 0 ? CELL_BORDER : YELLOW;
            vertexConsumer.vertex(matrix4f, i + (float)k, g, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, g, j).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, h, j).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, h, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, g, j + 16.0f).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, g, j + 16.0f).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, h, j + 16.0f).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i + (float)k, h, j + 16.0f).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (k = 2; k < 16; k += 2) {
            l = k % 4 == 0 ? CELL_BORDER : YELLOW;
            vertexConsumer.vertex(matrix4f, i, g, j + (float)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i, g, j + (float)k).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i, h, j + (float)k).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i, h, j + (float)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, g, j + (float)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, g, j + (float)k).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, h, j + (float)k).color(l).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, h, j + (float)k).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        for (k = this.minecraft.level.getMinBuildHeight(); k <= this.minecraft.level.getMaxBuildHeight(); k += 2) {
            float m = (float)((double)k - e);
            int n = k % 8 == 0 ? CELL_BORDER : YELLOW;
            vertexConsumer.vertex(matrix4f, i, m, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j).color(n).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j + 16.0f).color(n).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, m, j + 16.0f).color(n).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, m, j).color(n).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j).color(n).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j).color(1.0f, 1.0f, 0.0f, 0.0f).endVertex();
        }
        vertexConsumer = multiBufferSource.getBuffer(RenderType.debugLineStrip(2.0));
        for (k = 0; k <= 16; k += 16) {
            for (int l2 = 0; l2 <= 16; l2 += 16) {
                vertexConsumer.vertex(matrix4f, i + (float)k, g, j + (float)l2).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
                vertexConsumer.vertex(matrix4f, i + (float)k, g, j + (float)l2).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                vertexConsumer.vertex(matrix4f, i + (float)k, h, j + (float)l2).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
                vertexConsumer.vertex(matrix4f, i + (float)k, h, j + (float)l2).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            }
        }
        for (k = this.minecraft.level.getMinBuildHeight(); k <= this.minecraft.level.getMaxBuildHeight(); k += 16) {
            float m = (float)((double)k - e);
            vertexConsumer.vertex(matrix4f, i, m, j).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j + 16.0f).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, m, j + 16.0f).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i + 16.0f, m, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j).color(0.25f, 0.25f, 1.0f, 1.0f).endVertex();
            vertexConsumer.vertex(matrix4f, i, m, j).color(0.25f, 0.25f, 1.0f, 0.0f).endVertex();
        }
    }
}

