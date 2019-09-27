/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EndermanModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class CarriedBlockLayer
extends RenderLayer<EnderMan, EndermanModel<EnderMan>> {
    public CarriedBlockLayer(RenderLayerParent<EnderMan, EndermanModel<EnderMan>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, EnderMan enderMan, float f, float g, float h, float j, float k, float l, float m) {
        BlockState blockState = enderMan.getCarriedBlock();
        if (blockState == null) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(0.0, 0.6875, -0.75);
        poseStack.mulPose(Vector3f.XP.rotation(20.0f, true));
        poseStack.mulPose(Vector3f.YP.rotation(45.0f, true));
        poseStack.translate(0.25, 0.1875, 0.25);
        float n = 0.5f;
        poseStack.scale(-0.5f, -0.5f, 0.5f);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, 0, 10);
        poseStack.popPose();
    }
}

