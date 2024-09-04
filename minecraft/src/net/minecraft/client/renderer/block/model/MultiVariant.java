package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public record MultiVariant(List<Variant> variants) implements UnbakedBlockStateModel {
	public MultiVariant(List<Variant> variants) {
		if (variants.isEmpty()) {
			throw new IllegalArgumentException("Variant list must contain at least one element");
		} else {
			this.variants = variants;
		}
	}

	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		return this;
	}

	@Override
	public void resolveDependencies(UnbakedModel.Resolver resolver) {
		this.variants.forEach(variant -> resolver.resolve(variant.getModelLocation()));
	}

	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
		if (this.variants.size() == 1) {
			Variant variant = (Variant)this.variants.getFirst();
			return modelBaker.bake(variant.getModelLocation(), variant);
		} else {
			SimpleWeightedRandomList.Builder<BakedModel> builder = SimpleWeightedRandomList.builder();

			for (Variant variant2 : this.variants) {
				BakedModel bakedModel = modelBaker.bake(variant2.getModelLocation(), variant2);
				builder.add(bakedModel, variant2.getWeight());
			}

			return new WeightedBakedModel(builder.build());
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<MultiVariant> {
		public MultiVariant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			List<Variant> list = Lists.<Variant>newArrayList();
			if (jsonElement.isJsonArray()) {
				JsonArray jsonArray = jsonElement.getAsJsonArray();
				if (jsonArray.isEmpty()) {
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
