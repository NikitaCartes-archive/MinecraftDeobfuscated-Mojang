package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class LootItemRandomChanceCondition implements LootItemCondition {
	private final float probability;

	private LootItemRandomChanceCondition(float f) {
		this.probability = f;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.RANDOM_CHANCE;
	}

	public boolean test(LootContext lootContext) {
		return lootContext.getRandom().nextFloat() < this.probability;
	}

	public static LootItemCondition.Builder randomChance(float f) {
		return () -> new LootItemRandomChanceCondition(f);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootItemRandomChanceCondition> {
		public void serialize(JsonObject jsonObject, LootItemRandomChanceCondition lootItemRandomChanceCondition, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("chance", lootItemRandomChanceCondition.probability);
		}

		public LootItemRandomChanceCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			return new LootItemRandomChanceCondition(GsonHelper.getAsFloat(jsonObject, "chance"));
		}
	}
}
