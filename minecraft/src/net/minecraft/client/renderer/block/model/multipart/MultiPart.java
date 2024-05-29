package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Environment(EnvType.CLIENT)
public class MultiPart implements UnbakedModel {
	private final StateDefinition<Block, BlockState> definition;
	private final List<Selector> selectors;

	public MultiPart(StateDefinition<Block, BlockState> stateDefinition, List<Selector> list) {
		this.definition = stateDefinition;
		this.selectors = list;
	}

	public List<Selector> getSelectors() {
		return this.selectors;
	}

	public Set<MultiVariant> getMultiVariants() {
		Set<MultiVariant> set = Sets.<MultiVariant>newHashSet();

		for (Selector selector : this.selectors) {
			set.add(selector.getVariant());
		}

		return set;
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof MultiPart multiPart)
				? false
				: Objects.equals(this.definition, multiPart.definition) && Objects.equals(this.selectors, multiPart.selectors);
		}
	}

	public int hashCode() {
		return Objects.hash(new Object[]{this.definition, this.selectors});
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return (Collection<ResourceLocation>)this.getSelectors()
			.stream()
			.flatMap(selector -> selector.getVariant().getDependencies().stream())
			.collect(Collectors.toSet());
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> function) {
		this.getSelectors().forEach(selector -> selector.getVariant().resolveParents(function));
	}

	@Nullable
	@Override
	public BakedModel bake(ModelBaker modelBaker, Function<Material, TextureAtlasSprite> function, ModelState modelState) {
		MultiPartBakedModel.Builder builder = new MultiPartBakedModel.Builder();

		for (Selector selector : this.getSelectors()) {
			BakedModel bakedModel = selector.getVariant().bake(modelBaker, function, modelState);
			if (bakedModel != null) {
				builder.add(selector.getPredicate(this.definition), bakedModel);
			}
		}

		return builder.build();
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<MultiPart> {
		private final BlockModelDefinition.Context context;

		public Deserializer(BlockModelDefinition.Context context) {
			this.context = context;
		}

		public MultiPart deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			return new MultiPart(this.context.getDefinition(), this.getSelectors(jsonDeserializationContext, jsonElement.getAsJsonArray()));
		}

		private List<Selector> getSelectors(JsonDeserializationContext jsonDeserializationContext, JsonArray jsonArray) {
			List<Selector> list = Lists.<Selector>newArrayList();

			for (JsonElement jsonElement : jsonArray) {
				list.add((Selector)jsonDeserializationContext.deserialize(jsonElement, Selector.class));
			}

			return list;
		}
	}
}
