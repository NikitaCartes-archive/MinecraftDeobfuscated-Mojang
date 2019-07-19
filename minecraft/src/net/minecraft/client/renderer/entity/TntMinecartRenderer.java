package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class TntMinecartRenderer extends MinecartRenderer<MinecartTNT> {
	public TntMinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	protected void renderMinecartContents(MinecartTNT minecartTNT, float f, BlockState blockState) {
		int i = minecartTNT.getFuse();
		if (i > -1 && (float)i - f + 1.0F < 10.0F) {
			float g = 1.0F - ((float)i - f + 1.0F) / 10.0F;
			g = Mth.clamp(g, 0.0F, 1.0F);
			g *= g;
			g *= g;
			float h = 1.0F + g * 0.3F;
			GlStateManager.scalef(h, h, h);
		}

		super.renderMinecartContents(minecartTNT, f, blockState);
		if (i > -1 && i / 5 % 2 == 0) {
			BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
			GlStateManager.disableTexture();
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, (1.0F - ((float)i - f + 1.0F) / 100.0F) * 0.8F);
			GlStateManager.pushMatrix();
			blockRenderDispatcher.renderSingleBlock(Blocks.TNT.defaultBlockState(), 1.0F);
			GlStateManager.popMatrix();
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.enableTexture();
		}
	}
}
