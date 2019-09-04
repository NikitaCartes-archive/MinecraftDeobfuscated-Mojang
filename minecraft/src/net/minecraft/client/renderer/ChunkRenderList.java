package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockLayer;

@Environment(EnvType.CLIENT)
public class ChunkRenderList {
	private double xOff;
	private double yOff;
	private double zOff;
	protected final List<RenderChunk> chunks = Lists.<RenderChunk>newArrayListWithCapacity(17424);
	protected boolean ready;

	public void setCameraLocation(double d, double e, double f) {
		this.ready = true;
		this.chunks.clear();
		this.xOff = d;
		this.yOff = e;
		this.zOff = f;
	}

	public void translateToRelativeChunkPosition(RenderChunk renderChunk) {
		BlockPos blockPos = renderChunk.getOrigin();
		RenderSystem.translatef(
			(float)((double)blockPos.getX() - this.xOff), (float)((double)blockPos.getY() - this.yOff), (float)((double)blockPos.getZ() - this.zOff)
		);
	}

	public void add(RenderChunk renderChunk, BlockLayer blockLayer) {
		this.chunks.add(renderChunk);
	}

	public void render(BlockLayer blockLayer) {
		if (this.ready) {
			for (RenderChunk renderChunk : this.chunks) {
				VertexBuffer vertexBuffer = renderChunk.getBuffer(blockLayer.ordinal());
				RenderSystem.pushMatrix();
				this.translateToRelativeChunkPosition(renderChunk);
				vertexBuffer.bind();
				this.applyVertexDeclaration();
				vertexBuffer.draw(7);
				RenderSystem.popMatrix();
			}

			VertexBuffer.unbind();
			RenderSystem.clearCurrentColor();
			this.chunks.clear();
		}
	}

	private void applyVertexDeclaration() {
		RenderSystem.vertexPointer(3, 5126, 28, 0);
		RenderSystem.colorPointer(4, 5121, 28, 12);
		RenderSystem.texCoordPointer(2, 5126, 28, 16);
		RenderSystem.glClientActiveTexture(33985);
		RenderSystem.texCoordPointer(2, 5122, 28, 24);
		RenderSystem.glClientActiveTexture(33984);
	}
}
