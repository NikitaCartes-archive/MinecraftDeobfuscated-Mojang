package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.PaintingRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class PaintingRenderer extends EntityRenderer<Painting, PaintingRenderState> {
	public PaintingRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(PaintingRenderState paintingRenderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		PaintingVariant paintingVariant = paintingRenderState.variant;
		if (paintingVariant != null) {
			poseStack.pushPose();
			poseStack.mulPose(Axis.YP.rotationDegrees((float)(180 - paintingRenderState.direction.get2DDataValue() * 90)));
			PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
			TextureAtlasSprite textureAtlasSprite = paintingTextureManager.getBackSprite();
			VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolidZOffsetForward(textureAtlasSprite.atlasLocation()));
			this.renderPainting(
				poseStack,
				vertexConsumer,
				paintingRenderState.lightCoords,
				paintingVariant.width(),
				paintingVariant.height(),
				paintingTextureManager.get(paintingVariant),
				textureAtlasSprite
			);
			poseStack.popPose();
			super.render(paintingRenderState, poseStack, multiBufferSource, i);
		}
	}

	public PaintingRenderState createRenderState() {
		return new PaintingRenderState();
	}

	public void extractRenderState(Painting painting, PaintingRenderState paintingRenderState, float f) {
		super.extractRenderState(painting, paintingRenderState, f);
		Direction direction = painting.getDirection();
		PaintingVariant paintingVariant = painting.getVariant().value();
		paintingRenderState.direction = direction;
		paintingRenderState.variant = paintingVariant;
		int i = paintingVariant.width();
		int j = paintingVariant.height();
		if (paintingRenderState.lightCoords.length != i * j) {
			paintingRenderState.lightCoords = new int[i * j];
		}

		float g = (float)(-i) / 2.0F;
		float h = (float)(-j) / 2.0F;
		Level level = painting.level();

		for (int k = 0; k < j; k++) {
			for (int l = 0; l < i; l++) {
				float m = (float)l + g + 0.5F;
				float n = (float)k + h + 0.5F;
				int o = painting.getBlockX();
				int p = Mth.floor(painting.getY() + (double)n);
				int q = painting.getBlockZ();
				switch (direction) {
					case NORTH:
						o = Mth.floor(painting.getX() + (double)m);
						break;
					case WEST:
						q = Mth.floor(painting.getZ() - (double)m);
						break;
					case SOUTH:
						o = Mth.floor(painting.getX() - (double)m);
						break;
					case EAST:
						q = Mth.floor(painting.getZ() + (double)m);
				}

				paintingRenderState.lightCoords[l + k * i] = LevelRenderer.getLightColor(level, new BlockPos(o, p, q));
			}
		}
	}

	private void renderPainting(
		PoseStack poseStack, VertexConsumer vertexConsumer, int[] is, int i, int j, TextureAtlasSprite textureAtlasSprite, TextureAtlasSprite textureAtlasSprite2
	) {
		PoseStack.Pose pose = poseStack.last();
		float f = (float)(-i) / 2.0F;
		float g = (float)(-j) / 2.0F;
		float h = 0.03125F;
		float k = textureAtlasSprite2.getU0();
		float l = textureAtlasSprite2.getU1();
		float m = textureAtlasSprite2.getV0();
		float n = textureAtlasSprite2.getV1();
		float o = textureAtlasSprite2.getU0();
		float p = textureAtlasSprite2.getU1();
		float q = textureAtlasSprite2.getV0();
		float r = textureAtlasSprite2.getV(0.0625F);
		float s = textureAtlasSprite2.getU0();
		float t = textureAtlasSprite2.getU(0.0625F);
		float u = textureAtlasSprite2.getV0();
		float v = textureAtlasSprite2.getV1();
		double d = 1.0 / (double)i;
		double e = 1.0 / (double)j;

		for (int w = 0; w < i; w++) {
			for (int x = 0; x < j; x++) {
				float y = f + (float)(w + 1);
				float z = f + (float)w;
				float aa = g + (float)(x + 1);
				float ab = g + (float)x;
				int ac = is[w + x * i];
				float ad = textureAtlasSprite.getU((float)(d * (double)(i - w)));
				float ae = textureAtlasSprite.getU((float)(d * (double)(i - (w + 1))));
				float af = textureAtlasSprite.getV((float)(e * (double)(j - x)));
				float ag = textureAtlasSprite.getV((float)(e * (double)(j - (x + 1))));
				this.vertex(pose, vertexConsumer, y, ab, ae, af, -0.03125F, 0, 0, -1, ac);
				this.vertex(pose, vertexConsumer, z, ab, ad, af, -0.03125F, 0, 0, -1, ac);
				this.vertex(pose, vertexConsumer, z, aa, ad, ag, -0.03125F, 0, 0, -1, ac);
				this.vertex(pose, vertexConsumer, y, aa, ae, ag, -0.03125F, 0, 0, -1, ac);
				this.vertex(pose, vertexConsumer, y, aa, l, m, 0.03125F, 0, 0, 1, ac);
				this.vertex(pose, vertexConsumer, z, aa, k, m, 0.03125F, 0, 0, 1, ac);
				this.vertex(pose, vertexConsumer, z, ab, k, n, 0.03125F, 0, 0, 1, ac);
				this.vertex(pose, vertexConsumer, y, ab, l, n, 0.03125F, 0, 0, 1, ac);
				this.vertex(pose, vertexConsumer, y, aa, o, q, -0.03125F, 0, 1, 0, ac);
				this.vertex(pose, vertexConsumer, z, aa, p, q, -0.03125F, 0, 1, 0, ac);
				this.vertex(pose, vertexConsumer, z, aa, p, r, 0.03125F, 0, 1, 0, ac);
				this.vertex(pose, vertexConsumer, y, aa, o, r, 0.03125F, 0, 1, 0, ac);
				this.vertex(pose, vertexConsumer, y, ab, o, q, 0.03125F, 0, -1, 0, ac);
				this.vertex(pose, vertexConsumer, z, ab, p, q, 0.03125F, 0, -1, 0, ac);
				this.vertex(pose, vertexConsumer, z, ab, p, r, -0.03125F, 0, -1, 0, ac);
				this.vertex(pose, vertexConsumer, y, ab, o, r, -0.03125F, 0, -1, 0, ac);
				this.vertex(pose, vertexConsumer, y, aa, t, u, 0.03125F, -1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, y, ab, t, v, 0.03125F, -1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, y, ab, s, v, -0.03125F, -1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, y, aa, s, u, -0.03125F, -1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, z, aa, t, u, -0.03125F, 1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, z, ab, t, v, -0.03125F, 1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, z, ab, s, v, 0.03125F, 1, 0, 0, ac);
				this.vertex(pose, vertexConsumer, z, aa, s, u, 0.03125F, 1, 0, 0, ac);
			}
		}
	}

	private void vertex(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, int k, int l, int m, int n) {
		vertexConsumer.addVertex(pose, f, g, j)
			.setColor(-1)
			.setUv(h, i)
			.setOverlay(OverlayTexture.NO_OVERLAY)
			.setLight(n)
			.setNormal(pose, (float)k, (float)l, (float)m);
	}
}
