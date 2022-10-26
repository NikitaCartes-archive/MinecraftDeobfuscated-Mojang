/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IronGolemModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class IronGolemFlowerLayer
extends RenderLayer<IronGolem, IronGolemModel<IronGolem>> {
    private final BlockRenderDispatcher blockRenderer;

    public IronGolemFlowerLayer(RenderLayerParent<IronGolem, IronGolemModel<IronGolem>> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, IronGolem ironGolem, float f, float g, float h, float j, float k, float l) {
        if (ironGolem.getOfferFlowerTick() == 0) {
            return;
        }
        poseStack.pushPose();
        ModelPart modelPart = ((IronGolemModel)this.getParentModel()).getFlowerHoldingArm();
        modelPart.translateAndRotate(poseStack);
        poseStack.translate(-1.1875f, 1.0625f, -0.9375f);
        poseStack.translate(0.5f, 0.5f, 0.5f);
        float m = 0.5f;
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0f));
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.blockRenderer.renderSingleBlock(Blocks.POPPY.defaultBlockState(), poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

