/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

@Environment(value=EnvType.CLIENT)
public class PolarBearRenderer
extends MobRenderer<PolarBear, PolarBearModel<PolarBear>> {
    private static final ResourceLocation BEAR_LOCATION = new ResourceLocation("textures/entity/bear/polarbear.png");

    public PolarBearRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new PolarBearModel(), 0.9f);
    }

    @Override
    public ResourceLocation getTextureLocation(PolarBear polarBear) {
        return BEAR_LOCATION;
    }

    @Override
    protected void scale(PolarBear polarBear, PoseStack poseStack, float f) {
        poseStack.scale(1.2f, 1.2f, 1.2f);
        super.scale(polarBear, poseStack, f);
    }
}

