package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class BlockModelDefinition {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final Gson GSON = new GsonBuilder()
		.registerTypeAdapter(BlockModelDefinition.class, new BlockModelDefinition.Deserializer())
		.registerTypeAdapter(Variant.class, new Variant.Deserializer())
		.registerTypeAdapter(MultiVariant.class, new MultiVariant.Deserializer())
		.registerTypeAdapter(MultiPart.Definition.class, new MultiPart.Deserializer())
		.registerTypeAdapter(Selector.class, new Selector.Deserializer())
		.create();
	private final Map<String, MultiVariant> variants;
	@Nullable
	private final MultiPart.Definition multiPart;

	public static BlockModelDefinition fromStream(Reader reader) {
		return GsonHelper.fromJson(GSON, reader, BlockModelDefinition.class);
	}

	public static BlockModelDefinition fromJsonElement(JsonElement jsonElement) {
		return GSON.fromJson(jsonElement, BlockModelDefinition.class);
	}

	public BlockModelDefinition(Map<String, MultiVariant> map, @Nullable MultiPart.Definition definition) {
		this.multiPart = definition;
		this.variants = map;
	}

	@VisibleForTesting
	public MultiVariant getVariant(String string) {
		MultiVariant multiVariant = (MultiVariant)this.variants.get(string);
		if (multiVariant == null) {
			throw new BlockModelDefinition.MissingVariantException();
		} else {
			return multiVariant;
		}
	}

	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else {
			return !(object instanceof BlockModelDefinition blockModelDefinition)
				? false
				: this.variants.equals(blockModelDefinition.variants) && Objects.equals(this.multiPart, blockModelDefinition.multiPart);
		}
	}

	public int hashCode() {
		return 31 * this.variants.hashCode() + (this.multiPart != null ? this.multiPart.hashCode() : 0);
	}

	@VisibleForTesting
	public Set<MultiVariant> getMultiVariants() {
		Set<MultiVariant> set = Sets.<MultiVariant>newHashSet(this.variants.values());
		if (this.multiPart != null) {
			set.addAll(this.multiPart.getMultiVariants());
		}

		return set;
	}

	@Nullable
	public MultiPart.Definition getMultiPart() {
		return this.multiPart;
	}

	public Map<BlockState, UnbakedBlockStateModel> instantiate(StateDefinition<Block, BlockState> stateDefinition, String string) {
		Map<BlockState, UnbakedBlockStateModel> map = new IdentityHashMap();
		List<BlockState> list = stateDefinition.getPossibleStates();
		MultiPart multiPart;
		if (this.multiPart != null) {
			multiPart = this.multiPart.instantiate(stateDefinition);
			list.forEach(blockState -> map.put(blockState, multiPart));
		} else {
			multiPart = null;
		}

		this.variants.forEach((string2, multiVariant) -> {
			try {
				list.stream().filter(VariantSelector.predicate(stateDefinition, string2)).forEach(blockState -> {
					UnbakedModel unbakedModel = (UnbakedModel)map.put(blockState, multiVariant);
					if (unbakedModel != null && unbakedModel != multiPart) {
						String stringxx = (String)((Entry)this.variants.entrySet().stream().filter(entry -> entry.getValue() == unbakedModel).findFirst().get()).getKey();
						throw new RuntimeException("Overlapping definition with: " + stringxx);
					}
				});
			} catch (Exception var9) {
				LOGGER.warn("Exception loading blockstate definition: '{}' for variant: '{}': {}", string, string2, var9.getMessage());
			}
		});
		return map;
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<BlockModelDefinition> {
		public BlockModelDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Map<String, MultiVariant> map = this.getVariants(jsonDeserializationContext, jsonObject);
			MultiPart.Definition definition = this.getMultiPart(jsonDeserializationContext, jsonObject);
			if (map.isEmpty() && definition == null) {
				throw new JsonParseException("Neither 'variants' nor 'multipart' found");
			} else {
				return new BlockModelDefinition(map, definition);
			}
		}

		protected Map<String, MultiVariant> getVariants(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			Map<String, MultiVariant> map = Maps.<String, MultiVariant>newHashMap();
			if (jsonObject.has("variants")) {
				JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "variants");

				for (Entry<String, JsonElement> entry : jsonObject2.entrySet()) {
					map.put((String)entry.getKey(), (MultiVariant)jsonDeserializationContext.deserialize((JsonElement)entry.getValue(), MultiVariant.class));
				}
			}

			return map;
		}

		@Nullable
		protected MultiPart.Definition getMultiPart(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			if (!jsonObject.has("multipart")) {
				return null;
			} else {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "multipart");
				return jsonDeserializationContext.deserialize(jsonArray, MultiPart.Definition.class);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	protected static class MissingVariantException extends RuntimeException {
	}
}
