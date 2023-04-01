package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.voting.rules.Rules;
import net.minecraft.world.entity.GlowSquid;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public class GlowSquidRenderer extends SquidRenderer<GlowSquid> {
	public static final StencilRenderer.Triangle[] FACES = StencilRenderer.createNSphere(2);
	private static final ResourceLocation GLOW_SQUID_LOCATION = new ResourceLocation("textures/entity/squid/glow_squid.png");

	public GlowSquidRenderer(EntityRendererProvider.Context context, SquidModel<GlowSquid> squidModel) {
		super(context, squidModel);
	}

	public void render(GlowSquid glowSquid, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		if (Rules.GLOWING_GLOW_SQUIDS.get()) {
			float h = this.getBob(glowSquid, g) * 3.0F;
			if (h > 0.0F) {
				int j = FastColor.ARGB32.color(32, 0, 192, 255);
				poseStack.pushPose();
				poseStack.scale(h, h, h);
				Matrix4f matrix4f = poseStack.last().pose();
				StencilRenderer.render(FACES, matrix4f, multiBufferSource, j);
				poseStack.popPose();
			}
		}

		super.render(glowSquid, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(GlowSquid glowSquid) {
		return GLOW_SQUID_LOCATION;
	}

	protected int getBlockLightLevel(GlowSquid glowSquid, BlockPos blockPos) {
		int i = (int)Mth.clampedLerp(0.0F, 15.0F, 1.0F - (float)glowSquid.getDarkTicksRemaining() / 10.0F);
		return i == 15 ? 15 : Math.max(i, super.getBlockLightLevel(glowSquid, blockPos));
	}
}
