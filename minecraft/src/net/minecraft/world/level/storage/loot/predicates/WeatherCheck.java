package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;

public class WeatherCheck implements LootItemCondition {
	@Nullable
	private final Boolean isRaining;
	@Nullable
	private final Boolean isThundering;

	private WeatherCheck(@Nullable Boolean boolean_, @Nullable Boolean boolean2) {
		this.isRaining = boolean_;
		this.isThundering = boolean2;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.WEATHER_CHECK;
	}

	public boolean test(LootContext lootContext) {
		ServerLevel serverLevel = lootContext.getLevel();
		return this.isRaining != null && this.isRaining != serverLevel.isRaining()
			? false
			: this.isThundering == null || this.isThundering == serverLevel.isThundering();
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<WeatherCheck> {
		public void serialize(JsonObject jsonObject, WeatherCheck weatherCheck, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("raining", weatherCheck.isRaining);
			jsonObject.addProperty("thundering", weatherCheck.isThundering);
		}

		public WeatherCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			Boolean boolean_ = jsonObject.has("raining") ? GsonHelper.getAsBoolean(jsonObject, "raining") : null;
			Boolean boolean2 = jsonObject.has("thundering") ? GsonHelper.getAsBoolean(jsonObject, "thundering") : null;
			return new WeatherCheck(boolean_, boolean2);
		}
	}
}
