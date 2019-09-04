/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

@Environment(value=EnvType.CLIENT)
public class CampfireRenderer
extends BlockEntityRenderer<CampfireBlockEntity> {
    @Override
    public void render(CampfireBlockEntity campfireBlockEntity, double d, double e, double f, float g, int i) {
        Direction direction = campfireBlockEntity.getBlockState().getValue(CampfireBlock.FACING);
        NonNullList<ItemStack> nonNullList = campfireBlockEntity.getItems();
        for (int j = 0; j < nonNullList.size(); ++j) {
            ItemStack itemStack = nonNullList.get(j);
            if (itemStack == ItemStack.EMPTY) continue;
            RenderSystem.pushMatrix();
            RenderSystem.translatef((float)d + 0.5f, (float)e + 0.44921875f, (float)f + 0.5f);
            Direction direction2 = Direction.from2DDataValue((j + direction.get2DDataValue()) % 4);
            RenderSystem.rotatef(-direction2.toYRot(), 0.0f, 1.0f, 0.0f);
            RenderSystem.rotatef(90.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.translatef(-0.3125f, -0.3125f, 0.0f);
            RenderSystem.scalef(0.375f, 0.375f, 0.375f);
            Minecraft.getInstance().getItemRenderer().renderStatic(itemStack, ItemTransforms.TransformType.FIXED);
            RenderSystem.popMatrix();
        }
    }
}

