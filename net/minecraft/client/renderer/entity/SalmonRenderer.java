/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Salmon;

@Environment(value=EnvType.CLIENT)
public class SalmonRenderer
extends MobRenderer<Salmon, SalmonModel<Salmon>> {
    private static final ResourceLocation SALMON_LOCATION = new ResourceLocation("textures/entity/fish/salmon.png");

    public SalmonRenderer(EntityRendererProvider.Context context) {
        super(context, new SalmonModel(context.bakeLayer(ModelLayers.SALMON)), 0.4f);
    }

    @Override
    public ResourceLocation getTextureLocation(Salmon salmon) {
        return SALMON_LOCATION;
    }

    @Override
    protected void setupRotations(Salmon salmon, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(salmon, poseStack, f, g, h);
        float i = 1.0f;
        float j = 1.0f;
        if (!salmon.isInWater()) {
            i = 1.3f;
            j = 1.7f;
        }
        float k = i * 4.3f * Mth.sin(j * 0.6f * f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(k));
        poseStack.translate(0.0, 0.0, -0.4f);
        if (!salmon.isInWater()) {
            poseStack.translate(0.2f, 0.1f, 0.0);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
        }
    }
}

