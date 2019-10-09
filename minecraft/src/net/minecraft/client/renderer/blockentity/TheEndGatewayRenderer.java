package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;

@Environment(EnvType.CLIENT)
public class TheEndGatewayRenderer extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
	private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("textures/entity/end_gateway_beam.png");

	public TheEndGatewayRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		super(blockEntityRenderDispatcher);
	}

	public void render(
		TheEndGatewayBlockEntity theEndGatewayBlockEntity,
		double d,
		double e,
		double f,
		float g,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		int j
	) {
		if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown()) {
			float h = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(g) : theEndGatewayBlockEntity.getCooldownPercent(g);
			double k = theEndGatewayBlockEntity.isSpawning() ? 256.0 - e : 50.0;
			h = Mth.sin(h * (float) Math.PI);
			int l = Mth.floor((double)h * k);
			float[] fs = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
			long m = theEndGatewayBlockEntity.getLevel().getGameTime();
			BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, g, h, m, 0, l, fs, 0.15F, 0.175F);
			BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, g, h, m, 0, -l, fs, 0.15F, 0.175F);
		}

		super.render(theEndGatewayBlockEntity, d, e, f, g, poseStack, multiBufferSource, i, j);
	}

	@Override
	protected int getPasses(double d) {
		return super.getPasses(d) + 1;
	}

	@Override
	protected float getOffset() {
		return 1.0F;
	}
}
