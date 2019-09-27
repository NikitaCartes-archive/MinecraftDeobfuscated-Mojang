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
		TheEndGatewayBlockEntity theEndGatewayBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i
	) {
		if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown()) {
			float h = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(g) : theEndGatewayBlockEntity.getCooldownPercent(g);
			double j = theEndGatewayBlockEntity.isSpawning() ? 256.0 - e : 50.0;
			h = Mth.sin(h * (float) Math.PI);
			int k = Mth.floor((double)h * j);
			float[] fs = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColors() : DyeColor.PURPLE.getTextureDiffuseColors();
			long l = theEndGatewayBlockEntity.getLevel().getGameTime();
			BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, g, h, l, 0, k, fs, 0.15F, 0.175F);
			BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, g, h, l, 0, -k, fs, 0.15F, 0.175F);
		}

		super.render(theEndGatewayBlockEntity, d, e, f, g, poseStack, multiBufferSource, i);
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
