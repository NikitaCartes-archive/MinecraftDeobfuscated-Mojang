/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class ItemInHandLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public ItemInHandLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l, float m) {
        ItemStack itemStack2;
        boolean bl = ((LivingEntity)livingEntity).getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemStack = bl ? ((LivingEntity)livingEntity).getOffhandItem() : ((LivingEntity)livingEntity).getMainHandItem();
        ItemStack itemStack3 = itemStack2 = bl ? ((LivingEntity)livingEntity).getMainHandItem() : ((LivingEntity)livingEntity).getOffhandItem();
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        if (((EntityModel)this.getParentModel()).young) {
            float n = 0.5f;
            poseStack.translate(0.0, 0.75, 0.0);
            poseStack.scale(0.5f, 0.5f, 0.5f);
        }
        this.renderArmWithItem((LivingEntity)livingEntity, itemStack2, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, multiBufferSource);
        this.renderArmWithItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, multiBufferSource);
        poseStack.popPose();
    }

    private void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemTransforms.TransformType transformType, HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        if (itemStack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        ((ArmedModel)this.getParentModel()).translateToHand(0.0625f, humanoidArm, poseStack);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(-90.0f));
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0f));
        boolean bl = humanoidArm == HumanoidArm.LEFT;
        poseStack.translate((float)(bl ? -1 : 1) / 16.0f, 0.125, -0.625);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(livingEntity, itemStack, transformType, bl, poseStack, multiBufferSource);
        poseStack.popPose();
    }
}

