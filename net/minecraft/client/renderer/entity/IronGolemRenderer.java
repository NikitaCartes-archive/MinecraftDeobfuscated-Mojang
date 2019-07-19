/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(value=EnvType.CLIENT)
public class IronGolemRenderer
extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
    private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem.png");

    public IronGolemRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new IronGolemModel(), 0.7f);
        this.addLayer(new IronGolemFlowerLayer(this));
    }

    @Override
    protected ResourceLocation getTextureLocation(IronGolem ironGolem) {
        return GOLEM_LOCATION;
    }

    @Override
    protected void setupRotations(IronGolem ironGolem, float f, float g, float h) {
        super.setupRotations(ironGolem, f, g, h);
        if ((double)ironGolem.animationSpeed < 0.01) {
            return;
        }
        float i = 13.0f;
        float j = ironGolem.animationPosition - ironGolem.animationSpeed * (1.0f - h) + 6.0f;
        float k = (Math.abs(j % 13.0f - 6.5f) - 3.25f) / 3.25f;
        GlStateManager.rotatef(6.5f * k, 0.0f, 0.0f, 1.0f);
    }
}

