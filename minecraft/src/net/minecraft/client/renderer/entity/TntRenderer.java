package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.state.TntRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.PrimedTnt;

@Environment(EnvType.CLIENT)
public class TntRenderer extends EntityRenderer<PrimedTnt, TntRenderState> {
	private final BlockRenderDispatcher blockRenderer;

	public TntRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.shadowRadius = 0.5F;
		this.blockRenderer = context.getBlockRenderDispatcher();
	}

	public void render(TntRenderState tntRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.5F, 0.0F);
		float f = tntRenderState.fuseRemainingInTicks;
		if (tntRenderState.fuseRemainingInTicks < 10.0F) {
			float g = 1.0F - tntRenderState.fuseRemainingInTicks / 10.0F;
			g = Mth.clamp(g, 0.0F, 1.0F);
			g *= g;
			g *= g;
			float h = 1.0F + g * 0.3F;
			poseStack.scale(h, h, h);
		}

		poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
		poseStack.translate(-0.5F, -0.5F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
		if (tntRenderState.blockState != null) {
			TntMinecartRenderer.renderWhiteSolidBlock(this.blockRenderer, tntRenderState.blockState, poseStack, multiBufferSource, i, (int)f / 5 % 2 == 0);
		}

		poseStack.popPose();
		super.render(tntRenderState, poseStack, multiBufferSource, i);
	}

	public TntRenderState createRenderState() {
		return new TntRenderState();
	}

	public void extractRenderState(PrimedTnt primedTnt, TntRenderState tntRenderState, float f) {
		super.extractRenderState(primedTnt, tntRenderState, f);
		tntRenderState.fuseRemainingInTicks = (float)primedTnt.getFuse() - f + 1.0F;
		tntRenderState.blockState = primedTnt.getBlockState();
	}
}
