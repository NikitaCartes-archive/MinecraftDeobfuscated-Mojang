/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.DolphinModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.ItemStack;

@Environment(value=EnvType.CLIENT)
public class DolphinCarryingItemLayer
extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
    private final ItemInHandRenderer itemInHandRenderer;

    public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> renderLayerParent, ItemInHandRenderer itemInHandRenderer) {
        super(renderLayerParent);
        this.itemInHandRenderer = itemInHandRenderer;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Dolphin dolphin, float f, float g, float h, float j, float k, float l) {
        boolean bl = dolphin.getMainArm() == HumanoidArm.RIGHT;
        poseStack.pushPose();
        float m = 1.0f;
        float n = -1.0f;
        float o = Mth.abs(dolphin.getXRot()) / 60.0f;
        if (dolphin.getXRot() < 0.0f) {
            poseStack.translate(0.0f, 1.0f - o * 0.5f, -1.0f + o * 0.5f);
        } else {
            poseStack.translate(0.0f, 1.0f + o * 0.8f, -1.0f + o * 0.2f);
        }
        ItemStack itemStack = bl ? dolphin.getMainHandItem() : dolphin.getOffhandItem();
        this.itemInHandRenderer.renderItem(dolphin, itemStack, ItemTransforms.TransformType.GROUND, false, poseStack, multiBufferSource, i);
        poseStack.popPose();
    }
}

