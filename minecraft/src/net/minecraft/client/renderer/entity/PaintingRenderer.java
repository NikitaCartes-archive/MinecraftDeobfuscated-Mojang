package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
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
import net.minecraft.world.entity.decoration.Motive;
import net.minecraft.world.entity.decoration.Painting;

@Environment(EnvType.CLIENT)
public class PaintingRenderer extends EntityRenderer<Painting> {
	public PaintingRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	public void render(Painting painting, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		poseStack.pushPose();
		poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - f));
		Motive motive = painting.motive;
		float h = 0.0625F;
		poseStack.scale(0.0625F, 0.0625F, 0.0625F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(this.getTextureLocation(painting)));
		PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
		this.renderPainting(
			poseStack, vertexConsumer, painting, motive.getWidth(), motive.getHeight(), paintingTextureManager.get(motive), paintingTextureManager.getBackSprite()
		);
		poseStack.popPose();
		super.render(painting, f, g, poseStack, multiBufferSource, i);
	}

	public ResourceLocation getTextureLocation(Painting painting) {
		return Minecraft.getInstance().getPaintingTextures().getBackSprite().atlas().location();
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
		Matrix4f matrix4f = pose.pose();
		Matrix3f matrix3f = pose.normal();
		float f = (float)(-i) / 2.0F;
		float g = (float)(-j) / 2.0F;
		float h = 0.5F;
		float k = textureAtlasSprite2.getU0();
		float l = textureAtlasSprite2.getU1();
		float m = textureAtlasSprite2.getV0();
		float n = textureAtlasSprite2.getV1();
		float o = textureAtlasSprite2.getU0();
		float p = textureAtlasSprite2.getU1();
		float q = textureAtlasSprite2.getV0();
		float r = textureAtlasSprite2.getV(1.0);
		float s = textureAtlasSprite2.getU0();
		float t = textureAtlasSprite2.getU(1.0);
		float u = textureAtlasSprite2.getV0();
		float v = textureAtlasSprite2.getV1();
		int w = i / 16;
		int x = j / 16;
		double d = 16.0 / (double)w;
		double e = 16.0 / (double)x;

		for (int y = 0; y < w; y++) {
			for (int z = 0; z < x; z++) {
				float aa = f + (float)((y + 1) * 16);
				float ab = f + (float)(y * 16);
				float ac = g + (float)((z + 1) * 16);
				float ad = g + (float)(z * 16);
				int ae = Mth.floor(painting.getX());
				int af = Mth.floor(painting.getY() + (double)((ac + ad) / 2.0F / 16.0F));
				int ag = Mth.floor(painting.getZ());
				Direction direction = painting.getDirection();
				if (direction == Direction.NORTH) {
					ae = Mth.floor(painting.getX() + (double)((aa + ab) / 2.0F / 16.0F));
				}

				if (direction == Direction.WEST) {
					ag = Mth.floor(painting.getZ() - (double)((aa + ab) / 2.0F / 16.0F));
				}

				if (direction == Direction.SOUTH) {
					ae = Mth.floor(painting.getX() - (double)((aa + ab) / 2.0F / 16.0F));
				}

				if (direction == Direction.EAST) {
					ag = Mth.floor(painting.getZ() + (double)((aa + ab) / 2.0F / 16.0F));
				}

				int ah = LevelRenderer.getLightColor(painting.level, new BlockPos(ae, af, ag));
				float ai = textureAtlasSprite.getU(d * (double)(w - y));
				float aj = textureAtlasSprite.getU(d * (double)(w - (y + 1)));
				float ak = textureAtlasSprite.getV(e * (double)(x - z));
				float al = textureAtlasSprite.getV(e * (double)(x - (z + 1)));
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, aj, ak, -0.5F, 0, 0, -1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, ai, ak, -0.5F, 0, 0, -1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, ai, al, -0.5F, 0, 0, -1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, aj, al, -0.5F, 0, 0, -1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, k, m, 0.5F, 0, 0, 1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, l, m, 0.5F, 0, 0, 1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, l, n, 0.5F, 0, 0, 1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, k, n, 0.5F, 0, 0, 1, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, o, q, -0.5F, 0, 1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, p, q, -0.5F, 0, 1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, p, r, 0.5F, 0, 1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, o, r, 0.5F, 0, 1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, o, q, 0.5F, 0, -1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, p, q, 0.5F, 0, -1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, p, r, -0.5F, 0, -1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, o, r, -0.5F, 0, -1, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, t, u, 0.5F, -1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, t, v, 0.5F, -1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ad, s, v, -0.5F, -1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, aa, ac, s, u, -0.5F, -1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, t, u, -0.5F, 1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, t, v, -0.5F, 1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ad, s, v, 0.5F, 1, 0, 0, ah);
				this.vertex(matrix4f, matrix3f, vertexConsumer, ab, ac, s, u, 0.5F, 1, 0, 0, ah);
			}
		}
	}

	private void vertex(
		Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, int k, int l, int m, int n
	) {
		vertexConsumer.vertex(matrix4f, f, g, j)
			.color(255, 255, 255, 255)
			.uv(h, i)
			.overlayCoords(OverlayTexture.NO_OVERLAY)
			.uv2(n)
			.normal(matrix3f, (float)k, (float)l, (float)m)
			.endVertex();
	}
}
