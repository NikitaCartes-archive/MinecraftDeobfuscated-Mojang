package net.minecraft.client.renderer.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.SquidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.GlowSquid;

@Environment(EnvType.CLIENT)
public class GlowSquidRenderer extends SquidRenderer<GlowSquid> {
	private static final ResourceLocation GLOW_SQUID_LOCATION = new ResourceLocation("textures/entity/squid/glow_squid.png");

	public GlowSquidRenderer(EntityRendererProvider.Context context, SquidModel<GlowSquid> squidModel) {
		super(context, squidModel);
	}

	public ResourceLocation getTextureLocation(GlowSquid glowSquid) {
		return GLOW_SQUID_LOCATION;
	}

	protected int getBlockLightLevel(GlowSquid glowSquid, BlockPos blockPos) {
		int i = (int)Mth.clampedLerp(0.0, 15.0, (double)(1.0F - (float)glowSquid.getDarkTicksRemaining() / 10.0F));
		return i == 15 ? 15 : Math.max(i, super.getBlockLightLevel(glowSquid, blockPos));
	}
}
