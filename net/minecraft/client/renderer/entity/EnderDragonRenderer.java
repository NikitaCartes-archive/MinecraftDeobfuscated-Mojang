/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EnderDragonDeathLayer;
import net.minecraft.client.renderer.entity.layers.EnderDragonEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(value=EnvType.CLIENT)
public class EnderDragonRenderer
extends MobRenderer<EnderDragon, DragonModel> {
    public static final ResourceLocation CRYSTAL_BEAM_LOCATION = new ResourceLocation("textures/entity/end_crystal/end_crystal_beam.png");
    private static final ResourceLocation DRAGON_EXPLODING_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon_exploding.png");
    private static final ResourceLocation DRAGON_LOCATION = new ResourceLocation("textures/entity/enderdragon/dragon.png");

    public EnderDragonRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new DragonModel(0.0f), 0.5f);
        this.addLayer(new EnderDragonEyesLayer(this));
        this.addLayer(new EnderDragonDeathLayer(this));
    }

    @Override
    protected void setupRotations(EnderDragon enderDragon, float f, float g, float h) {
        float i = (float)enderDragon.getLatencyPos(7, h)[0];
        float j = (float)(enderDragon.getLatencyPos(5, h)[1] - enderDragon.getLatencyPos(10, h)[1]);
        RenderSystem.rotatef(-i, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(j * 10.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.translatef(0.0f, 0.0f, 1.0f);
        if (enderDragon.deathTime > 0) {
            float k = ((float)enderDragon.deathTime + h - 1.0f) / 20.0f * 1.6f;
            if ((k = Mth.sqrt(k)) > 1.0f) {
                k = 1.0f;
            }
            RenderSystem.rotatef(k * this.getFlipDegrees(enderDragon), 0.0f, 0.0f, 1.0f);
        }
    }

    @Override
    protected void renderModel(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k) {
        if (enderDragon.dragonDeathTime > 0) {
            float l = (float)enderDragon.dragonDeathTime / 200.0f;
            RenderSystem.depthFunc(515);
            RenderSystem.enableAlphaTest();
            RenderSystem.alphaFunc(516, l);
            this.bindTexture(DRAGON_EXPLODING_LOCATION);
            ((DragonModel)this.model).render(enderDragon, f, g, h, i, j, k);
            RenderSystem.alphaFunc(516, 0.1f);
            RenderSystem.depthFunc(514);
        }
        this.bindTexture(enderDragon);
        ((DragonModel)this.model).render(enderDragon, f, g, h, i, j, k);
        if (enderDragon.hurtTime > 0) {
            RenderSystem.depthFunc(514);
            RenderSystem.disableTexture();
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.color4f(1.0f, 0.0f, 0.0f, 0.5f);
            ((DragonModel)this.model).render(enderDragon, f, g, h, i, j, k);
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            RenderSystem.depthFunc(515);
        }
    }

    @Override
    public void render(EnderDragon enderDragon, double d, double e, double f, float g, float h) {
        super.render(enderDragon, d, e, f, g, h);
        if (enderDragon.nearestCrystal != null) {
            this.bindTexture(CRYSTAL_BEAM_LOCATION);
            float i = Mth.sin(((float)enderDragon.nearestCrystal.tickCount + h) * 0.2f) / 2.0f + 0.5f;
            i = (i * i + i) * 0.2f;
            EnderDragonRenderer.renderCrystalBeams(d, e, f, h, Mth.lerp((double)(1.0f - h), enderDragon.x, enderDragon.xo), Mth.lerp((double)(1.0f - h), enderDragon.y, enderDragon.yo), Mth.lerp((double)(1.0f - h), enderDragon.z, enderDragon.zo), enderDragon.tickCount, enderDragon.nearestCrystal.x, (double)i + enderDragon.nearestCrystal.y, enderDragon.nearestCrystal.z);
        }
    }

    public static void renderCrystalBeams(double d, double e, double f, float g, double h, double i, double j, int k, double l, double m, double n) {
        float o = (float)(l - h);
        float p = (float)(m - 1.0 - i);
        float q = (float)(n - j);
        float r = Mth.sqrt(o * o + q * q);
        float s = Mth.sqrt(o * o + p * p + q * q);
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e + 2.0f, (float)f);
        RenderSystem.rotatef((float)(-Math.atan2(q, o)) * 57.295776f - 90.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef((float)(-Math.atan2(r, p)) * 57.295776f - 90.0f, 1.0f, 0.0f, 0.0f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Lighting.turnOff();
        RenderSystem.disableCull();
        RenderSystem.shadeModel(7425);
        float t = 0.0f - ((float)k + g) * 0.01f;
        float u = Mth.sqrt(o * o + p * p + q * q) / 32.0f - ((float)k + g) * 0.01f;
        bufferBuilder.begin(5, DefaultVertexFormat.POSITION_TEX_COLOR);
        int v = 8;
        for (int w = 0; w <= 8; ++w) {
            float x = Mth.sin((float)(w % 8) * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float y = Mth.cos((float)(w % 8) * ((float)Math.PI * 2) / 8.0f) * 0.75f;
            float z = (float)(w % 8) / 8.0f;
            bufferBuilder.vertex(x * 0.2f, y * 0.2f, 0.0).uv(z, t).color(0, 0, 0, 255).endVertex();
            bufferBuilder.vertex(x, y, s).uv(z, u).color(255, 255, 255, 255).endVertex();
        }
        tesselator.end();
        RenderSystem.enableCull();
        RenderSystem.shadeModel(7424);
        Lighting.turnOn();
        RenderSystem.popMatrix();
    }

    @Override
    protected ResourceLocation getTextureLocation(EnderDragon enderDragon) {
        return DRAGON_LOCATION;
    }
}

