/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
    protected final EntityRenderDispatcher entityRenderDispatcher;
    protected float shadowRadius;
    protected float shadowStrength = 1.0f;

    protected EntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    public boolean shouldRender(T entity, Frustum frustum, double d, double e, double f) {
        if (!((Entity)entity).shouldRender(d, e, f)) {
            return false;
        }
        if (((Entity)entity).noCulling) {
            return true;
        }
        AABB aABB = ((Entity)entity).getBoundingBoxForCulling().inflate(0.5);
        if (aABB.hasNaN() || aABB.getSize() == 0.0) {
            aABB = new AABB(((Entity)entity).getX() - 2.0, ((Entity)entity).getY() - 2.0, ((Entity)entity).getZ() - 2.0, ((Entity)entity).getX() + 2.0, ((Entity)entity).getY() + 2.0, ((Entity)entity).getZ() + 2.0);
        }
        return frustum.isVisible(aABB);
    }

    public Vec3 getRenderOffset(T entity, float f) {
        return Vec3.ZERO;
    }

    public void render(T entity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        if (!this.shouldShowName(entity)) {
            return;
        }
        this.renderNameTag(entity, ((Entity)entity).getDisplayName().getColoredString(), poseStack, multiBufferSource, i);
    }

    protected boolean shouldShowName(T entity) {
        return ((Entity)entity).shouldShowName() && ((Entity)entity).hasCustomName();
    }

    public abstract ResourceLocation getTextureLocation(T var1);

    public Font getFont() {
        return this.entityRenderDispatcher.getFont();
    }

    protected void renderNameTag(T entity, String string, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        double d = this.entityRenderDispatcher.distanceToSqr((Entity)entity);
        if (d > 4096.0) {
            return;
        }
        boolean bl = !((Entity)entity).isDiscrete();
        float f = ((Entity)entity).getBbHeight() + 0.5f;
        int j = "deadmau5".equals(string) ? -10 : 0;
        poseStack.pushPose();
        poseStack.translate(0.0, f, 0.0);
        poseStack.mulPose(this.entityRenderDispatcher.camera.rotation());
        poseStack.scale(-0.025f, -0.025f, 0.025f);
        Matrix4f matrix4f = poseStack.last().pose();
        float g = Minecraft.getInstance().options.getBackgroundOpacity(0.25f);
        int k = (int)(g * 255.0f) << 24;
        Font font = this.getFont();
        float h = -font.width(string) / 2;
        font.drawInBatch(string, h, j, 0x20FFFFFF, false, matrix4f, multiBufferSource, bl, k, i);
        if (bl) {
            font.drawInBatch(string, h, j, -1, false, matrix4f, multiBufferSource, false, 0, i);
        }
        poseStack.popPose();
    }

    public EntityRenderDispatcher getDispatcher() {
        return this.entityRenderDispatcher;
    }
}

