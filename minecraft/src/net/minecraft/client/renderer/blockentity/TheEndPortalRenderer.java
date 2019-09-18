package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.nio.FloatBuffer;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.TheEndPortalBlockEntity;

@Environment(EnvType.CLIENT)
public class TheEndPortalRenderer<T extends TheEndPortalBlockEntity> extends BlockEntityRenderer<T> {
	private static final ResourceLocation END_SKY_LOCATION = new ResourceLocation("textures/environment/end_sky.png");
	private static final ResourceLocation END_PORTAL_LOCATION = new ResourceLocation("textures/entity/end_portal.png");
	private static final Random RANDOM = new Random(31100L);
	private static final FloatBuffer MODELVIEW = MemoryTracker.createFloatBuffer(16);
	private static final FloatBuffer PROJECTION = MemoryTracker.createFloatBuffer(16);
	private final FloatBuffer buffer = MemoryTracker.createFloatBuffer(16);

	public void render(T theEndPortalBlockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
		RenderSystem.disableLighting();
		RANDOM.setSeed(31100L);
		RenderSystem.getMatrix(2982, MODELVIEW);
		RenderSystem.getMatrix(2983, PROJECTION);
		double h = d * d + e * e + f * f;
		int j = this.getPasses(h);
		float k = this.getOffset();
		boolean bl = false;

		for (int l = 0; l < j; l++) {
			RenderSystem.pushMatrix();
			float m = 2.0F / (float)(18 - l);
			if (l == 0) {
				this.bindTexture(END_SKY_LOCATION);
				m = 0.15F;
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			}

			if (l >= 1) {
				this.bindTexture(END_PORTAL_LOCATION);
				bl = true;
				FogRenderer.resetFogColor(true);
			}

			if (l == 1) {
				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
			}

			RenderSystem.texGenMode(GlStateManager.TexGen.S, 9216);
			RenderSystem.texGenMode(GlStateManager.TexGen.T, 9216);
			RenderSystem.texGenMode(GlStateManager.TexGen.R, 9216);
			RenderSystem.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
			RenderSystem.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
			RenderSystem.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
			RenderSystem.enableTexGen(GlStateManager.TexGen.S);
			RenderSystem.enableTexGen(GlStateManager.TexGen.T);
			RenderSystem.enableTexGen(GlStateManager.TexGen.R);
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(5890);
			RenderSystem.pushMatrix();
			RenderSystem.loadIdentity();
			RenderSystem.translatef(0.5F, 0.5F, 0.0F);
			RenderSystem.scalef(0.5F, 0.5F, 1.0F);
			float n = (float)(l + 1);
			RenderSystem.translatef(17.0F / n, (2.0F + n / 1.5F) * ((float)(Util.getMillis() % 800000L) / 800000.0F), 0.0F);
			RenderSystem.rotatef((n * n * 4321.0F + n * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
			RenderSystem.scalef(4.5F - n / 4.0F, 4.5F - n / 4.0F, 1.0F);
			RenderSystem.multMatrix(PROJECTION);
			RenderSystem.multMatrix(MODELVIEW);
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferBuilder = tesselator.getBuilder();
			bufferBuilder.begin(7, DefaultVertexFormat.POSITION_COLOR);
			float o = (RANDOM.nextFloat() * 0.5F + 0.1F) * m;
			float p = (RANDOM.nextFloat() * 0.5F + 0.4F) * m;
			float q = (RANDOM.nextFloat() * 0.5F + 0.5F) * m;
			if (theEndPortalBlockEntity.shouldRenderFace(Direction.SOUTH)) {
				bufferBuilder.vertex(d, e, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e + 1.0, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e + 1.0, f + 1.0).color(o, p, q, 1.0F).endVertex();
			}

			if (theEndPortalBlockEntity.shouldRenderFace(Direction.NORTH)) {
				bufferBuilder.vertex(d, e + 1.0, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e + 1.0, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e, f).color(o, p, q, 1.0F).endVertex();
			}

			if (theEndPortalBlockEntity.shouldRenderFace(Direction.EAST)) {
				bufferBuilder.vertex(d + 1.0, e + 1.0, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e + 1.0, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e, f).color(o, p, q, 1.0F).endVertex();
			}

			if (theEndPortalBlockEntity.shouldRenderFace(Direction.WEST)) {
				bufferBuilder.vertex(d, e, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e + 1.0, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e + 1.0, f).color(o, p, q, 1.0F).endVertex();
			}

			if (theEndPortalBlockEntity.shouldRenderFace(Direction.DOWN)) {
				bufferBuilder.vertex(d, e, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e, f + 1.0).color(o, p, q, 1.0F).endVertex();
			}

			if (theEndPortalBlockEntity.shouldRenderFace(Direction.UP)) {
				bufferBuilder.vertex(d, e + (double)k, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e + (double)k, f + 1.0).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d + 1.0, e + (double)k, f).color(o, p, q, 1.0F).endVertex();
				bufferBuilder.vertex(d, e + (double)k, f).color(o, p, q, 1.0F).endVertex();
			}

			tesselator.end();
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(5888);
			this.bindTexture(END_SKY_LOCATION);
		}

		RenderSystem.disableBlend();
		RenderSystem.disableTexGen(GlStateManager.TexGen.S);
		RenderSystem.disableTexGen(GlStateManager.TexGen.T);
		RenderSystem.disableTexGen(GlStateManager.TexGen.R);
		RenderSystem.enableLighting();
		if (bl) {
			FogRenderer.resetFogColor(false);
		}
	}

	protected int getPasses(double d) {
		int i;
		if (d > 36864.0) {
			i = 1;
		} else if (d > 25600.0) {
			i = 3;
		} else if (d > 16384.0) {
			i = 5;
		} else if (d > 9216.0) {
			i = 7;
		} else if (d > 4096.0) {
			i = 9;
		} else if (d > 1024.0) {
			i = 11;
		} else if (d > 576.0) {
			i = 13;
		} else if (d > 256.0) {
			i = 14;
		} else {
			i = 15;
		}

		return i;
	}

	protected float getOffset() {
		return 0.75F;
	}

	private FloatBuffer getBuffer(float f, float g, float h, float i) {
		this.buffer.clear();
		this.buffer.put(f).put(g).put(h).put(i);
		this.buffer.flip();
		return this.buffer;
	}
}
