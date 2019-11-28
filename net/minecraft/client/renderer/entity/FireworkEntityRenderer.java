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
    public void render(FireworkRocketEntity fireworkRocketEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        if (fireworkRocketEntity.isShotAtAngle()) {
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0f));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0f));
        }
        this.itemRenderer.renderStatic(fireworkRocketEntity.getItem(), ItemTransforms.TransformType.GROUND, i, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource);
        poseStack.popPose();
        super.render(fireworkRocketEntity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(FireworkRocketEntity fireworkRocketEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

