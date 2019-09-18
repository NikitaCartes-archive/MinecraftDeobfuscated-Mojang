/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.StuckInBodyLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Environment(value=EnvType.CLIENT)
public class BeeStingerLayer<T extends LivingEntity, M extends PlayerModel<T>>
extends StuckInBodyLayer<T, M> {
    private static final ResourceLocation BEE_STINGER_LOCATION = new ResourceLocation("textures/entity/bee/bee_stinger.png");

    public BeeStingerLayer(LivingEntityRenderer<T, M> livingEntityRenderer) {
        super(livingEntityRenderer);
    }

    @Override
    protected int numStuck(T livingEntity) {
        return ((LivingEntity)livingEntity).getStingerCount();
    }

    @Override
    protected void preRenderStuckItem(T livingEntity) {
        Lighting.turnOff();
        RenderSystem.pushMatrix();
        this.bindTexture(BEE_STINGER_LOCATION);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableLighting();
        RenderSystem.enableRescaleNormal();
    }

    @Override
    protected void postRenderStuckItem() {
        RenderSystem.disableRescaleNormal();
        RenderSystem.enableLighting();
        RenderSystem.popMatrix();
        Lighting.turnOn();
    }

    @Override
    protected void renderStuckItem(Entity entity, float f, float g, float h, float i) {
        RenderSystem.pushMatrix();
        float j = Mth.sqrt(f * f + h * h);
        float k = (float)(Math.atan2(f, h) * 57.2957763671875);
        float l = (float)(Math.atan2(g, j) * 57.2957763671875);
        RenderSystem.translatef(0.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(k - 90.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(l, 0.0f, 0.0f, 1.0f);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        float m = 0.0f;
        float n = 0.125f;
        float o = 0.0f;
        float p = 0.0625f;
        float q = 0.03125f;
        RenderSystem.rotatef(45.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.scalef(0.03125f, 0.03125f, 0.03125f);
        RenderSystem.translatef(2.5f, 0.0f, 0.0f);
        for (int r = 0; r < 4; ++r) {
            RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
            bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.vertex(-4.5, -1.0, 0.0).uv(0.0, 0.0).endVertex();
            bufferBuilder.vertex(4.5, -1.0, 0.0).uv(0.125, 0.0).endVertex();
            bufferBuilder.vertex(4.5, 1.0, 0.0).uv(0.125, 0.0625).endVertex();
            bufferBuilder.vertex(-4.5, 1.0, 0.0).uv(0.0, 0.0625).endVertex();
            tesselator.end();
        }
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

