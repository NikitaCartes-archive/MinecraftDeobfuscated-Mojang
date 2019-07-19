package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTableProblemCollector;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;

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
	public void validate(
		LootTableProblemCollector lootTableProblemCollector,
		Function<ResourceLocation, LootTable> function,
		Set<ResourceLocation> set,
		LootContextParamSet lootContextParamSet
	) {
		LootItemCondition.super.validate(lootTableProblemCollector, function, set, lootContextParamSet);
		this.term.validate(lootTableProblemCollector, function, set, lootContextParamSet);
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
