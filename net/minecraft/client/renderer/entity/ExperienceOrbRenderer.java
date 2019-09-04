/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;

@Environment(value=EnvType.CLIENT)
public class ExperienceOrbRenderer
extends EntityRenderer<ExperienceOrb> {
    private static final ResourceLocation EXPERIENCE_ORB_LOCATION = new ResourceLocation("textures/entity/experience_orb.png");

    public ExperienceOrbRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.15f;
        this.shadowStrength = 0.75f;
    }

    @Override
    public void render(ExperienceOrb experienceOrb, double d, double e, double f, float g, float h) {
        if (this.solidRender || Minecraft.getInstance().getEntityRenderDispatcher().options == null) {
            return;
        }
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e, (float)f);
        this.bindTexture(experienceOrb);
        Lighting.turnOn();
        int i = experienceOrb.getIcon();
        float j = (float)(i % 4 * 16 + 0) / 64.0f;
        float k = (float)(i % 4 * 16 + 16) / 64.0f;
        float l = (float)(i / 4 * 16 + 0) / 64.0f;
        float m = (float)(i / 4 * 16 + 16) / 64.0f;
        float n = 1.0f;
        float o = 0.5f;
        float p = 0.25f;
        int q = experienceOrb.getLightColor();
        int r = q % 65536;
        int s = q / 65536;
        RenderSystem.glMultiTexCoord2f(33985, r, s);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        float t = 255.0f;
        float u = ((float)experienceOrb.tickCount + h) / 2.0f;
        int v = (int)((Mth.sin(u + 0.0f) + 1.0f) * 0.5f * 255.0f);
        int w = 255;
        int x = (int)((Mth.sin(u + 4.1887903f) + 1.0f) * 0.1f * 255.0f);
        RenderSystem.translatef(0.0f, 0.1f, 0.0f);
        RenderSystem.rotatef(180.0f - this.entityRenderDispatcher.playerRotY, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * -this.entityRenderDispatcher.playerRotX, 1.0f, 0.0f, 0.0f);
        float y = 0.3f;
        RenderSystem.scalef(0.3f, 0.3f, 0.3f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        bufferBuilder.vertex(-0.5, -0.25, 0.0).uv(j, m).color(v, 255, x, 128).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.5, -0.25, 0.0).uv(k, m).color(v, 255, x, 128).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(0.5, 0.75, 0.0).uv(k, l).color(v, 255, x, 128).normal(0.0f, 1.0f, 0.0f).endVertex();
        bufferBuilder.vertex(-0.5, 0.75, 0.0).uv(j, l).color(v, 255, x, 128).normal(0.0f, 1.0f, 0.0f).endVertex();
        tesselator.end();
        RenderSystem.disableBlend();
        RenderSystem.disableRescaleNormal();
        RenderSystem.popMatrix();
        super.render(experienceOrb, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(ExperienceOrb experienceOrb) {
        return EXPERIENCE_ORB_LOCATION;
    }
}

