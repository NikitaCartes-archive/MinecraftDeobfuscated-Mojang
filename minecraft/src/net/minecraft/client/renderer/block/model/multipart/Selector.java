package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.MultiVariant;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

@Environment(EnvType.CLIENT)
public class Selector {
	private final Condition condition;
	private final MultiVariant variant;

	public Selector(Condition condition, MultiVariant multiVariant) {
		if (condition == null) {
			throw new IllegalArgumentException("Missing condition for selector");
		} else if (multiVariant == null) {
			throw new IllegalArgumentException("Missing variant for selector");
		} else {
			this.condition = condition;
			this.variant = multiVariant;
		}
	}

	public MultiVariant getVariant() {
		return this.variant;
	}

	public Predicate<BlockState> getPredicate(StateDefinition<Block, BlockState> stateDefinition) {
		return this.condition.getPredicate(stateDefinition);
	}

	public boolean equals(Object object) {
		return this == object;
	}

	public int hashCode() {
		return System.identityHashCode(this);
	}

	@Environment(EnvType.CLIENT)
	public static class Deserializer implements JsonDeserializer<Selector> {
		public Selector deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			return new Selector(this.getSelector(jsonObject), jsonDeserializationContext.deserialize(jsonObject.get("apply"), MultiVariant.class));
		}

		private Condition getSelector(JsonObject jsonObject) {
			return jsonObject.has("when") ? getCondition(GsonHelper.getAsJsonObject(jsonObject, "when")) : Condition.TRUE;
		}

		@VisibleForTesting
		static Condition getCondition(JsonObject jsonObject) {
			Set<Entry<String, JsonElement>> set = jsonObject.entrySet();
			if (set.isEmpty()) {
				throw new JsonParseException("No elements found in selector");
			} else if (set.size() == 1) {
				if (jsonObject.has("OR")) {
					List<Condition> list = (List<Condition>)Streams.stream(GsonHelper.getAsJsonArray(jsonObject, "OR"))
						.map(jsonElement -> getCondition(jsonElement.getAsJsonObject()))
						.collect(Collectors.toList());
					return new OrCondition(list);
				} else if (jsonObject.has("AND")) {
					List<Condition> list = (List<Condition>)Streams.stream(GsonHelper.getAsJsonArray(jsonObject, "AND"))
						.map(jsonElement -> getCondition(jsonElement.getAsJsonObject()))
						.collect(Collectors.toList());
					return new AndCondition(list);
				} else {
					return getKeyValueCondition((Entry<String, JsonElement>)set.iterator().next());
				}
			} else {
				return new AndCondition((Iterable<? extends Condition>)set.stream().map(Selector.Deserializer::getKeyValueCondition).collect(Collectors.toList()));
			}
		}

		private static Condition getKeyValueCondition(Entry<String, JsonElement> entry) {
			return new KeyValueCondition((String)entry.getKey(), ((JsonElement)entry.getValue()).getAsString());
		}
	}
}
