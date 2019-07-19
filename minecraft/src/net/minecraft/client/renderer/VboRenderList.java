package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.BlockLayer;

@Environment(EnvType.CLIENT)
public class VboRenderList extends ChunkRenderList {
	@Override
	public void render(BlockLayer blockLayer) {
		if (this.ready) {
			for (RenderChunk renderChunk : this.chunks) {
				VertexBuffer vertexBuffer = renderChunk.getBuffer(blockLayer.ordinal());
				GlStateManager.pushMatrix();
				this.translateToRelativeChunkPosition(renderChunk);
				vertexBuffer.bind();
				this.applyVertexDeclaration();
				vertexBuffer.draw(7);
				GlStateManager.popMatrix();
			}

			VertexBuffer.unbind();
			GlStateManager.clearCurrentColor();
			this.chunks.clear();
		}
	}

	private void applyVertexDeclaration() {
		GlStateManager.vertexPointer(3, 5126, 28, 0);
		GlStateManager.colorPointer(4, 5121, 28, 12);
		GlStateManager.texCoordPointer(2, 5126, 28, 16);
		GLX.glClientActiveTexture(GLX.GL_TEXTURE1);
		GlStateManager.texCoordPointer(2, 5122, 28, 24);
		GLX.glClientActiveTexture(GLX.GL_TEXTURE0);
	}
}
