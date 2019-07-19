/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@Environment(value=EnvType.CLIENT)
public class AnimatedEntityBlockRenderer {
    public void renderSingleBlock(Block block, float f) {
        GlStateManager.color4f(f, f, f, 1.0f);
        GlStateManager.rotatef(90.0f, 0.0f, 1.0f, 0.0f);
        EntityBlockRenderer.instance.renderByItem(new ItemStack(block));
    }
}

