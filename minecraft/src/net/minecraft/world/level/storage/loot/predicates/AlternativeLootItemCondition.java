package net.minecraft.world.level.storage.loot.predicates;

import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;

public class AlternativeLootItemCondition implements LootItemCondition {
	final LootItemCondition[] terms;
	private final Predicate<LootContext> composedPredicate;

	AlternativeLootItemCondition(LootItemCondition[] lootItemConditions) {
		this.terms = lootItemConditions;
		this.composedPredicate = LootItemConditions.orConditions(lootItemConditions);
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.ALTERNATIVE;
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

	public static AlternativeLootItemCondition.Builder alternative(LootItemCondition.Builder... builders) {
		return new AlternativeLootItemCondition.Builder(builders);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final List<LootItemCondition> terms = Lists.<LootItemCondition>newArrayList();

		public Builder(LootItemCondition.Builder... builders) {
			for (LootItemCondition.Builder builder : builders) {
				this.terms.add(builder.build());
			}
		}

		@Override
		public AlternativeLootItemCondition.Builder or(LootItemCondition.Builder builder) {
			this.terms.add(builder.build());
			return this;
		}

		@Override
		public LootItemCondition build() {
			return new AlternativeLootItemCondition((LootItemCondition[])this.terms.toArray(new LootItemCondition[0]));
		}
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<AlternativeLootItemCondition> {
		public void serialize(JsonObject jsonObject, AlternativeLootItemCondition alternativeLootItemCondition, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("terms", jsonSerializationContext.serialize(alternativeLootItemCondition.terms));
		}

		public AlternativeLootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LootItemCondition[] lootItemConditions = GsonHelper.getAsObject(jsonObject, "terms", jsonDeserializationContext, LootItemCondition[].class);
			return new AlternativeLootItemCondition(lootItemConditions);
		}
	}
}
