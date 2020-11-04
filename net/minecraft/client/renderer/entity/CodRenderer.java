/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Cod;

@Environment(value=EnvType.CLIENT)
public class CodRenderer
extends MobRenderer<Cod, CodModel<Cod>> {
    private static final ResourceLocation COD_LOCATION = new ResourceLocation("textures/entity/fish/cod.png");

    public CodRenderer(EntityRendererProvider.Context context) {
        super(context, new CodModel(context.getLayer(ModelLayers.COD)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(Cod cod) {
        return COD_LOCATION;
    }

    @Override
    protected void setupRotations(Cod cod, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(cod, poseStack, f, g, h);
        float i = 4.3f * Mth.sin(0.6f * f);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(i));
        if (!cod.isInWater()) {
            poseStack.translate(0.1f, 0.1f, -0.1f);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(90.0f));
        }
    }
}

