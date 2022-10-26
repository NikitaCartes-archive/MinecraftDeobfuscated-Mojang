/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class CarriedBlockLayer
extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
    private final BlockRenderDispatcher blockRenderer;

    public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EnderMan enderMan, float f, float g, float h, float j, float k, float l) {
        BlockState blockState = enderMan.getCarriedBlock();
        if (blockState == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0f, 0.6875f, -0.75f);
        poseStack.mulPose(Axis.XP.rotationDegrees(20.0f));
        poseStack.mulPose(Axis.YP.rotationDegrees(45.0f));
        poseStack.translate(0.25f, 0.1875f, 0.25f);
        float m = 0.5f;
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0f));
        this.blockRenderer.renderSingleBlock(blockState, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

