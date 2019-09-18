package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;

public class StatePropertiesPredicate {
	public static final StatePropertiesPredicate ANY = new StatePropertiesPredicate(ImmutableList.of());
	private final List<StatePropertiesPredicate.PropertyMatcher> properties;

	private static StatePropertiesPredicate.PropertyMatcher fromJson(String string, JsonElement jsonElement) {
		if (jsonElement.isJsonPrimitive()) {
			String string2 = jsonElement.getAsString();
			return new StatePropertiesPredicate.ExactPropertyMatcher(string, string2);
		} else {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
			String string3 = jsonObject.has("min") ? getStringOrNull(jsonObject.get("min")) : null;
			String string4 = jsonObject.has("max") ? getStringOrNull(jsonObject.get("max")) : null;
			return (StatePropertiesPredicate.PropertyMatcher)(string3 != null && string3.equals(string4)
				? new StatePropertiesPredicate.ExactPropertyMatcher(string, string3)
				: new StatePropertiesPredicate.RangedPropertyMatcher(string, string3, string4));
		}
	}

	@Nullable
	private static String getStringOrNull(JsonElement jsonElement) {
		return jsonElement.isJsonNull() ? null : jsonElement.getAsString();
	}

	private StatePropertiesPredicate(List<StatePropertiesPredicate.PropertyMatcher> list) {
		this.properties = ImmutableList.copyOf(list);
	}

	public <S extends StateHolder<S>> boolean matches(StateDefinition<?, S> stateDefinition, S stateHolder) {
		for (StatePropertiesPredicate.PropertyMatcher propertyMatcher : this.properties) {
			if (!propertyMatcher.match(stateDefinition, stateHolder)) {
				return false;
			}
		}

		return true;
	}

	public boolean matches(BlockState blockState) {
		return this.matches(blockState.getBlock().getStateDefinition(), blockState);
	}

	public boolean matches(FluidState fluidState) {
		return this.matches(fluidState.getType().getStateDefinition(), fluidState);
	}

	public void checkState(StateDefinition<?, ?> stateDefinition, Consumer<String> consumer) {
		this.properties.forEach(propertyMatcher -> propertyMatcher.checkState(stateDefinition, consumer));
	}

	public static StatePropertiesPredicate fromJson(@Nullable JsonElement jsonElement) {
		if (jsonElement != null && !jsonElement.isJsonNull()) {
			JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "properties");
			List<StatePropertiesPredicate.PropertyMatcher> list = Lists.<StatePropertiesPredicate.PropertyMatcher>newArrayList();

			for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
				list.add(fromJson((String)entry.getKey(), (JsonElement)entry.getValue()));
			}

			return new StatePropertiesPredicate(list);
		} else {
			return ANY;
		}
	}

	public JsonElement serializeToJson() {
		if (this == ANY) {
			return JsonNull.INSTANCE;
		} else {
			JsonObject jsonObject = new JsonObject();
			if (!this.properties.isEmpty()) {
				this.properties.forEach(propertyMatcher -> jsonObject.add(propertyMatcher.getName(), propertyMatcher.toJson()));
			}

			return jsonObject;
		}
	}

	public static class Builder {
		private final List<StatePropertiesPredicate.PropertyMatcher> matchers = Lists.<StatePropertiesPredicate.PropertyMatcher>newArrayList();

		private Builder() {
		}

		public static StatePropertiesPredicate.Builder properties() {
			return new StatePropertiesPredicate.Builder();
		}

		public StatePropertiesPredicate.Builder hasProperty(Property<?> property, String string) {
			this.matchers.add(new StatePropertiesPredicate.ExactPropertyMatcher(property.getName(), string));
			return this;
		}

		public StatePropertiesPredicate.Builder hasProperty(Property<Integer> property, int i) {
			return this.hasProperty(property, Integer.toString(i));
		}

		public StatePropertiesPredicate.Builder hasProperty(Property<Boolean> property, boolean bl) {
			return this.hasProperty(property, Boolean.toString(bl));
		}

		public <T extends Comparable<T> & StringRepresentable> StatePropertiesPredicate.Builder hasProperty(Property<T> property, T comparable) {
			return this.hasProperty(property, comparable.getSerializedName());
		}

		public StatePropertiesPredicate build() {
			return new StatePropertiesPredicate(this.matchers);
		}
	}

	static class ExactPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
		private final String value;

		public ExactPropertyMatcher(String string, String string2) {
			super(string);
			this.value = string2;
		}

		@Override
		protected <T extends Comparable<T>> boolean match(StateHolder<?> stateHolder, Property<T> property) {
			T comparable = stateHolder.getValue(property);
			Optional<T> optional = property.getValue(this.value);
			return optional.isPresent() && comparable.compareTo(optional.get()) == 0;
		}

		@Override
		public JsonElement toJson() {
			return new JsonPrimitive(this.value);
		}
	}

	abstract static class PropertyMatcher {
		private final String name;

		public PropertyMatcher(String string) {
			this.name = string;
		}

		public <S extends StateHolder<S>> boolean match(StateDefinition<?, S> stateDefinition, S stateHolder) {
			Property<?> property = stateDefinition.getProperty(this.name);
			return property == null ? false : this.match(stateHolder, property);
		}

		protected abstract <T extends Comparable<T>> boolean match(StateHolder<?> stateHolder, Property<T> property);

		public abstract JsonElement toJson();

		public String getName() {
			return this.name;
		}

		public void checkState(StateDefinition<?, ?> stateDefinition, Consumer<String> consumer) {
			Property<?> property = stateDefinition.getProperty(this.name);
			if (property == null) {
				consumer.accept(this.name);
			}
		}
	}

	static class RangedPropertyMatcher extends StatePropertiesPredicate.PropertyMatcher {
		@Nullable
		private final String minValue;
		@Nullable
		private final String maxValue;

		public RangedPropertyMatcher(String string, @Nullable String string2, @Nullable String string3) {
			super(string);
			this.minValue = string2;
			this.maxValue = string3;
		}

		@Override
		protected <T extends Comparable<T>> boolean match(StateHolder<?> stateHolder, Property<T> property) {
			T comparable = stateHolder.getValue(property);
			if (this.minValue != null) {
				Optional<T> optional = property.getValue(this.minValue);
				if (!optional.isPresent() || comparable.compareTo(optional.get()) < 0) {
					return false;
				}
			}

			if (this.maxValue != null) {
				Optional<T> optional = property.getValue(this.maxValue);
				if (!optional.isPresent() || comparable.compareTo(optional.get()) > 0) {
					return false;
				}
			}

			return true;
		}

		@Override
		public JsonElement toJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.minValue != null) {
				jsonObject.addProperty("min", this.minValue);
			}

			if (this.maxValue != null) {
				jsonObject.addProperty("max", this.maxValue);
			}

			return jsonObject;
		}
	}
}
