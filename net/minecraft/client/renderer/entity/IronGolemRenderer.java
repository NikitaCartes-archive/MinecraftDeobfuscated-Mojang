/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.IronGolemCrackinessLayer;
import net.minecraft.client.renderer.entity.layers.IronGolemFlowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;

@Environment(value=EnvType.CLIENT)
public class IronGolemRenderer
extends MobRenderer<IronGolem, IronGolemModel<IronGolem>> {
    private static final ResourceLocation GOLEM_LOCATION = new ResourceLocation("textures/entity/iron_golem/iron_golem.png");

    public IronGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new IronGolemModel(context.bakeLayer(ModelLayers.IRON_GOLEM)), 0.7f);
        this.addLayer(new IronGolemCrackinessLayer(this));
        this.addLayer(new IronGolemFlowerLayer(this));
    }

    @Override
    public ResourceLocation getTextureLocation(IronGolem ironGolem) {
        return GOLEM_LOCATION;
    }

    @Override
    protected void setupRotations(IronGolem ironGolem, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(ironGolem, poseStack, f, g, h);
        if ((double)ironGolem.animationSpeed < 0.01) {
            return;
        }
        float i = 13.0f;
        float j = ironGolem.animationPosition - ironGolem.animationSpeed * (1.0f - h) + 6.0f;
        float k = (Math.abs(j % 13.0f - 6.5f) - 3.25f) / 3.25f;
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(6.5f * k));
    }
}

