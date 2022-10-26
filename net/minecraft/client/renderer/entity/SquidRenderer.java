/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

@Environment(value=EnvType.CLIENT)
public class SquidRenderer<T extends Squid>
extends MobRenderer<T, SquidModel<T>> {
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid/squid.png");

    public SquidRenderer(EntityRendererProvider.Context context, SquidModel<T> squidModel) {
        super(context, squidModel, 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(T squid) {
        return SQUID_LOCATION;
    }

    @Override
    protected void setupRotations(T squid, PoseStack poseStack, float f, float g, float h) {
        float i = Mth.lerp(h, ((Squid)squid).xBodyRotO, ((Squid)squid).xBodyRot);
        float j = Mth.lerp(h, ((Squid)squid).zBodyRotO, ((Squid)squid).zBodyRot);
        poseStack.translate(0.0f, 0.5f, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0f - g));
        poseStack.mulPose(Axis.XP.rotationDegrees(i));
        poseStack.mulPose(Axis.YP.rotationDegrees(j));
        poseStack.translate(0.0f, -1.2f, 0.0f);
    }

    @Override
    protected float getBob(T squid, float f) {
        return Mth.lerp(f, ((Squid)squid).oldTentacleAngle, ((Squid)squid).tentacleAngle);
    }
}

