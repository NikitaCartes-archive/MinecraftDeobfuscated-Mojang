/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitchModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@Environment(value=EnvType.CLIENT)
public class WitchItemLayer<T extends LivingEntity>
extends CrossedArmsItemLayer<T, WitchModel<T>> {
    public WitchItemLayer(RenderLayerParent<T, WitchModel<T>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
        super(renderLayerParent, itemInHandRenderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
        ItemStack itemStack = ((LivingEntity)livingEntity).getMainHandItem();
        poseStack.pushPose();
        if (itemStack.is(Items.POTION)) {
            ((WitchModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
            ((WitchModel)this.getParentModel()).getNose().translateAndRotate(poseStack);
            poseStack.translate(0.0625f, 0.25f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0f));
            poseStack.mulPose(Axis.XP.rotationDegrees(140.0f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0f));
            poseStack.translate(0.0f, -0.4f, 0.4f);
        }
        super.render(poseStack, multiBufferSource, i, livingEntity, f, g, h, j, k, l);
        poseStack.popPose();
    }
}

