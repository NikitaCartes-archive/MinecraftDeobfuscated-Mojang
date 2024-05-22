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
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;

@Environment(EnvType.CLIENT)
public class PaintingRenderer extends EntityRenderer<Painting> {
	public PaintingRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(Painting painting, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
		PaintingVariant paintingVariant = painting.getVariant().value();
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(this.getTextureLocation(painting)));
		PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
		this.renderPainting(
			poseStack,
			vertexConsumer,
			painting,
			paintingVariant.width(),
			paintingVariant.height(),
			paintingTextureManager.get(paintingVariant),
			paintingTextureManager.getBackSprite()
		);
		poseStack.popPose();
		super.render(painting, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Painting painting) {
		return Minecraft.getInstance().getPaintingTextures().getBackSprite().atlasLocation();
	}

	private void renderPainting(
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		Painting painting,
		int i,
		int j,
		TextureAtlasSprite textureAtlasSprite,
		TextureAtlasSprite textureAtlasSprite2
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
				int ac = painting.getBlockX();
				int ad = Mth.floor(painting.getY() + (double)((aa + ab) / 2.0F));
				int ae = painting.getBlockZ();
				Direction direction = painting.getDirection();
				if (direction == Direction.NORTH) {
					ac = Mth.floor(painting.getX() + (double)((y + z) / 2.0F));
				}

				if (direction == Direction.WEST) {
					ae = Mth.floor(painting.getZ() - (double)((y + z) / 2.0F));
				}

				if (direction == Direction.SOUTH) {
					ac = Mth.floor(painting.getX() - (double)((y + z) / 2.0F));
				}

				if (direction == Direction.EAST) {
					ae = Mth.floor(painting.getZ() + (double)((y + z) / 2.0F));
				}

				int af = LevelRenderer.getLightColor(painting.level(), new BlockPos(ac, ad, ae));
				float ag = textureAtlasSprite.getU((float)(d * (double)(i - w)));
				float ah = textureAtlasSprite.getU((float)(d * (double)(i - (w + 1))));
				float ai = textureAtlasSprite.getV((float)(e * (double)(j - x)));
				float aj = textureAtlasSprite.getV((float)(e * (double)(j - (x + 1))));
				this.vertex(pose, vertexConsumer, y, ab, ah, ai, -0.03125F, 0, 0, -1, af);
				this.vertex(pose, vertexConsumer, z, ab, ag, ai, -0.03125F, 0, 0, -1, af);
				this.vertex(pose, vertexConsumer, z, aa, ag, aj, -0.03125F, 0, 0, -1, af);
				this.vertex(pose, vertexConsumer, y, aa, ah, aj, -0.03125F, 0, 0, -1, af);
				this.vertex(pose, vertexConsumer, y, aa, l, m, 0.03125F, 0, 0, 1, af);
				this.vertex(pose, vertexConsumer, z, aa, k, m, 0.03125F, 0, 0, 1, af);
				this.vertex(pose, vertexConsumer, z, ab, k, n, 0.03125F, 0, 0, 1, af);
				this.vertex(pose, vertexConsumer, y, ab, l, n, 0.03125F, 0, 0, 1, af);
				this.vertex(pose, vertexConsumer, y, aa, o, q, -0.03125F, 0, 1, 0, af);
				this.vertex(pose, vertexConsumer, z, aa, p, q, -0.03125F, 0, 1, 0, af);
				this.vertex(pose, vertexConsumer, z, aa, p, r, 0.03125F, 0, 1, 0, af);
				this.vertex(pose, vertexConsumer, y, aa, o, r, 0.03125F, 0, 1, 0, af);
				this.vertex(pose, vertexConsumer, y, ab, o, q, 0.03125F, 0, -1, 0, af);
				this.vertex(pose, vertexConsumer, z, ab, p, q, 0.03125F, 0, -1, 0, af);
				this.vertex(pose, vertexConsumer, z, ab, p, r, -0.03125F, 0, -1, 0, af);
				this.vertex(pose, vertexConsumer, y, ab, o, r, -0.03125F, 0, -1, 0, af);
				this.vertex(pose, vertexConsumer, y, aa, t, u, 0.03125F, -1, 0, 0, af);
				this.vertex(pose, vertexConsumer, y, ab, t, v, 0.03125F, -1, 0, 0, af);
				this.vertex(pose, vertexConsumer, y, ab, s, v, -0.03125F, -1, 0, 0, af);
				this.vertex(pose, vertexConsumer, y, aa, s, u, -0.03125F, -1, 0, 0, af);
				this.vertex(pose, vertexConsumer, z, aa, t, u, -0.03125F, 1, 0, 0, af);
				this.vertex(pose, vertexConsumer, z, ab, t, v, -0.03125F, 1, 0, 0, af);
				this.vertex(pose, vertexConsumer, z, ab, s, v, 0.03125F, 1, 0, 0, af);
				this.vertex(pose, vertexConsumer, z, aa, s, u, 0.03125F, 1, 0, 0, af);
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
