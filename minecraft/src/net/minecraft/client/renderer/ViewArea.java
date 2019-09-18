package net.minecraft.client.renderer;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class ViewArea {
	protected final LevelRenderer levelRenderer;
	protected final Level level;
	protected int chunkGridSizeY;
	protected int chunkGridSizeX;
	protected int chunkGridSizeZ;
	public ChunkRenderDispatcher.RenderChunk[] chunks;

	public ViewArea(ChunkRenderDispatcher chunkRenderDispatcher, Level level, int i, LevelRenderer levelRenderer) {
		this.levelRenderer = levelRenderer;
		this.level = level;
		this.setViewDistance(i);
		this.createChunks(chunkRenderDispatcher);
	}

	protected void createChunks(ChunkRenderDispatcher chunkRenderDispatcher) {
		int i = this.chunkGridSizeX * this.chunkGridSizeY * this.chunkGridSizeZ;
		this.chunks = new ChunkRenderDispatcher.RenderChunk[i];

		for (int j = 0; j < this.chunkGridSizeX; j++) {
			for (int k = 0; k < this.chunkGridSizeY; k++) {
				for (int l = 0; l < this.chunkGridSizeZ; l++) {
					int m = this.getChunkIndex(j, k, l);
					this.chunks[m] = chunkRenderDispatcher.new RenderChunk();
					this.chunks[m].setOrigin(j * 16, k * 16, l * 16);
				}
			}
		}
	}

	public void releaseAllBuffers() {
		for (ChunkRenderDispatcher.RenderChunk renderChunk : this.chunks) {
			renderChunk.releaseBuffers();
		}
	}

	private int getChunkIndex(int i, int j, int k) {
		return (k * this.chunkGridSizeY + j) * this.chunkGridSizeX + i;
	}

	protected void setViewDistance(int i) {
		int j = i * 2 + 1;
		this.chunkGridSizeX = j;
		this.chunkGridSizeY = 16;
		this.chunkGridSizeZ = j;
	}

	public void repositionCamera(double d, double e) {
		int i = Mth.floor(d) - 8;
		int j = Mth.floor(e) - 8;
		int k = this.chunkGridSizeX * 16;

		for (int l = 0; l < this.chunkGridSizeX; l++) {
			int m = this.getCoordinate(i, k, l);

			for (int n = 0; n < this.chunkGridSizeZ; n++) {
				int o = this.getCoordinate(j, k, n);

				for (int p = 0; p < this.chunkGridSizeY; p++) {
					int q = p * 16;
					ChunkRenderDispatcher.RenderChunk renderChunk = this.chunks[this.getChunkIndex(l, p, n)];
					renderChunk.setOrigin(m, q, o);
				}
			}
		}
	}

	private int getCoordinate(int i, int j, int k) {
		int l = k * 16;
		int m = l - i + j / 2;
		if (m < 0) {
			m -= j - 1;
		}

		return l - m / j * j;
	}

	public void setDirty(int i, int j, int k, boolean bl) {
		int l = Math.floorMod(i, this.chunkGridSizeX);
		int m = Math.floorMod(j, this.chunkGridSizeY);
		int n = Math.floorMod(k, this.chunkGridSizeZ);
		ChunkRenderDispatcher.RenderChunk renderChunk = this.chunks[this.getChunkIndex(l, m, n)];
		renderChunk.setDirty(bl);
	}

	@Nullable
	protected ChunkRenderDispatcher.RenderChunk getRenderChunkAt(BlockPos blockPos) {
		int i = Mth.intFloorDiv(blockPos.getX(), 16);
		int j = Mth.intFloorDiv(blockPos.getY(), 16);
		int k = Mth.intFloorDiv(blockPos.getZ(), 16);
		if (j >= 0 && j < this.chunkGridSizeY) {
			i = Mth.positiveModulo(i, this.chunkGridSizeX);
			k = Mth.positiveModulo(k, this.chunkGridSizeZ);
			return this.chunks[this.getChunkIndex(i, j, k)];
		} else {
			return null;
		}
	}
}
