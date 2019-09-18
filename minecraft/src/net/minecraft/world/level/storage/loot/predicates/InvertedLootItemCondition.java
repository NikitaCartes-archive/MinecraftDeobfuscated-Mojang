package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class InvertedLootItemCondition implements LootItemCondition {
	private final LootItemCondition term;

	private InvertedLootItemCondition(LootItemCondition lootItemCondition) {
		this.term = lootItemCondition;
	}

	public final boolean test(LootContext lootContext) {
		return !this.term.test(lootContext);
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.term.getReferencedContextParams();
	}

	@Override
	public void validate(ValidationContext validationContext) {
		LootItemCondition.super.validate(validationContext);
		this.term.validate(validationContext);
	}

	public static LootItemCondition.Builder invert(LootItemCondition.Builder builder) {
		InvertedLootItemCondition invertedLootItemCondition = new InvertedLootItemCondition(builder.build());
		return () -> invertedLootItemCondition;
	}

	public static class Serializer extends LootItemCondition.Serializer<InvertedLootItemCondition> {
		public Serializer() {
			super(new ResourceLocation("inverted"), InvertedLootItemCondition.class);
		}

		public void serialize(JsonObject jsonObject, InvertedLootItemCondition invertedLootItemCondition, JsonSerializationContext jsonSerializationContext) {
			jsonObject.add("term", jsonSerializationContext.serialize(invertedLootItemCondition.term));
		}

		public InvertedLootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			LootItemCondition lootItemCondition = GsonHelper.getAsObject(jsonObject, "term", jsonDeserializationContext, LootItemCondition.class);
			return new InvertedLootItemCondition(lootItemCondition);
		}
	}
}
