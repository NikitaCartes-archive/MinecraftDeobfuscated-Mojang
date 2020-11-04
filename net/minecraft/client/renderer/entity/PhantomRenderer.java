/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PhantomModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Phantom;

@Environment(value=EnvType.CLIENT)
public class PhantomRenderer
extends MobRenderer<Phantom, PhantomModel<Phantom>> {
    private static final ResourceLocation PHANTOM_LOCATION = new ResourceLocation("textures/entity/phantom.png");

    public PhantomRenderer(EntityRendererProvider.Context context) {
        super(context, new PhantomModel(context.getLayer(ModelLayers.PHANTOM)), 0.75f);
        this.addLayer(new PhantomEyesLayer<Phantom>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(Phantom phantom) {
        return PHANTOM_LOCATION;
    }

    @Override
    protected void scale(Phantom phantom, PoseStack poseStack, float f) {
        int i = phantom.getPhantomSize();
        float g = 1.0f + 0.15f * (float)i;
        poseStack.scale(g, g, g);
        poseStack.translate(0.0, 1.3125, 0.1875);
    }

    @Override
    protected void setupRotations(Phantom phantom, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(phantom, poseStack, f, g, h);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(phantom.xRot));
    }
}

