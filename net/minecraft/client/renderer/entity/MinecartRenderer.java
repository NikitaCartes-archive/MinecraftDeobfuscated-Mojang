/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart>
extends EntityRenderer<T> {
    private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
    protected final EntityModel<T> model = new MinecartModel();

    public MinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
        this.shadowRadius = 0.7f;
    }

    @Override
    public void render(T abstractMinecart, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        super.render(abstractMinecart, d, e, f, g, h, poseStack, multiBufferSource);
        poseStack.pushPose();
        long l = (long)((Entity)abstractMinecart).getId() * 493286711L;
        l = l * l * 4392167121L + l * 98761L;
        float i = (((float)(l >> 16 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float j = (((float)(l >> 20 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        float k = (((float)(l >> 24 & 7L) + 0.5f) / 8.0f - 0.5f) * 0.004f;
        poseStack.translate(i, j, k);
        double m = Mth.lerp((double)h, ((AbstractMinecart)abstractMinecart).xOld, ((AbstractMinecart)abstractMinecart).x);
        double n = Mth.lerp((double)h, ((AbstractMinecart)abstractMinecart).yOld, ((AbstractMinecart)abstractMinecart).y);
        double o = Mth.lerp((double)h, ((AbstractMinecart)abstractMinecart).zOld, ((AbstractMinecart)abstractMinecart).z);
        double p = 0.3f;
        Vec3 vec3 = ((AbstractMinecart)abstractMinecart).getPos(m, n, o);
        float q = Mth.lerp(h, ((AbstractMinecart)abstractMinecart).xRotO, ((AbstractMinecart)abstractMinecart).xRot);
        if (vec3 != null) {
            Vec3 vec32 = ((AbstractMinecart)abstractMinecart).getPosOffs(m, n, o, 0.3f);
            Vec3 vec33 = ((AbstractMinecart)abstractMinecart).getPosOffs(m, n, o, -0.3f);
            if (vec32 == null) {
                vec32 = vec3;
            }
            if (vec33 == null) {
                vec33 = vec3;
            }
            poseStack.translate(vec3.x - m, (vec32.y + vec33.y) / 2.0 - n, vec3.z - o);
            Vec3 vec34 = vec33.add(-vec32.x, -vec32.y, -vec32.z);
            if (vec34.length() != 0.0) {
                vec34 = vec34.normalize();
                g = (float)(Math.atan2(vec34.z, vec34.x) * 180.0 / Math.PI);
                q = (float)(Math.atan(vec34.y) * 73.0);
            }
        }
        poseStack.translate(0.0, 0.375, 0.0);
        poseStack.mulPose(Vector3f.YP.rotation(180.0f - g, true));
        poseStack.mulPose(Vector3f.ZP.rotation(-q, true));
        float r = (float)((AbstractMinecart)abstractMinecart).getHurtTime() - h;
        float s = ((AbstractMinecart)abstractMinecart).getDamage() - h;
        if (s < 0.0f) {
            s = 0.0f;
        }
        if (r > 0.0f) {
            poseStack.mulPose(Vector3f.XP.rotation(Mth.sin(r) * r * s / 10.0f * (float)((AbstractMinecart)abstractMinecart).getHurtDir(), true));
        }
        int t = ((AbstractMinecart)abstractMinecart).getDisplayOffset();
        int u = ((Entity)abstractMinecart).getLightColor();
        BlockState blockState = ((AbstractMinecart)abstractMinecart).getDisplayBlockState();
        if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
            poseStack.pushPose();
            float v = 0.75f;
            poseStack.scale(0.75f, 0.75f, 0.75f);
            poseStack.translate(-0.5, (float)(t - 8) / 16.0f, 0.5);
            this.renderMinecartContents(abstractMinecart, h, blockState, poseStack, multiBufferSource, u);
            poseStack.popPose();
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        this.model.setupAnim(abstractMinecart, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(this.getTextureLocation(abstractMinecart)));
        OverlayTexture.setDefault(vertexConsumer);
        this.model.renderToBuffer(poseStack, vertexConsumer, u);
        vertexConsumer.unsetDefaultOverlayCoords();
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T abstractMinecart) {
        return MINECART_LOCATION;
    }

    protected void renderMinecartContents(T abstractMinecart, float f, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, 0, 10);
    }
}

