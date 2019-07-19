package net.minecraft.client.renderer.chunk;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public interface RenderChunkFactory {
	RenderChunk create(Level level, LevelRenderer levelRenderer);
}
