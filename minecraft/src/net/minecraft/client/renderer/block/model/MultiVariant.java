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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public record MultiVariant(List<Variant> variants) implements UnbakedBlockStateModel {
	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		return this;
	}

	@Override
	public void resolveDependencies(UnbakedModel.Resolver resolver, UnbakedModel.ResolutionContext resolutionContext) {
		this.variants.forEach(variant -> resolver.resolve(variant.getModelLocation()));
	}

	@Nullable
	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
		if (this.variants.isEmpty()) {
			return null;
		} else {
			WeightedBakedModel.Builder builder = new WeightedBakedModel.Builder();

			for (Variant variant : this.variants) {
				BakedModel bakedModel = modelBaker.bake(variant.getModelLocation(), variant);
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
