package net.minecraft.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;

@Environment(EnvType.CLIENT)
public interface ParticleRenderType {
	ParticleRenderType TERRAIN_SHEET = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.depthMask(true);
			textureManager.bind(TextureAtlas.LOCATION_BLOCKS);
			bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
		}

		public String toString() {
			return "TERRAIN_SHEET";
		}
	};
	ParticleRenderType PARTICLE_SHEET_OPAQUE = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
			bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
		}

		public String toString() {
			return "PARTICLE_SHEET_OPAQUE";
		}
	};
	ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.depthMask(true);
			textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
			RenderSystem.enableBlend();
			RenderSystem.blendFuncSeparate(
				GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
				GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
			);
			RenderSystem.alphaFunc(516, 0.003921569F);
			bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
		}

		public String toString() {
			return "PARTICLE_SHEET_TRANSLUCENT";
		}
	};
	ParticleRenderType PARTICLE_SHEET_LIT = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.disableBlend();
			RenderSystem.depthMask(true);
			textureManager.bind(TextureAtlas.LOCATION_PARTICLES);
			bufferBuilder.begin(7, DefaultVertexFormat.PARTICLE);
		}

		@Override
		public void end(Tesselator tesselator) {
			tesselator.end();
		}

		public String toString() {
			return "PARTICLE_SHEET_LIT";
		}
	};
	ParticleRenderType CUSTOM = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();
		}

		@Override
		public void end(Tesselator tesselator) {
		}

		public String toString() {
			return "CUSTOM";
		}
	};
	ParticleRenderType NO_RENDER = new ParticleRenderType() {
		@Override
		public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
		}

		@Override
		public void end(Tesselator tesselator) {
		}

		public String toString() {
			return "NO_RENDER";
		}
	};

	void begin(BufferBuilder bufferBuilder, TextureManager textureManager);

	void end(Tesselator tesselator);
}
