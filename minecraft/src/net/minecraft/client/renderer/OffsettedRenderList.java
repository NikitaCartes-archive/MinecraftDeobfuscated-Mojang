package net.minecraft.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.chunk.ListedRenderChunk;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.world.level.BlockLayer;

@Environment(EnvType.CLIENT)
public class OffsettedRenderList extends ChunkRenderList {
	@Override
	public void render(BlockLayer blockLayer) {
		if (this.ready) {
			for (RenderChunk renderChunk : this.chunks) {
				ListedRenderChunk listedRenderChunk = (ListedRenderChunk)renderChunk;
				GlStateManager.pushMatrix();
				this.translateToRelativeChunkPosition(renderChunk);
				GlStateManager.callList(listedRenderChunk.getGlListId(blockLayer, listedRenderChunk.getCompiledChunk()));
				GlStateManager.popMatrix();
			}

			GlStateManager.clearCurrentColor();
			this.chunks.clear();
		}
	}
}
