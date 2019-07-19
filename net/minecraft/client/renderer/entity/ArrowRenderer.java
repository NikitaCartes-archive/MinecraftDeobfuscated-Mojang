/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.AbstractArrow;

@Environment(value=EnvType.CLIENT)
public abstract class ArrowRenderer<T extends AbstractArrow>
extends EntityRenderer<T> {
    public ArrowRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(T abstractArrow, double d, double e, double f, float g, float h) {
        this.bindTexture(abstractArrow);
        GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.translatef((float)d, (float)e, (float)f);
        GlStateManager.rotatef(Mth.lerp(h, ((AbstractArrow)abstractArrow).yRotO, ((AbstractArrow)abstractArrow).yRot) - 90.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotatef(Mth.lerp(h, ((AbstractArrow)abstractArrow).xRotO, ((AbstractArrow)abstractArrow).xRot), 0.0f, 0.0f, 1.0f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        boolean i = false;
        float j = 0.0f;
        float k = 0.5f;
        float l = 0.0f;
        float m = 0.15625f;
        float n = 0.0f;
        float o = 0.15625f;
        float p = 0.15625f;
        float q = 0.3125f;
        float r = 0.05625f;
        GlStateManager.enableRescaleNormal();
        float s = (float)((AbstractArrow)abstractArrow).shakeTime - h;
        if (s > 0.0f) {
            float t = -Mth.sin(s * 3.0f) * s;
            GlStateManager.rotatef(t, 0.0f, 0.0f, 1.0f);
        }
        GlStateManager.rotatef(45.0f, 1.0f, 0.0f, 0.0f);
        GlStateManager.scalef(0.05625f, 0.05625f, 0.05625f);
        GlStateManager.translatef(-4.0f, 0.0f, 0.0f);
        if (this.solidRender) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(abstractArrow));
        }
        GlStateManager.normal3f(0.05625f, 0.0f, 0.0f);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(-7.0, -2.0, -2.0).uv(0.0, 0.15625).endVertex();
        bufferBuilder.vertex(-7.0, -2.0, 2.0).uv(0.15625, 0.15625).endVertex();
        bufferBuilder.vertex(-7.0, 2.0, 2.0).uv(0.15625, 0.3125).endVertex();
        bufferBuilder.vertex(-7.0, 2.0, -2.0).uv(0.0, 0.3125).endVertex();
        tesselator.end();
        GlStateManager.normal3f(-0.05625f, 0.0f, 0.0f);
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(-7.0, 2.0, -2.0).uv(0.0, 0.15625).endVertex();
        bufferBuilder.vertex(-7.0, 2.0, 2.0).uv(0.15625, 0.15625).endVertex();
        bufferBuilder.vertex(-7.0, -2.0, 2.0).uv(0.15625, 0.3125).endVertex();
        bufferBuilder.vertex(-7.0, -2.0, -2.0).uv(0.0, 0.3125).endVertex();
        tesselator.end();
        for (int u = 0; u < 4; ++u) {
            GlStateManager.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
            GlStateManager.normal3f(0.0f, 0.0f, 0.05625f);
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(-8.0, -2.0, 0.0).uv(0.0, 0.0).endVertex();
            bufferBuilder.vertex(8.0, -2.0, 0.0).uv(0.5, 0.0).endVertex();
            bufferBuilder.vertex(8.0, 2.0, 0.0).uv(0.5, 0.15625).endVertex();
            bufferBuilder.vertex(-8.0, 2.0, 0.0).uv(0.0, 0.15625).endVertex();
            tesselator.end();
        }
        if (this.solidRender) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
        super.render(abstractArrow, d, e, f, g, h);
    }
}

