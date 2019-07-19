package net.minecraft.client.renderer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockLayer;

@Environment(EnvType.CLIENT)
public abstract class ChunkRenderList {
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
		GlStateManager.translatef(
			(float)((double)blockPos.getX() - this.xOff), (float)((double)blockPos.getY() - this.yOff), (float)((double)blockPos.getZ() - this.zOff)
		);
	}

	public void add(RenderChunk renderChunk, BlockLayer blockLayer) {
		this.chunks.add(renderChunk);
	}

	public abstract void render(BlockLayer blockLayer);
}
