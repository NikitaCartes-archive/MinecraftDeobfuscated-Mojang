/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class CapeLayer
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public CapeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l) {
        if (!abstractClientPlayer.isCapeLoaded() || abstractClientPlayer.isInvisible() || !abstractClientPlayer.isModelPartShown(PlayerModelPart.CAPE) || abstractClientPlayer.getCloakTextureLocation() == null) {
            return;
        }
        ItemStack itemStack = abstractClientPlayer.getItemBySlot(EquipmentSlot.CHEST);
        if (itemStack.getItem() == Items.ELYTRA) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, 0.0, 0.125);
        double d = Mth.lerp((double)h, abstractClientPlayer.xCloakO, abstractClientPlayer.xCloak) - Mth.lerp((double)h, abstractClientPlayer.xo, abstractClientPlayer.getX());
        double e = Mth.lerp((double)h, abstractClientPlayer.yCloakO, abstractClientPlayer.yCloak) - Mth.lerp((double)h, abstractClientPlayer.yo, abstractClientPlayer.getY());
        double m = Mth.lerp((double)h, abstractClientPlayer.zCloakO, abstractClientPlayer.zCloak) - Mth.lerp((double)h, abstractClientPlayer.zo, abstractClientPlayer.getZ());
        float n = abstractClientPlayer.yBodyRotO + (abstractClientPlayer.yBodyRot - abstractClientPlayer.yBodyRotO);
        double o = Mth.sin(n * ((float)Math.PI / 180));
        double p = -Mth.cos(n * ((float)Math.PI / 180));
        float q = (float)e * 10.0f;
        q = Mth.clamp(q, -6.0f, 32.0f);
        float r = (float)(d * o + m * p) * 100.0f;
        r = Mth.clamp(r, 0.0f, 150.0f);
        float s = (float)(d * p - m * o) * 100.0f;
        s = Mth.clamp(s, -20.0f, 20.0f);
        if (r < 0.0f) {
            r = 0.0f;
        }
        float t = Mth.lerp(h, abstractClientPlayer.oBob, abstractClientPlayer.bob);
        q += Mth.sin(Mth.lerp(h, abstractClientPlayer.walkDistO, abstractClientPlayer.walkDist) * 6.0f) * 32.0f * t;
        if (abstractClientPlayer.isCrouching()) {
            q += 25.0f;
        }
        poseStack.mulPose(Vector3f.XP.rotationDegrees(6.0f + r / 2.0f + q));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(s / 2.0f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f - s / 2.0f));
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(abstractClientPlayer.getCloakTextureLocation()));
        ((PlayerModel)this.getParentModel()).renderCloak(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

