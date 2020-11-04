/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ambient.Bat;

@Environment(value=EnvType.CLIENT)
public class BatRenderer
extends MobRenderer<Bat, BatModel> {
    private static final ResourceLocation BAT_LOCATION = new ResourceLocation("textures/entity/bat.png");

    public BatRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.getLayer(ModelLayers.BAT)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(Bat bat) {
        return BAT_LOCATION;
    }

    @Override
    protected void scale(Bat bat, PoseStack poseStack, float f) {
        poseStack.scale(0.35f, 0.35f, 0.35f);
    }

    @Override
    protected void setupRotations(Bat bat, PoseStack poseStack, float f, float g, float h) {
        if (bat.isResting()) {
            poseStack.translate(0.0, -0.1f, 0.0);
        } else {
            poseStack.translate(0.0, Mth.cos(f * 0.3f) * 0.1f, 0.0);
        }
        super.setupRotations(bat, poseStack, f, g, h);
    }
}

