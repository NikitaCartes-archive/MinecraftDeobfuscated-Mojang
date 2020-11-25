/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ShulkerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ShulkerHeadLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class ShulkerRenderer
extends MobRenderer<Shulker, ShulkerModel<Shulker>> {
    public static final ResourceLocation DEFAULT_TEXTURE_LOCATION = new ResourceLocation("textures/" + Sheets.DEFAULT_SHULKER_TEXTURE_LOCATION.texture().getPath() + ".png");
    public static final ResourceLocation[] TEXTURE_LOCATION = (ResourceLocation[])Sheets.SHULKER_TEXTURE_LOCATION.stream().map(material -> new ResourceLocation("textures/" + material.texture().getPath() + ".png")).toArray(ResourceLocation[]::new);

    public ShulkerRenderer(EntityRendererProvider.Context context) {
        super(context, new ShulkerModel(context.bakeLayer(ModelLayers.SHULKER)), 0.0f);
        this.addLayer(new ShulkerHeadLayer(this));
    }

    @Override
    public Vec3 getRenderOffset(Shulker shulker, float f) {
        int i = shulker.getClientSideTeleportInterpolation();
        if (i > 0 && shulker.hasValidInterpolationPositions()) {
            BlockPos blockPos = shulker.getAttachPosition();
            BlockPos blockPos2 = shulker.getOldAttachPosition();
            double d = (double)((float)i - f) / 6.0;
            d *= d;
            double e = (double)(blockPos.getX() - blockPos2.getX()) * d;
            double g = (double)(blockPos.getY() - blockPos2.getY()) * d;
            double h = (double)(blockPos.getZ() - blockPos2.getZ()) * d;
            return new Vec3(-e, -g, -h);
        }
        return super.getRenderOffset(shulker, f);
    }

    @Override
    public boolean shouldRender(Shulker shulker, Frustum frustum, double d, double e, double f) {
        if (super.shouldRender(shulker, frustum, d, e, f)) {
            return true;
        }
        if (shulker.getClientSideTeleportInterpolation() > 0 && shulker.hasValidInterpolationPositions()) {
            Vec3 vec3 = Vec3.atLowerCornerOf(shulker.getAttachPosition());
            Vec3 vec32 = Vec3.atLowerCornerOf(shulker.getOldAttachPosition());
            if (frustum.isVisible(new AABB(vec32.x, vec32.y, vec32.z, vec3.x, vec3.y, vec3.z))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ResourceLocation getTextureLocation(Shulker shulker) {
        if (shulker.getColor() == null) {
            return DEFAULT_TEXTURE_LOCATION;
        }
        return TEXTURE_LOCATION[shulker.getColor().getId()];
    }

    @Override
    protected void setupRotations(Shulker shulker, PoseStack poseStack, float f, float g, float h) {
        super.setupRotations(shulker, poseStack, f, g + 180.0f, h);
        poseStack.translate(0.0, 0.5, 0.0);
        poseStack.mulPose(shulker.getAttachFace().getOpposite().getRotation());
        poseStack.translate(0.0, -0.5, 0.0);
    }
}

