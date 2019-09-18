/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@Environment(value=EnvType.CLIENT)
public class VillagerTradeItemLayer<T extends LivingEntity>
extends RenderLayer<T, VillagerModel<T>> {
    private final ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

    public VillagerTradeItemLayer(RenderLayerParent<T, VillagerModel<T>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        boolean bl;
        ItemStack itemStack = ((LivingEntity)livingEntity).getItemBySlot(EquipmentSlot.MAINHAND);
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        Block block = Block.byItem(item);
        RenderSystem.pushMatrix();
        boolean bl2 = bl = this.itemRenderer.isGui3d(itemStack) && RenderType.getRenderLayer(block.defaultBlockState()) == RenderType.TRANSLUCENT;
        if (bl) {
            RenderSystem.depthMask(false);
        }
        RenderSystem.translatef(0.0f, 0.4f, -0.4f);
        RenderSystem.rotatef(180.0f, 1.0f, 0.0f, 0.0f);
        this.itemRenderer.renderWithMobState(itemStack, (LivingEntity)livingEntity, ItemTransforms.TransformType.GROUND, false);
        if (bl) {
            RenderSystem.depthMask(true);
        }
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

