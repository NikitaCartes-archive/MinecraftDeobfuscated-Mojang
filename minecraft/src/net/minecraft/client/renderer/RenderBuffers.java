package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.TextureAtlas;

@Environment(EnvType.CLIENT)
public class RenderBuffers {
	private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
	private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), object2ObjectLinkedOpenHashMap -> {
		object2ObjectLinkedOpenHashMap.put(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.solid()));
		object2ObjectLinkedOpenHashMap.put(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.cutout()));
		object2ObjectLinkedOpenHashMap.put(RenderType.entityNoOutline(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
		object2ObjectLinkedOpenHashMap.put(RenderType.entityTranslucent(TextureAtlas.LOCATION_BLOCKS), this.fixedBufferPack.builder(RenderType.translucent()));
		object2ObjectLinkedOpenHashMap.put(RenderType.translucentNoCrumbling(), new BufferBuilder(RenderType.translucentNoCrumbling().bufferSize()));
		object2ObjectLinkedOpenHashMap.put(RenderType.glint(), new BufferBuilder(RenderType.glint().bufferSize()));
		object2ObjectLinkedOpenHashMap.put(RenderType.entityGlint(), new BufferBuilder(RenderType.entityGlint().bufferSize()));
		object2ObjectLinkedOpenHashMap.put(RenderType.waterMask(), new BufferBuilder(RenderType.waterMask().bufferSize()));
	});
	private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
	private final MultiBufferSource.BufferSource effectBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
	private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

	public ChunkBufferBuilderPack fixedBufferPack() {
		return this.fixedBufferPack;
	}

	public MultiBufferSource.BufferSource bufferSource() {
		return this.bufferSource;
	}

	public MultiBufferSource.BufferSource effectBufferSource() {
		return this.effectBufferSource;
	}

	public OutlineBufferSource outlineBufferSource() {
		return this.outlineBufferSource;
	}
}
