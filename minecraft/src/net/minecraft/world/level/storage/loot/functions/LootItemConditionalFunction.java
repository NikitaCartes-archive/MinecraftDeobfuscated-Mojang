package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootItemConditionalFunction implements LootItemFunction {
	protected final LootItemCondition[] predicates;
	private final Predicate<LootContext> compositePredicates;

	protected LootItemConditionalFunction(LootItemCondition[] lootItemConditions) {
		this.predicates = lootItemConditions;
		this.compositePredicates = LootItemConditions.andConditions(lootItemConditions);
	}

	public final ItemStack apply(ItemStack itemStack, LootContext lootContext) {
		return this.compositePredicates.test(lootContext) ? this.run(itemStack, lootContext) : itemStack;
	}

	protected abstract ItemStack run(ItemStack itemStack, LootContext lootContext);

	@Override
	public void validate(ValidationContext validationContext) {
		LootItemFunction.super.validate(validationContext);

		for (int i = 0; i < this.predicates.length; i++) {
			this.predicates[i].validate(validationContext.forChild(".conditions[" + i + "]"));
		}
	}

	protected static LootItemConditionalFunction.Builder<?> simpleBuilder(Function<LootItemCondition[], LootItemFunction> function) {
		return new LootItemConditionalFunction.DummyBuilder(function);
	}

	public abstract static class Builder<T extends LootItemConditionalFunction.Builder<T>> implements LootItemFunction.Builder, ConditionUserBuilder<T> {
		private final List<LootItemCondition> conditions = Lists.<LootItemCondition>newArrayList();

		public T when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this.getThis();
		}

		public final T unwrap() {
			return this.getThis();
		}

		protected abstract T getThis();

		protected LootItemCondition[] getConditions() {
			return (LootItemCondition[])this.conditions.toArray(new LootItemCondition[0]);
		}
	}

	static final class DummyBuilder extends LootItemConditionalFunction.Builder<LootItemConditionalFunction.DummyBuilder> {
		private final Function<LootItemCondition[], LootItemFunction> constructor;

		public DummyBuilder(Function<LootItemCondition[], LootItemFunction> function) {
			this.constructor = function;
		}

		protected LootItemConditionalFunction.DummyBuilder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return (LootItemFunction)this.constructor.apply(this.getConditions());
		}
	}

	public abstract static class Serializer<T extends LootItemConditionalFunction> implements net.minecraft.world.level.storage.loot.Serializer<T> {
		public void serialize(JsonObject jsonObject, T lootItemConditionalFunction, JsonSerializationContext jsonSerializationContext) {
			if (!ArrayUtils.isEmpty((Object[])lootItemConditionalFunction.predicates)) {
				jsonObject.add("conditions", jsonSerializationContext.serialize(lootItemConditionalFunction.predicates));
			}
		}

		public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(
				jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class
			);
			return this.deserialize(jsonObject, jsonDeserializationContext, lootItemConditions);
		}

		public abstract T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions);
	}
}
