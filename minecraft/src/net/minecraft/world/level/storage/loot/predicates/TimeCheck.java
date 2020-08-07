package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;

public class TimeCheck implements LootItemCondition {
	@Nullable
	private final Long period;
	private final RandomValueBounds value;

	private TimeCheck(@Nullable Long long_, RandomValueBounds randomValueBounds) {
		this.period = long_;
		this.value = randomValueBounds;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.TIME_CHECK;
	}

	public boolean test(LootContext lootContext) {
		ServerLevel serverLevel = lootContext.getLevel();
		long l = serverLevel.getDayTime();
		if (this.period != null) {
			l %= this.period;
		}

		return this.value.matchesValue((int)l);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
		public void serialize(JsonObject jsonObject, TimeCheck timeCheck, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("period", timeCheck.period);
			jsonObject.add("value", jsonSerializationContext.serialize(timeCheck.value));
		}

		public TimeCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			Long long_ = jsonObject.has("period") ? GsonHelper.getAsLong(jsonObject, "period") : null;
			RandomValueBounds randomValueBounds = GsonHelper.getAsObject(jsonObject, "value", jsonDeserializationContext, RandomValueBounds.class);
			return new TimeCheck(long_, randomValueBounds);
		}
	}
}
