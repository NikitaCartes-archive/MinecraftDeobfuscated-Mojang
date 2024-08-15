package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

@Environment(EnvType.CLIENT)
public abstract class StuckInBodyLayer<M extends PlayerModel> extends RenderLayer<PlayerRenderState, M> {
	private final Model model;
	private final ResourceLocation texture;
	private final StuckInBodyLayer.PlacementStyle placementStyle;

	public StuckInBodyLayer(
		LivingEntityRenderer<?, PlayerRenderState, M> livingEntityRenderer,
		Model model,
		ResourceLocation resourceLocation,
		StuckInBodyLayer.PlacementStyle placementStyle
	) {
		super(livingEntityRenderer);
		this.model = model;
		this.texture = resourceLocation;
		this.placementStyle = placementStyle;
	}

	protected abstract int numStuck(PlayerRenderState playerRenderState);

	private void renderStuckItem(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, float h) {
		float j = Mth.sqrt(f * f + h * h);
		float k = (float)(Math.atan2((double)f, (double)h) * 180.0F / (float)Math.PI);
		float l = (float)(Math.atan2((double)g, (double)j) * 180.0F / (float)Math.PI);
		poseStack.mulPose(Axis.YP.rotationDegrees(k - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(l));
		this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.model.renderType(this.texture)), i, OverlayTexture.NO_OVERLAY);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, PlayerRenderState playerRenderState, float f, float g) {
		int j = this.numStuck(playerRenderState);
		if (j > 0) {
			RandomSource randomSource = RandomSource.create((long)playerRenderState.id);

			for (int k = 0; k < j; k++) {
				poseStack.pushPose();
				ModelPart modelPart = this.getParentModel().getRandomBodyPart(randomSource);
				ModelPart.Cube cube = modelPart.getRandomCube(randomSource);
				modelPart.translateAndRotate(poseStack);
				float h = randomSource.nextFloat();
				float l = randomSource.nextFloat();
				float m = randomSource.nextFloat();
				if (this.placementStyle == StuckInBodyLayer.PlacementStyle.ON_SURFACE) {
					int n = randomSource.nextInt(3);
					switch (n) {
						case 0:
							h = snapToFace(h);
							break;
						case 1:
							l = snapToFace(l);
							break;
						default:
							m = snapToFace(m);
					}
				}

				poseStack.translate(Mth.lerp(h, cube.minX, cube.maxX) / 16.0F, Mth.lerp(l, cube.minY, cube.maxY) / 16.0F, Mth.lerp(m, cube.minZ, cube.maxZ) / 16.0F);
				this.renderStuckItem(poseStack, multiBufferSource, i, -(h * 2.0F - 1.0F), -(l * 2.0F - 1.0F), -(m * 2.0F - 1.0F));
				poseStack.popPose();
			}
		}
	}

	private static float snapToFace(float f) {
		return f > 0.5F ? 1.0F : 0.5F;
	}

	@Environment(EnvType.CLIENT)
	public static enum PlacementStyle {
		IN_CUBE,
		ON_SURFACE;
	}
}
