/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Squid;

@Environment(value=EnvType.CLIENT)
public class SquidRenderer
extends MobRenderer<Squid, SquidModel<Squid>> {
    private static final ResourceLocation SQUID_LOCATION = new ResourceLocation("textures/entity/squid.png");

    public SquidRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new SquidModel(), 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(Squid squid) {
        return SQUID_LOCATION;
    }

    @Override
    protected void setupRotations(Squid squid, PoseStack poseStack, float f, float g, float h) {
        float i = Mth.lerp(h, squid.xBodyRotO, squid.xBodyRot);
        float j = Mth.lerp(h, squid.zBodyRotO, squid.zBodyRot);
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(Vector3f.YP.rotation(180.0f - g, true));
        poseStack.mulPose(Vector3f.XP.rotation(i, true));
        poseStack.mulPose(Vector3f.YP.rotation(j, true));
        poseStack.translate(0.0, -1.2f, 0.0);
    }

    @Override
    protected float getBob(Squid squid, float f) {
        return Mth.lerp(f, squid.oldTentacleAngle, squid.tentacleAngle);
    }
}

