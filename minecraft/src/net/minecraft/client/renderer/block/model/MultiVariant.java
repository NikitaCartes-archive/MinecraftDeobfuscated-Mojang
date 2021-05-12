package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class MultiVariant implements UnbakedModel {
	private final List<Variant> variants;

	public MultiVariant(List<Variant> list) {
		this.variants = list;
	}

	public List<Variant> getVariants() {
		return this.variants;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return object instanceof MultiVariant multiVariant ? this.variants.equals(multiVariant.variants) : false;
		}
	}

	public int hashCode() {
		return this.variants.hashCode();
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return (Collection<ResourceLocation>)this.getVariants().stream().map(Variant::getModelLocation).collect(Collectors.toSet());
	}

	@Override
	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> function, Set<Pair<String, String>> set) {
		return (Collection<Material>)this.getVariants()
			.stream()
			.map(Variant::getModelLocation)
			.distinct()
			.flatMap(resourceLocation -> ((UnbakedModel)function.apply(resourceLocation)).getMaterials(function, set).stream())
			.collect(Collectors.toSet());
	}

	@Nullable
	@Override
	public BakedModel bake(ModelBakery modelBakery, Function<Material, TextureAtlasSprite> function, ModelState modelState, ResourceLocation resourceLocation) {
		if (this.getVariants().isEmpty()) {
			return null;
		} else {
			WeightedBakedModel.Builder builder = new WeightedBakedModel.Builder();

			for (Variant variant : this.getVariants()) {
				BakedModel bakedModel = modelBakery.bake(variant.getModelLocation(), variant);
				builder.add(bakedModel, variant.getWeight());
			}

			return builder.build();
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<MultiVariant> {
		public MultiVariant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			List<Variant> list = Lists.<Variant>newArrayList();
			if (jsonElement.isJsonArray()) {
				JsonArray jsonArray = jsonElement.getAsJsonArray();
				if (jsonArray.size() == 0) {
					throw new JsonParseException("Empty variant array");
				}

				for (JsonElement jsonElement2 : jsonArray) {
					list.add((Variant)jsonDeserializationContext.deserialize(jsonElement2, Variant.class));
				}
			} else {
				list.add((Variant)jsonDeserializationContext.deserialize(jsonElement, Variant.class));
			}

			return new MultiVariant(list);
		}
	}
}
