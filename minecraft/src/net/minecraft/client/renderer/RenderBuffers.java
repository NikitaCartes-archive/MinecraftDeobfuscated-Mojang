package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.util.SortedMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.resources.model.ModelBakery;

@Environment(EnvType.CLIENT)
public class RenderBuffers {
	private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
	private final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), object2ObjectLinkedOpenHashMap -> {
		object2ObjectLinkedOpenHashMap.put(Sheets.solidBlockSheet(), this.fixedBufferPack.builder(RenderType.solid()));
		object2ObjectLinkedOpenHashMap.put(Sheets.cutoutBlockSheet(), this.fixedBufferPack.builder(RenderType.cutout()));
		object2ObjectLinkedOpenHashMap.put(Sheets.bannerSheet(), this.fixedBufferPack.builder(RenderType.cutoutMipped()));
		object2ObjectLinkedOpenHashMap.put(Sheets.translucentCullBlockSheet(), this.fixedBufferPack.builder(RenderType.translucent()));
		put(object2ObjectLinkedOpenHashMap, Sheets.shieldSheet());
		put(object2ObjectLinkedOpenHashMap, Sheets.bedSheet());
		put(object2ObjectLinkedOpenHashMap, Sheets.shulkerBoxSheet());
		put(object2ObjectLinkedOpenHashMap, Sheets.signSheet());
		put(object2ObjectLinkedOpenHashMap, Sheets.hangingSignSheet());
		put(object2ObjectLinkedOpenHashMap, Sheets.chestSheet());
		put(object2ObjectLinkedOpenHashMap, RenderType.translucentNoCrumbling());
		put(object2ObjectLinkedOpenHashMap, RenderType.armorGlint());
		put(object2ObjectLinkedOpenHashMap, RenderType.armorEntityGlint());
		put(object2ObjectLinkedOpenHashMap, RenderType.glint());
		put(object2ObjectLinkedOpenHashMap, RenderType.glintDirect());
		put(object2ObjectLinkedOpenHashMap, RenderType.glintTranslucent());
		put(object2ObjectLinkedOpenHashMap, RenderType.entityGlint());
		put(object2ObjectLinkedOpenHashMap, RenderType.entityGlintDirect());
		put(object2ObjectLinkedOpenHashMap, RenderType.waterMask());
		ModelBakery.DESTROY_TYPES.forEach(renderType -> put(object2ObjectLinkedOpenHashMap, renderType));
	});
	private final MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(this.fixedBuffers, new BufferBuilder(256));
	private final MultiBufferSource.BufferSource crumblingBufferSource = MultiBufferSource.immediate(new BufferBuilder(256));
	private final OutlineBufferSource outlineBufferSource = new OutlineBufferSource(this.bufferSource);

	private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> object2ObjectLinkedOpenHashMap, RenderType renderType) {
		object2ObjectLinkedOpenHashMap.put(renderType, new BufferBuilder(renderType.bufferSize()));
	}

	public SectionBufferBuilderPack fixedBufferPack() {
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
