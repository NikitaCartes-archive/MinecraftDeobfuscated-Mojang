/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class CrossedArmsItemLayer<T extends LivingEntity, M extends EntityModel<T>>
extends RenderLayer<T, M> {
    public CrossedArmsItemLayer(RenderLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        poseStack.pushPose();
        poseStack.translate(0.0, 0.4f, -0.4f);
        poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0f));
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.MAINHAND);
        Minecraft.getInstance().getItemInHandRenderer().renderItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
        poseStack.popPose();
    }
}

