package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.level.BlockLayer;

@Environment(EnvType.CLIENT)
public class ChunkBufferBuilderPack {
	private final BufferBuilder[] builders = new BufferBuilder[BlockLayer.values().length];

	public ChunkBufferBuilderPack() {
		this.builders[BlockLayer.SOLID.ordinal()] = new BufferBuilder(2097152);
		this.builders[BlockLayer.CUTOUT.ordinal()] = new BufferBuilder(131072);
		this.builders[BlockLayer.CUTOUT_MIPPED.ordinal()] = new BufferBuilder(131072);
		this.builders[BlockLayer.TRANSLUCENT.ordinal()] = new BufferBuilder(262144);
	}

	public BufferBuilder builder(BlockLayer blockLayer) {
		return this.builders[blockLayer.ordinal()];
	}

	public BufferBuilder builder(int i) {
		return this.builders[i];
	}
}
