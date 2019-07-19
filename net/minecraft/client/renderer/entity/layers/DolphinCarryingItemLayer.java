/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.DolphinModel;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockLayer;
import net.minecraft.world.level.block.Block;

@Environment(value=EnvType.CLIENT)
public class DolphinCarryingItemLayer
extends RenderLayer<Dolphin, DolphinModel<Dolphin>> {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public DolphinCarryingItemLayer(RenderLayerParent<Dolphin, DolphinModel<Dolphin>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(Dolphin dolphin, float f, float g, float h, float i, float j, float k, float l) {
        ItemStack itemStack2;
        boolean bl = dolphin.getMainArm() == HumanoidArm.RIGHT;
        ItemStack itemStack = bl ? dolphin.getOffhandItem() : dolphin.getMainHandItem();
        ItemStack itemStack3 = itemStack2 = bl ? dolphin.getMainHandItem() : dolphin.getOffhandItem();
        if (itemStack.isEmpty() && itemStack2.isEmpty()) {
            return;
        }
        this.renderItemOnNose(dolphin, itemStack2);
    }

    private void renderItemOnNose(LivingEntity livingEntity, ItemStack itemStack) {
        boolean bl;
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        Block block = Block.byItem(item);
        GlStateManager.pushMatrix();
        boolean bl2 = bl = this.itemRenderer.isGui3d(itemStack) && block.getRenderLayer() == BlockLayer.TRANSLUCENT;
        if (bl) {
            GlStateManager.depthMask(false);
        }
        float f = 1.0f;
        float g = -1.0f;
        float h = Mth.abs(livingEntity.xRot) / 60.0f;
        if (livingEntity.xRot < 0.0f) {
            GlStateManager.translatef(0.0f, 1.0f - h * 0.5f, -1.0f + h * 0.5f);
        } else {
            GlStateManager.translatef(0.0f, 1.0f + h * 0.8f, -1.0f + h * 0.2f);
        }
        this.itemRenderer.renderWithMobState(itemStack, livingEntity, ItemTransforms.TransformType.GROUND, false);
        if (bl) {
            GlStateManager.depthMask(true);
        }
        GlStateManager.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

