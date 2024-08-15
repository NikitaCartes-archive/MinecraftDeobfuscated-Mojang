package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SequencedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;

@Environment(EnvType.CLIENT)
public class RenderBuffers {
	private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
	private final SectionBufferBuilderPool sectionBufferPool;
	private final MultiBufferSource.BufferSource bufferSource;
	private final MultiBufferSource.BufferSource crumblingBufferSource;
	private final OutlineBufferSource outlineBufferSource;

	public RenderBuffers(int i) {
		this.sectionBufferPool = SectionBufferBuilderPool.allocate(i);
		SequencedMap<RenderType, ByteBufferBuilder> sequencedMap = Util.make(
			new Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>(), object2ObjectLinkedOpenHashMap -> {
				object2ObjectLinkedOpenHashMap.put(Sheets.solidBlockSheet(), this.fixedBufferPack.buffer(RenderType.solid()));
				object2ObjectLinkedOpenHashMap.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.buffer(RenderType.cutout()));
				object2ObjectLinkedOpenHashMap.put(Sheets.bannerSheet(), this.fixedBufferPack.buffer(RenderType.cutoutMipped()));
				object2ObjectLinkedOpenHashMap.put(Sheets.translucentCullBlockSheet(), this.fixedBufferPack.buffer(RenderType.translucent()));
				put(object2ObjectLinkedOpenHashMap, Sheets.shieldSheet());
				put(object2ObjectLinkedOpenHashMap, Sheets.bedSheet());
				put(object2ObjectLinkedOpenHashMap, Sheets.shulkerBoxSheet());
				put(object2ObjectLinkedOpenHashMap, Sheets.signSheet());
				put(object2ObjectLinkedOpenHashMap, Sheets.hangingSignSheet());
				object2ObjectLinkedOpenHashMap.put(Sheets.chestSheet(), new ByteBufferBuilder(786432));
				put(object2ObjectLinkedOpenHashMap, RenderType.armorEntityGlint());
				put(object2ObjectLinkedOpenHashMap, RenderType.glint());
				put(object2ObjectLinkedOpenHashMap, RenderType.glintTranslucent());
				put(object2ObjectLinkedOpenHashMap, RenderType.entityGlint());
				put(object2ObjectLinkedOpenHashMap, RenderType.entityGlintDirect());
				put(object2ObjectLinkedOpenHashMap, RenderType.waterMask());
			}
		);
		this.bufferSource = MultiBufferSource.immediateWithBuffers(sequencedMap, new ByteBufferBuilder(786432));
		this.outlineBufferSource = new OutlineBufferSource(this.bufferSource);
		SequencedMap<RenderType, ByteBufferBuilder> sequencedMap2 = Util.make(
			new Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder>(),
			object2ObjectLinkedOpenHashMap -> ModelBakery.DESTROY_TYPES.forEach(renderType -> put(object2ObjectLinkedOpenHashMap, renderType))
		);
		this.crumblingBufferSource = MultiBufferSource.immediateWithBuffers(sequencedMap2, new ByteBufferBuilder(0));
	}

	private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> object2ObjectLinkedOpenHashMap, RenderType renderType) {
		object2ObjectLinkedOpenHashMap.put(renderType, new ByteBufferBuilder(renderType.bufferSize()));
	}

	public SectionBufferBuilderPack fixedBufferPack() {
		return this.fixedBufferPack;
	}

	public SectionBufferBuilderPool sectionBufferPool() {
		return this.sectionBufferPool;
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
