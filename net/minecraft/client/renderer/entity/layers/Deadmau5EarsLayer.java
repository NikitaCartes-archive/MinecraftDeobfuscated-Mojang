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
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;

@Environment(value=EnvType.CLIENT)
public class Deadmau5EarsLayer
extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    public Deadmau5EarsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, AbstractClientPlayer abstractClientPlayer, float f, float g, float h, float j, float k, float l) {
        if (!"deadmau5".equals(abstractClientPlayer.getName().getString()) || !abstractClientPlayer.isSkinLoaded() || abstractClientPlayer.isInvisible()) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(abstractClientPlayer.getSkinTextureLocation()));
        int m = LivingEntityRenderer.getOverlayCoords(abstractClientPlayer, 0.0f);
        for (int n = 0; n < 2; ++n) {
            float o = Mth.lerp(h, abstractClientPlayer.yRotO, abstractClientPlayer.getYRot()) - Mth.lerp(h, abstractClientPlayer.yBodyRotO, abstractClientPlayer.yBodyRot);
            float p = Mth.lerp(h, abstractClientPlayer.xRotO, abstractClientPlayer.getXRot());
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(o));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(p));
            poseStack.translate(0.375f * (float)(n * 2 - 1), 0.0, 0.0);
            poseStack.translate(0.0, -0.375, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-p));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-o));
            float q = 1.3333334f;
            poseStack.scale(1.3333334f, 1.3333334f, 1.3333334f);
            ((PlayerModel)this.getParentModel()).renderEars(poseStack, vertexConsumer, i, m);
            poseStack.popPose();
        }
    }
}

