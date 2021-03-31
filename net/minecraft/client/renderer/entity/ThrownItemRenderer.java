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
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

@Environment(value=EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity>
extends EntityRenderer<T> {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25f;
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRendererProvider.Context context, float f, boolean bl) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.scale = f;
        this.fullBright = bl;
    }

    public ThrownItemRenderer(EntityRendererProvider.Context context) {
        this(context, 1.0f, false);
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos blockPos) {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, blockPos);
    }

    @Override
    public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (((Entity)entity).tickCount < 2 && this.entityRenderDispatcher.camera.getEntity().distanceToSqr((Entity)entity) < 12.25) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        this.itemRenderer.renderStatic(((ItemSupplier)entity).getItem(), ItemTransforms.TransformType.GROUND, i, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource, ((Entity)entity).getId());
        poseStack.popPose();
        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

