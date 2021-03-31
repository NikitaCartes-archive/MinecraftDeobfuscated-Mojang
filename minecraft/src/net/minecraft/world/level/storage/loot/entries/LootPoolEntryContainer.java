package net.minecraft.world.level.storage.loot.entries;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.apache.commons.lang3.ArrayUtils;

public abstract class LootPoolEntryContainer implements ComposableEntryContainer {
	protected final LootItemCondition[] conditions;
	private final Predicate<LootContext> compositeCondition;

	protected LootPoolEntryContainer(LootItemCondition[] lootItemConditions) {
		this.conditions = lootItemConditions;
		this.compositeCondition = LootItemConditions.andConditions(lootItemConditions);
	}

	public void validate(ValidationContext validationContext) {
		for (int i = 0; i < this.conditions.length; i++) {
			this.conditions[i].validate(validationContext.forChild(".condition[" + i + "]"));
		}
	}

	protected final boolean canRun(LootContext lootContext) {
		return this.compositeCondition.test(lootContext);
	}

	public abstract LootPoolEntryType getType();

	public abstract static class Builder<T extends LootPoolEntryContainer.Builder<T>> implements ConditionUserBuilder<T> {
		private final List<LootItemCondition> conditions = Lists.<LootItemCondition>newArrayList();

		protected abstract T getThis();

		public T when(LootItemCondition.Builder builder) {
			this.conditions.add(builder.build());
			return this.getThis();
		}

		public final T unwrap() {
			return this.getThis();
		}

		protected LootItemCondition[] getConditions() {
			return (LootItemCondition[])this.conditions.toArray(new LootItemCondition[0]);
		}

		public AlternativesEntry.Builder otherwise(LootPoolEntryContainer.Builder<?> builder) {
			return new AlternativesEntry.Builder(this, builder);
		}

		public EntryGroup.Builder append(LootPoolEntryContainer.Builder<?> builder) {
			return new EntryGroup.Builder(this, builder);
		}

		public SequentialEntry.Builder then(LootPoolEntryContainer.Builder<?> builder) {
			return new SequentialEntry.Builder(this, builder);
		}

		public abstract LootPoolEntryContainer build();
	}

	public abstract static class Serializer<T extends LootPoolEntryContainer> implements net.minecraft.world.level.storage.loot.Serializer<T> {
		public final void serialize(JsonObject jsonObject, T lootPoolEntryContainer, JsonSerializationContext jsonSerializationContext) {
			if (!ArrayUtils.isEmpty((Object[])lootPoolEntryContainer.conditions)) {
				jsonObject.add("conditions", jsonSerializationContext.serialize(lootPoolEntryContainer.conditions));
			}

			this.serializeCustom(jsonObject, lootPoolEntryContainer, jsonSerializationContext);
		}

		public final T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(
				jsonObject, "conditions", new LootItemCondition[0], jsonDeserializationContext, LootItemCondition[].class
			);
			return this.deserializeCustom(jsonObject, jsonDeserializationContext, lootItemConditions);
		}

		public abstract void serializeCustom(JsonObject jsonObject, T lootPoolEntryContainer, JsonSerializationContext jsonSerializationContext);

		public abstract T deserializeCustom(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions);
	}
}
