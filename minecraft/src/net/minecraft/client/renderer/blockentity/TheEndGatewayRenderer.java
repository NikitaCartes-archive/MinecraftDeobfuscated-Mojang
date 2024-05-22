package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;

@Environment(EnvType.CLIENT)
public class TheEndGatewayRenderer extends TheEndPortalRenderer<TheEndGatewayBlockEntity> {
	private static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/end_gateway_beam.png");

	public TheEndGatewayRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	public void render(TheEndGatewayBlockEntity theEndGatewayBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		if (theEndGatewayBlockEntity.isSpawning() || theEndGatewayBlockEntity.isCoolingDown()) {
			float g = theEndGatewayBlockEntity.isSpawning() ? theEndGatewayBlockEntity.getSpawnPercent(f) : theEndGatewayBlockEntity.getCooldownPercent(f);
			double d = theEndGatewayBlockEntity.isSpawning() ? (double)theEndGatewayBlockEntity.getLevel().getMaxBuildHeight() : 50.0;
			g = Mth.sin(g * (float) Math.PI);
			int k = Mth.floor((double)g * d);
			int l = theEndGatewayBlockEntity.isSpawning() ? DyeColor.MAGENTA.getTextureDiffuseColor() : DyeColor.PURPLE.getTextureDiffuseColor();
			long m = theEndGatewayBlockEntity.getLevel().getGameTime();
			BeaconRenderer.renderBeaconBeam(poseStack, multiBufferSource, BEAM_LOCATION, f, g, m, -k, k * 2, l, 0.15F, 0.175F);
		}

		super.render(theEndGatewayBlockEntity, f, poseStack, multiBufferSource, i, j);
	}

	@Override
	protected float getOffsetUp() {
		return 1.0F;
	}

	@Override
	protected float getOffsetDown() {
		return 0.0F;
	}

	@Override
	protected RenderType renderType() {
		return RenderType.endGateway();
	}

	@Override
	public int getViewDistance() {
		return 256;
	}
}
