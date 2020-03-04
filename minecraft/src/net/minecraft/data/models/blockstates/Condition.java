package net.minecraft.data.models.blockstates;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public interface Condition extends Supplier<JsonElement> {
	void validate(StateDefinition<?, ?> stateDefinition);

	static Condition.TerminalCondition condition() {
		return new Condition.TerminalCondition();
	}

	static Condition or(Condition... conditions) {
		return new Condition.CompositeCondition(Condition.Operation.OR, Arrays.asList(conditions));
	}

	public static class CompositeCondition implements Condition {
		private final Condition.Operation operation;
		private final List<Condition> subconditions;

		private CompositeCondition(Condition.Operation operation, List<Condition> list) {
			this.operation = operation;
			this.subconditions = list;
		}

		@Override
		public void validate(StateDefinition<?, ?> stateDefinition) {
			this.subconditions.forEach(condition -> condition.validate(stateDefinition));
		}

		public JsonElement get() {
			JsonArray jsonArray = new JsonArray();
			this.subconditions.stream().map(Supplier::get).forEach(jsonArray::add);
			JsonObject jsonObject = new JsonObject();
			jsonObject.add(this.operation.id, jsonArray);
			return jsonObject;
		}
	}

	public static enum Operation {
		AND("AND"),
		OR("OR");

		private final String id;

		private Operation(String string2) {
			this.id = string2;
		}
	}

	public static class TerminalCondition implements Condition {
		private final Map<Property<?>, String> terms = Maps.<Property<?>, String>newHashMap();

		private static <T extends Comparable<T>> String joinValues(Property<T> property, Stream<T> stream) {
			return (String)stream.map(property::getName).collect(Collectors.joining("|"));
		}

		private static <T extends Comparable<T>> String getTerm(Property<T> property, T comparable, T[] comparables) {
			return joinValues(property, Stream.concat(Stream.of(comparable), Stream.of(comparables)));
		}

		private <T extends Comparable<T>> void putValue(Property<T> property, String string) {
			String string2 = (String)this.terms.put(property, string);
			if (string2 != null) {
				throw new IllegalStateException("Tried to replace " + property + " value from " + string2 + " to " + string);
			}
		}

		public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> property, T comparable) {
			this.putValue(property, property.getName(comparable));
			return this;
		}

		@SafeVarargs
		public final <T extends Comparable<T>> Condition.TerminalCondition term(Property<T> property, T comparable, T... comparables) {
			this.putValue(property, getTerm(property, comparable, comparables));
			return this;
		}

		public JsonElement get() {
			JsonObject jsonObject = new JsonObject();
			this.terms.forEach((property, string) -> jsonObject.addProperty(property.getName(), string));
			return jsonObject;
		}

		@Override
		public void validate(StateDefinition<?, ?> stateDefinition) {
			List<Property<?>> list = (List<Property<?>>)this.terms
				.keySet()
				.stream()
				.filter(property -> stateDefinition.getProperty(property.getName()) != property)
				.collect(Collectors.toList());
			if (!list.isEmpty()) {
				throw new IllegalStateException("Properties " + list + " are missing from " + stateDefinition);
			}
		}
	}
}
