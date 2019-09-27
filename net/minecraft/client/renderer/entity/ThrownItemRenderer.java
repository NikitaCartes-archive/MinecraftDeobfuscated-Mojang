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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ItemSupplier;

@Environment(value=EnvType.CLIENT)
public class ThrownItemRenderer<T extends Entity>
extends EntityRenderer<T> {
    private final ItemRenderer itemRenderer;
    private final float scale;

    public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, float f) {
        super(entityRenderDispatcher);
        this.itemRenderer = itemRenderer;
        this.scale = f;
    }

    public ThrownItemRenderer(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer) {
        this(entityRenderDispatcher, itemRenderer, 1.0f);
    }

    @Override
    public void render(T entity, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, this.scale);
        poseStack.mulPose(Vector3f.YP.rotation(-this.entityRenderDispatcher.playerRotY, true));
        poseStack.mulPose(Vector3f.XP.rotation((float)(this.entityRenderDispatcher.options.thirdPersonView == 2 ? -1 : 1) * this.entityRenderDispatcher.playerRotX, true));
        poseStack.mulPose(Vector3f.YP.rotation(180.0f, true));
        this.itemRenderer.renderStatic(((ItemSupplier)entity).getItem(), ItemTransforms.TransformType.GROUND, ((Entity)entity).getLightColor(), poseStack, multiBufferSource);
        poseStack.popPose();
        super.render(entity, d, e, f, g, h, poseStack, multiBufferSource);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}

