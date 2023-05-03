package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public abstract class CompositeLootItemCondition implements LootItemCondition {
	final LootItemCondition[] terms;
	private final Predicate<LootContext> composedPredicate;

	protected CompositeLootItemCondition(LootItemCondition[] lootItemConditions, Predicate<LootContext> predicate) {
		this.terms = lootItemConditions;
		this.composedPredicate = predicate;
	}

	public final boolean test(LootContext lootContext) {
		return this.composedPredicate.test(lootContext);
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootItemCondition.super.validate(validationContext);

		for (int i = 0; i < this.terms.length; i++) {
			this.terms[i].validate(validationContext.forChild(".term[" + i + "]"));
		}
	}

	public abstract static class Builder implements LootItemCondition.Builder {
		private final List<LootItemCondition> terms = new ArrayList();

		public Builder(LootItemCondition.Builder... builders) {
			for (LootItemCondition.Builder builder : builders) {
				this.terms.add(builder.build());
			}
		}

		public void addTerm(LootItemCondition.Builder builder) {
			this.terms.add(builder.build());
		}

		@Override
		public LootItemCondition build() {
			LootItemCondition[] lootItemConditions = (LootItemCondition[])this.terms.toArray(LootItemCondition[]::new);
			return this.create(lootItemConditions);
		}

		protected abstract LootItemCondition create(LootItemCondition[] lootItemConditions);
	}

	public abstract static class Serializer<T extends CompositeLootItemCondition> implements net.minecraft.world.level.storage.loot.Serializer<T> {
		public void serialize(JsonObject jsonObject, CompositeLootItemCondition compositeLootItemCondition, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("terms", jsonSerializationContext.serialize(compositeLootItemCondition.terms));
		}

		public T deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(jsonObject, "terms", jsonDeserializationContext, LootItemCondition[].class);
			return this.create(lootItemConditions);
		}

		protected abstract T create(LootItemCondition[] lootItemConditions);
	}
}
