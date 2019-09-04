package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
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

	public void render(Painting painting, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		RenderSystem.translated(d, e, f);
		RenderSystem.rotatef(180.0F - g, 0.0F, 1.0F, 0.0F);
		RenderSystem.enableRescaleNormal();
		this.bindTexture(painting);
		Motive motive = painting.motive;
		float i = 0.0625F;
		RenderSystem.scalef(0.0625F, 0.0625F, 0.0625F);
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(painting));
		}

		PaintingTextureManager paintingTextureManager = Minecraft.getInstance().getPaintingTextures();
		this.renderPainting(painting, motive.getWidth(), motive.getHeight(), paintingTextureManager.get(motive), paintingTextureManager.getBackSprite());
		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
		super.render(painting, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(Painting painting) {
		return TextureAtlas.LOCATION_PAINTINGS;
	}

	private void renderPainting(Painting painting, int i, int j, TextureAtlasSprite textureAtlasSprite, TextureAtlasSprite textureAtlasSprite2) {
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
				this.setBrightness(painting, (aa + ab) / 2.0F, (ac + ad) / 2.0F);
				float ae = textureAtlasSprite.getU(d * (double)(w - y));
				float af = textureAtlasSprite.getU(d * (double)(w - (y + 1)));
				float ag = textureAtlasSprite.getV(e * (double)(x - z));
				float ah = textureAtlasSprite.getV(e * (double)(x - (z + 1)));
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_NORMAL);
				bufferBuilder.vertex((double)aa, (double)ad, -0.5).uv((double)af, (double)ag).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ad, -0.5).uv((double)ae, (double)ag).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ac, -0.5).uv((double)ae, (double)ah).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ac, -0.5).uv((double)af, (double)ah).normal(0.0F, 0.0F, -1.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ac, 0.5).uv((double)k, (double)m).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ac, 0.5).uv((double)l, (double)m).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ad, 0.5).uv((double)l, (double)n).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ad, 0.5).uv((double)k, (double)n).normal(0.0F, 0.0F, 1.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ac, -0.5).uv((double)o, (double)q).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ac, -0.5).uv((double)p, (double)q).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ac, 0.5).uv((double)p, (double)r).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ac, 0.5).uv((double)o, (double)r).normal(0.0F, 1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ad, 0.5).uv((double)o, (double)q).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ad, 0.5).uv((double)p, (double)q).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ad, -0.5).uv((double)p, (double)r).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ad, -0.5).uv((double)o, (double)r).normal(0.0F, -1.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ac, 0.5).uv((double)t, (double)u).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ad, 0.5).uv((double)t, (double)v).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ad, -0.5).uv((double)s, (double)v).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)aa, (double)ac, -0.5).uv((double)s, (double)u).normal(-1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ac, -0.5).uv((double)t, (double)u).normal(1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ad, -0.5).uv((double)t, (double)v).normal(1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ad, 0.5).uv((double)s, (double)v).normal(1.0F, 0.0F, 0.0F).endVertex();
				bufferBuilder.vertex((double)ab, (double)ac, 0.5).uv((double)s, (double)u).normal(1.0F, 0.0F, 0.0F).endVertex();
				tesselator.end();
			}
		}
	}

	private void setBrightness(Painting painting, float f, float g) {
		int i = Mth.floor(painting.x);
		int j = Mth.floor(painting.y + (double)(g / 16.0F));
		int k = Mth.floor(painting.z);
		Direction direction = painting.getDirection();
		if (direction == Direction.NORTH) {
			i = Mth.floor(painting.x + (double)(f / 16.0F));
		}

		if (direction == Direction.WEST) {
			k = Mth.floor(painting.z - (double)(f / 16.0F));
		}

		if (direction == Direction.SOUTH) {
			i = Mth.floor(painting.x - (double)(f / 16.0F));
		}

		if (direction == Direction.EAST) {
			k = Mth.floor(painting.z + (double)(f / 16.0F));
		}

		int l = this.entityRenderDispatcher.level.getLightColor(new BlockPos(i, j, k));
		int m = l % 65536;
		int n = l / 65536;
		RenderSystem.glMultiTexCoord2f(33985, (float)m, (float)n);
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
	}
}
