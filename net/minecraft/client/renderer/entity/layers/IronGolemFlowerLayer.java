/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class IronGolemFlowerLayer
extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolem ironGolem, float f, float g, float h, float j, float k, float l, float m) {
        if (ironGolem.getOfferFlowerTick() == 0) {
            return;
        }
        poseStack.pushPose();
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.mulPose(Vector3f.XP.rotation(5.0f + 180.0f * ((IronGolemModel)this.getParentModel()).getFlowerHoldingArm().xRot / (float)Math.PI, true));
        poseStack.mulPose(Vector3f.XP.rotation(90.0f, true));
        poseStack.translate(0.6875, -0.3125, 1.0625);
        float n = 0.5f;
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Vector3f.XP.rotation(180.0f, true));
        poseStack.translate(-0.5, -0.5, 0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.POPPY.defaultBlockState(), poseStack, multiBufferSource, i, 0, 10);
        poseStack.popPose();
    }
}

