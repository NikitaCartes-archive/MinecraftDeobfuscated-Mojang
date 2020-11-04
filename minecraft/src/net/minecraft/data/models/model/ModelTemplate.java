package net.minecraft.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class ModelTemplate {
	private final Optional<ResourceLocation> model;
	private final Set<TextureSlot> requiredSlots;
	private final Optional<String> suffix;

	public ModelTemplate(Optional<ResourceLocation> optional, Optional<String> optional2, TextureSlot... textureSlots) {
		this.model = optional;
		this.suffix = optional2;
		this.requiredSlots = ImmutableSet.copyOf(textureSlots);
	}

	public ResourceLocation create(Block block, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer) {
		return this.create(ModelLocationUtils.getModelLocation(block, (String)this.suffix.orElse("")), textureMapping, biConsumer);
	}

	public ResourceLocation createWithSuffix(
		Block block, String string, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		return this.create(ModelLocationUtils.getModelLocation(block, string + (String)this.suffix.orElse("")), textureMapping, biConsumer);
	}

	public ResourceLocation createWithOverride(
		Block block, String string, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		return this.create(ModelLocationUtils.getModelLocation(block, string), textureMapping, biConsumer);
	}

	public ResourceLocation create(
		ResourceLocation resourceLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, Supplier<JsonElement>> biConsumer
	) {
		Map<TextureSlot, ResourceLocation> map = this.createMap(textureMapping);
		biConsumer.accept(resourceLocation, (Supplier)() -> {
			JsonObject jsonObject = new JsonObject();
			this.model.ifPresent(resourceLocationx -> jsonObject.addProperty("parent", resourceLocationx.toString()));
			if (!map.isEmpty()) {
				JsonObject jsonObject2 = new JsonObject();
				map.forEach((textureSlot, resourceLocationx) -> jsonObject2.addProperty(textureSlot.getId(), resourceLocationx.toString()));
				jsonObject.add("textures", jsonObject2);
			}

			return jsonObject;
		});
		return resourceLocation;
	}

	private Map<TextureSlot, ResourceLocation> createMap(TextureMapping textureMapping) {
		return (Map<TextureSlot, ResourceLocation>)Streams.concat(this.requiredSlots.stream(), textureMapping.getForced())
			.collect(ImmutableMap.toImmutableMap(Function.identity(), textureMapping::get));
	}
}
