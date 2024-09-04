package net.minecraft.client.renderer.block.model.multipart;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.block.model.UnbakedBlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Environment(EnvType.CLIENT)
public class MultiPart implements UnbakedBlockStateModel {
	private final List<MultiPart.InstantiatedSelector> selectors;

	MultiPart(List<MultiPart.InstantiatedSelector> list) {
		this.selectors = list;
	}

	@Override
	public Object visualEqualityGroup(BlockState blockState) {
		IntList intList = new IntArrayList();

		for (int i = 0; i < this.selectors.size(); i++) {
			if (((MultiPart.InstantiatedSelector)this.selectors.get(i)).predicate.test(blockState)) {
				intList.add(i);
			}
		}

		@Environment(EnvType.CLIENT)
		record Key(MultiPart model, IntList selectors) {
			Key(IntList selectors) {
				this.selectors = selectors;
			}
		}

		return new Key(intList);
	}

	@Override
	public void resolveDependencies(UnbakedModel.Resolver resolver) {
		this.selectors.forEach(instantiatedSelector -> instantiatedSelector.variant.resolveDependencies(resolver));
	}

	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
		List<MultiPartBakedModel.Selector> list = new ArrayList(this.selectors.size());

		for (MultiPart.InstantiatedSelector instantiatedSelector : this.selectors) {
			BakedModel bakedModel = instantiatedSelector.variant.bake(modelBaker, function, modelState);
			list.add(new MultiPartBakedModel.Selector(instantiatedSelector.predicate, bakedModel));
		}

		return new MultiPartBakedModel(list);
	}

	@Environment(EnvType.CLIENT)
	public static record Definition(List<Selector> selectors) {
		public MultiPart instantiate(StateDefinition<Block, BlockState> stateDefinition) {
			List<MultiPart.InstantiatedSelector> list = this.selectors
				.stream()
				.map(selector -> new MultiPart.InstantiatedSelector(selector.getPredicate(stateDefinition), selector.getVariant()))
				.toList();
			return new MultiPart(list);
		}

		public Set<MultiVariant> getMultiVariants() {
			return (Set<MultiVariant>)this.selectors.stream().map(Selector::getVariant).collect(Collectors.toSet());
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<MultiPart.Definition> {
		public MultiPart.Definition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return new MultiPart.Definition(this.getSelectors(jsonDeserializationContext, jsonElement.getAsJsonArray()));
		}

		private List<Selector> getSelectors(JsonDeserializationContext jsonDeserializationContext, JsonArray jsonArray) {
			List<Selector> list = new ArrayList();
			if (jsonArray.isEmpty()) {
				throw new JsonSyntaxException("Empty selector array");
			} else {
				for (JsonElement jsonElement : jsonArray) {
					list.add((Selector)jsonDeserializationContext.deserialize(jsonElement, Selector.class));
				}

				return list;
			}
		}
	}

	@Environment(EnvType.CLIENT)
	static record InstantiatedSelector(Predicate<BlockState> predicate, MultiVariant variant) {
	}
}
