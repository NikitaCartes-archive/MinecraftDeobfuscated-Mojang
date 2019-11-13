package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;

@Environment(EnvType.CLIENT)
public class RenderBuffers {
	private final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
	private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), object2ObjectLinkedOpenHashMap -> {
		object2ObjectLinkedOpenHashMap.put(RenderType.blockentitySolid(), this.fixedBufferPack.builder(RenderType.solid()));
		object2ObjectLinkedOpenHashMap.put(RenderType.blockentityCutout(), this.fixedBufferPack.builder(RenderType.cutout()));
		object2ObjectLinkedOpenHashMap.put(RenderType.blockentityNoOutline(), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
		object2ObjectLinkedOpenHashMap.put(RenderType.blockentityTranslucent(), this.fixedBufferPack.builder(RenderType.translucent()));
		put(object2ObjectLinkedOpenHashMap, RenderType.translucentNoCrumbling());
		put(object2ObjectLinkedOpenHashMap, RenderType.glint());
		put(object2ObjectLinkedOpenHashMap, RenderType.entityGlint());
		put(object2ObjectLinkedOpenHashMap, RenderType.waterMask());

		for (int i = 0; i < 10; i++) {
			RenderType renderType = RenderType.crumbling(i);
			put(object2ObjectLinkedOpenHashMap, renderType);
		}
	});
	private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
	private final MultiBufferSource.BufferSource crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
	private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

	private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> object2ObjectLinkedOpenHashMap, RenderType renderType) {
		object2ObjectLinkedOpenHashMap.put(renderType, new BufferBuilder(renderType.bufferSize()));
	}

	public ChunkBufferBuilderPack fixedBufferPack() {
		return this.fixedBufferPack;
	}

	public MultiBufferSource.BufferSource bufferSource() {
		return this.bufferSource;
	}

	public MultiBufferSource.BufferSource crumblingBufferSource() {
		return this.crumblingBufferSource;
	}

	public OutlineBufferSource outlineBufferSource() {
		return this.outlineBufferSource;
	}
}
