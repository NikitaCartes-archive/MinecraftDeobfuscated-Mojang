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
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.multipart.MultiPart;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Environment(EnvType.CLIENT)
public class BlockModelDefinition {
	private final Map<String, MultiVariant> variants = Maps.<String, MultiVariant>newLinkedHashMap();
	private MultiPart multiPart;

	public static BlockModelDefinition fromStream(BlockModelDefinition.Context context, Reader reader) {
		return GsonHelper.fromJson(context.gson, reader, BlockModelDefinition.class);
	}

	public static BlockModelDefinition fromJsonElement(BlockModelDefinition.Context context, JsonElement jsonElement) {
		return context.gson.fromJson(jsonElement, BlockModelDefinition.class);
	}

	public BlockModelDefinition(Map<String, MultiVariant> map, MultiPart multiPart) {
		this.multiPart = multiPart;
		this.variants.putAll(map);
	}

	public BlockModelDefinition(List<BlockModelDefinition> list) {
		BlockModelDefinition blockModelDefinition = null;

		for (BlockModelDefinition blockModelDefinition2 : list) {
			if (blockModelDefinition2.isMultiPart()) {
				this.variants.clear();
				blockModelDefinition = blockModelDefinition2;
			}

			this.variants.putAll(blockModelDefinition2.variants);
		}

		if (blockModelDefinition != null) {
			this.multiPart = blockModelDefinition.multiPart;
		}
	}

	@VisibleForTesting
	public boolean hasVariant(String string) {
		return this.variants.get(string) != null;
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
			if (object instanceof BlockModelDefinition blockModelDefinition && this.variants.equals(blockModelDefinition.variants)) {
				return this.isMultiPart() ? this.multiPart.equals(blockModelDefinition.multiPart) : !blockModelDefinition.isMultiPart();
			}

			return false;
		}
	}

	public int hashCode() {
		return 31 * this.variants.hashCode() + (this.isMultiPart() ? this.multiPart.hashCode() : 0);
	}

	public Map<String, MultiVariant> getVariants() {
		return this.variants;
	}

	@VisibleForTesting
	public Set<MultiVariant> getMultiVariants() {
		Set<MultiVariant> set = Sets.<MultiVariant>newHashSet(this.variants.values());
		if (this.isMultiPart()) {
			set.addAll(this.multiPart.getMultiVariants());
		}

		return set;
	}

	public boolean isMultiPart() {
		return this.multiPart != null;
	}

	public MultiPart getMultiPart() {
		return this.multiPart;
	}

	@Environment(EnvType.CLIENT)
	public static final class Context {
		protected final Gson gson = new GsonBuilder()
			.registerTypeAdapter(BlockModelDefinition.class, new BlockModelDefinition.Deserializer())
			.registerTypeAdapter(Variant.class, new Variant.Deserializer())
			.registerTypeAdapter(MultiVariant.class, new MultiVariant.Deserializer())
			.registerTypeAdapter(MultiPart.class, new MultiPart.Deserializer(this))
			.registerTypeAdapter(Selector.class, new Selector.Deserializer())
			.create();
		private StateDefinition<Block, BlockState> definition;

		public StateDefinition<Block, BlockState> getDefinition() {
			return this.definition;
		}

		public void setDefinition(StateDefinition<Block, BlockState> stateDefinition) {
			this.definition = stateDefinition;
		}
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<BlockModelDefinition> {
		public BlockModelDefinition deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			Map<String, MultiVariant> map = this.getVariants(jsonDeserializationContext, jsonObject);
			MultiPart multiPart = this.getMultiPart(jsonDeserializationContext, jsonObject);
			if (!map.isEmpty() || multiPart != null && !multiPart.getMultiVariants().isEmpty()) {
				return new BlockModelDefinition(map, multiPart);
			} else {
				throw new JsonParseException("Neither 'variants' nor 'multipart' found");
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
		protected MultiPart getMultiPart(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
			if (!jsonObject.has("multipart")) {
				return null;
			} else {
				JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "multipart");
				return jsonDeserializationContext.deserialize(jsonArray, MultiPart.class);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	protected class MissingVariantException extends RuntimeException {
	}
}
