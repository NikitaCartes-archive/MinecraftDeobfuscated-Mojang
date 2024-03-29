package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Map;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class SectionBufferBuilderPack implements AutoCloseable {
	public static final int TOTAL_BUFFERS_SIZE = RenderType.chunkBufferLayers().stream().mapToInt(RenderType::bufferSize).sum();
	private final Map<RenderType, BufferBuilder> builders = (Map<RenderType, BufferBuilder>)RenderType.chunkBufferLayers()
		.stream()
		.collect(Collectors.toMap(renderType -> renderType, renderType -> new BufferBuilder(renderType.bufferSize())));

	public BufferBuilder builder(RenderType renderType) {
		return (BufferBuilder)this.builders.get(renderType);
	}

	public void clearAll() {
		this.builders.values().forEach(BufferBuilder::clear);
	}

	public void discardAll() {
		this.builders.values().forEach(BufferBuilder::discard);
	}

	public void close() {
		this.builders.values().forEach(BufferBuilder::release);
	}
}
