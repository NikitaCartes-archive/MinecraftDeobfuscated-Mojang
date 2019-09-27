/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;

@Environment(value=EnvType.CLIENT)
public class CreeperRenderer
extends MobRenderer<Creeper, CreeperModel<Creeper>> {
    private static final ResourceLocation CREEPER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper.png");

    public CreeperRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new CreeperModel(), 0.5f);
        this.addLayer(new CreeperPowerLayer(this));
    }

    @Override
    protected void scale(Creeper creeper, PoseStack poseStack, float f) {
        float g = creeper.getSwelling(f);
        float h = 1.0f + Mth.sin(g * 100.0f) * g * 0.01f;
        g = Mth.clamp(g, 0.0f, 1.0f);
        g *= g;
        g *= g;
        float i = (1.0f + g * 0.4f) * h;
        float j = (1.0f + g * 0.1f) / h;
        poseStack.scale(i, j, i);
    }

    @Override
    protected float getWhiteOverlayProgress(Creeper creeper, float f) {
        float g = creeper.getSwelling(f);
        if ((int)(g * 10.0f) % 2 == 0) {
            return 0.0f;
        }
        return Mth.clamp(g, 0.0f, 1.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(Creeper creeper) {
        return CREEPER_LOCATION;
    }

    @Override
    protected /* synthetic */ float getWhiteOverlayProgress(LivingEntity livingEntity, float f) {
        return this.getWhiteOverlayProgress((Creeper)livingEntity, f);
    }
}

