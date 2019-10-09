/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;

@Environment(value=EnvType.CLIENT)
public class FireworkEntityRenderer
extends EntityRenderer<FireworkRocketEntity> {
    private final ItemRenderer itemRenderer;

    public FireworkEntityRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
    }

    @Override
    public void render(FireworkRocketEntity fireworkRocketEntity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(-this.entityRenderDispatcher.playerRotY));
        float i = (float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX;
        poseStack.mulPose(Vector3f.XP.rotationDegrees(i));
        if (fireworkRocketEntity.isShotAtAngle()) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        } else {
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        }
        this.itemRenderer.renderStatic(fireworkRocketEntity.getItem(), ItemTransforms.TransformType.GROUND, fireworkRocketEntity.getLightColor(), OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource);
        poseStack.popPose();
        super.render(fireworkRocketEntity, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

