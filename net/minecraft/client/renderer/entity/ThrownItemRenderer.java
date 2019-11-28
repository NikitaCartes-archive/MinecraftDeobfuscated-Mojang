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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

@Environment(value=EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity>
extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;
    private final boolean fullBright;

    public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, float f, boolean bl) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
        this.scale = f;
        this.fullBright = bl;
    }

    public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        this(entityRenderDispatcher, itemRenderer, 1.0f, false);
    }

    @Override
    protected int getBlockLightLevel(T entity, float f) {
        return this.fullBright ? 15 : super.getBlockLightLevel(entity, f);
    }

    @Override
    public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        this.itemRenderer.renderStatic(((ItemSupplier)entity).getItem(), ItemTransforms.TransformType.GROUND, i, OverlayTexture.NO_OVERLAY, poseStack, multiBufferSource);
        poseStack.popPose();
        super.render(entity, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

