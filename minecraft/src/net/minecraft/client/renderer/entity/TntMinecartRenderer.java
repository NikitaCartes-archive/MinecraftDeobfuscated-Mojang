package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.MinecartTntRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class TntMinecartRenderer extends AbstractMinecartRenderer<MinecartTNT, MinecartTntRenderState> {
	private final BlockRenderDispatcher blockRenderer;

	public TntMinecartRenderer(EntityRendererProvider.Context context) {
		super(context, ModelLayers.TNT_MINECART);
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	protected void renderMinecartContents(
		MinecartTntRenderState minecartTntRenderState, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		float f = minecartTntRenderState.fuseRemainingInTicks;
		if (f > -1.0F && f < 10.0F) {
			float g = 1.0F - f / 10.0F;
			g = Mth.clamp(g, 0.0F, 1.0F);
			g *= g;
			g *= g;
			float h = 1.0F + g * 0.3F;
			poseStack.scale(h, h, h);
		}

		renderWhiteSolidBlock(this.blockRenderer, blockState, poseStack, multiBufferSource, i, f > -1.0F && (int)f / 5 % 2 == 0);
	}

	public static void renderWhiteSolidBlock(
		BlockRenderDispatcher blockRenderDispatcher, BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, boolean bl
	) {
		int j;
		if (bl) {
			j = OverlayTexture.pack(OverlayTexture.u(1.0F), 10);
		} else {
			j = OverlayTexture.NO_OVERLAY;
		}

		blockRenderDispatcher.renderSingleBlock(blockState, poseStack, multiBufferSource, i, j);
	}

	public MinecartTntRenderState createRenderState() {
		return new MinecartTntRenderState();
	}

	public void extractRenderState(MinecartTNT minecartTNT, MinecartTntRenderState minecartTntRenderState, float f) {
		super.extractRenderState(minecartTNT, minecartTntRenderState, f);
		minecartTntRenderState.fuseRemainingInTicks = minecartTNT.getFuse() > -1 ? (float)minecartTNT.getFuse() - f + 1.0F : -1.0F;
	}
}
