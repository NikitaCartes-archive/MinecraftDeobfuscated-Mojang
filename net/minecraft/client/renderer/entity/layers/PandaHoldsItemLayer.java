/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PandaModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class PandaHoldsItemLayer
extends RenderLayer<Panda, PandaModel<Panda>> {
    public PandaHoldsItemLayer(RenderLayerParent<Panda, PandaModel<Panda>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Panda panda, float f, float g, float h, float j, float k, float l) {
        ItemStack itemStack = panda.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!panda.isSitting() || panda.isScared()) {
            return;
        }
        float m = -0.6f;
        float n = 1.4f;
        if (panda.isEating()) {
            m -= 0.2f * Mth.sin(j * 0.6f) + 0.2f;
            n -= 0.09f * Mth.sin(j * 0.6f);
        }
        poseStack.pushPose();
        poseStack.translate(0.1f, n, m);
        Minecraft.getInstance().getItemInHandRenderer().renderItem(panda, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
        poseStack.popPose();
    }
}

