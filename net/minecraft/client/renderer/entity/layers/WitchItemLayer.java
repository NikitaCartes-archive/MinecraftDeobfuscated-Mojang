/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;

@Environment(value=EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity>
extends RenderLayer<T, WitchModel<T>> {
    public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        ItemStack itemStack = ((LivingEntity)livingEntity).getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (((WitchModel)this.getParentModel()).young) {
            poseStack.translate(0.0, 0.625, 0.0);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(20.0f));
            float n = 0.5f;
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        ((WitchModel)this.getParentModel()).getNose().translateAndRotate(poseStack, 0.0625f);
        poseStack.translate(-0.0625, 0.53125, 0.21875);
        Item item = itemStack.getItem();
        if (Block.byItem(item).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
            poseStack.translate(0.0, 0.0625, -0.25);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(30.0f));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-5.0f));
            float o = 0.375f;
            poseStack.scale(0.375f, -0.375f, 0.375f);
        } else if (item == Items.BOW) {
            poseStack.translate(0.0, 0.125, -0.125);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-45.0f));
            float o = 0.625f;
            poseStack.scale(0.625f, -0.625f, 0.625f);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-100.0f));
            poseStack.mulPose(Vector3f.YP.rotationDegrees(-20.0f));
        } else {
            poseStack.translate(0.1875, 0.1875, 0.0);
            float o = 0.875f;
            poseStack.scale(0.875f, 0.875f, 0.875f);
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-20.0f));
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-60.0f));
            poseStack.mulPose(Vector3f.ZP.rotationDegrees(-30.0f));
        }
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-15.0f));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(40.0f));
        Minecraft.getInstance().getItemInHandRenderer().renderItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, false, poseStack, multiBufferSource);
        poseStack.popPose();
    }
}

