/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.FoxModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.FoxHeldItemLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Fox;

@Environment(value=EnvType.CLIENT)
public class FoxRenderer
extends MobRenderer<Fox, FoxModel<Fox>> {
    private static final ResourceLocation RED_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/fox.png");
    private static final ResourceLocation RED_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/fox_sleep.png");
    private static final ResourceLocation SNOW_FOX_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox.png");
    private static final ResourceLocation SNOW_FOX_SLEEP_TEXTURE = new ResourceLocation("textures/entity/fox/snow_fox_sleep.png");

    public FoxRenderer(EntityRendererProvider.Context context) {
        super(context, new FoxModel(context.bakeLayer(ModelLayers.FOX)), 0.4f);
        this.addLayer(new FoxHeldItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    protected void setupRotations(Fox fox, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(fox, poseStack, f, g, h);
        if (fox.isPouncing() || fox.isFaceplanted()) {
            float i = -Mth.lerp(h, fox.xRotO, fox.getXRot());
            poseStack.mulPose(Axis.XP.rotationDegrees(i));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Fox fox) {
        if (fox.getVariant() == Fox.Type.RED) {
            return fox.isSleeping() ? RED_FOX_SLEEP_TEXTURE : RED_FOX_TEXTURE;
        }
        return fox.isSleeping() ? SNOW_FOX_SLEEP_TEXTURE : SNOW_FOX_TEXTURE;
    }
}

