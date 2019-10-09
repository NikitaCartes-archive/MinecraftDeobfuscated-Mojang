/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SnowGolemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

@Environment(value=EnvType.CLIENT)
public class SnowGolemHeadLayer
extends RenderLayer<SnowGolem, SnowGolemModel<SnowGolem>> {
    public SnowGolemHeadLayer(RenderLayerParent<SnowGolem, SnowGolemModel<SnowGolem>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, SnowGolem snowGolem, float f, float g, float h, float j, float k, float l, float m) {
        if (snowGolem.isInvisible() || !snowGolem.hasPumpkin()) {
            return;
        }
        poseStack.pushPose();
        ((SnowGolemModel)this.getParentModel()).getHead().translateAndRotate(poseStack, 0.0625f);
        float n = 0.625f;
        poseStack.translate(0.0, -0.34375, 0.0);
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        poseStack.scale(0.625f, -0.625f, -0.625f);
        ItemStack itemStack = new ItemStack(Blocks.CARVED_PUMPKIN);
        Minecraft.getInstance().getItemRenderer().renderStatic(snowGolem, itemStack, ItemTransforms.TransformType.HEAD, false, poseStack, multiBufferSource, snowGolem.level, snowGolem.getLightColor(), LivingEntityRenderer.getOverlayCoords(snowGolem, 0.0f));
        poseStack.popPose();
    }
}

