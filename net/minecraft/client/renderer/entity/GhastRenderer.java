/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Ghast;

@Environment(value=EnvType.CLIENT)
public class GhastRenderer
extends MobRenderer<Ghast, GhastModel<Ghast>> {
    private static final ResourceLocation GHAST_LOCATION = new ResourceLocation("textures/entity/ghast/ghast.png");
    private static final ResourceLocation GHAST_SHOOTING_LOCATION = new ResourceLocation("textures/entity/ghast/ghast_shooting.png");

    public GhastRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new GhastModel(), 1.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Ghast ghast) {
        if (ghast.isCharging()) {
            return GHAST_SHOOTING_LOCATION;
        }
        return GHAST_LOCATION;
    }

    @Override
    protected void scale(Ghast ghast, PoseStack poseStack, float f) {
        float g = 1.0f;
        float h = 4.5f;
        float i = 4.5f;
        poseStack.scale(4.5f, 4.5f, 4.5f);
    }
}

