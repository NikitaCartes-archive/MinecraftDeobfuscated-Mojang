/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.WitchModel;
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
    public void render(T livingEntity, float f, float g, float h, float i, float j, float k, float l) {
        ItemStack itemStack = ((LivingEntity)livingEntity).getMainHandItem();
        if (itemStack.isEmpty()) {
            return;
        }
        RenderSystem.color3f(1.0f, 1.0f, 1.0f);
        RenderSystem.pushMatrix();
        if (((WitchModel)this.getParentModel()).young) {
            RenderSystem.translatef(0.0f, 0.625f, 0.0f);
            RenderSystem.rotatef(-20.0f, -1.0f, 0.0f, 0.0f);
            float m = 0.5f;
            RenderSystem.scalef(0.5f, 0.5f, 0.5f);
        }
        ((WitchModel)this.getParentModel()).getNose().translateTo(0.0625f);
        RenderSystem.translatef(-0.0625f, 0.53125f, 0.21875f);
        Item item = itemStack.getItem();
        if (Block.byItem(item).defaultBlockState().getRenderShape() == RenderShape.ENTITYBLOCK_ANIMATED) {
            RenderSystem.translatef(0.0f, 0.0625f, -0.25f);
            RenderSystem.rotatef(30.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(-5.0f, 0.0f, 1.0f, 0.0f);
            float n = 0.375f;
            RenderSystem.scalef(0.375f, -0.375f, 0.375f);
        } else if (item == Items.BOW) {
            RenderSystem.translatef(0.0f, 0.125f, -0.125f);
            RenderSystem.rotatef(-45.0f, 0.0f, 1.0f, 0.0f);
            float n = 0.625f;
            RenderSystem.scalef(0.625f, -0.625f, 0.625f);
            RenderSystem.rotatef(-100.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(-20.0f, 0.0f, 1.0f, 0.0f);
        } else {
            RenderSystem.translatef(0.1875f, 0.1875f, 0.0f);
            float n = 0.875f;
            RenderSystem.scalef(0.875f, 0.875f, 0.875f);
            RenderSystem.rotatef(-20.0f, 0.0f, 0.0f, 1.0f);
            RenderSystem.rotatef(-60.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(-30.0f, 0.0f, 0.0f, 1.0f);
        }
        RenderSystem.rotatef(-15.0f, 1.0f, 0.0f, 0.0f);
        RenderSystem.rotatef(40.0f, 0.0f, 0.0f, 1.0f);
        Minecraft.getInstance().getItemInHandRenderer().renderItem((LivingEntity)livingEntity, itemStack, ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
        RenderSystem.popMatrix();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}

