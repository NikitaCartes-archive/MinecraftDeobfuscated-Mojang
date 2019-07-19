package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.EntityBlockRenderer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

@Environment(EnvType.CLIENT)
public class AnimatedEntityBlockRenderer {
	public void renderSingleBlock(Block block, float f) {
		GlStateManager.color4f(f, f, f, 1.0F);
		GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);
		EntityBlockRenderer.instance.renderByItem(new ItemStack(block));
	}
}
