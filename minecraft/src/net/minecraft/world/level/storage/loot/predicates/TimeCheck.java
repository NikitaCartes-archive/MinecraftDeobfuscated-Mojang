package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.IntRange;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;

public class TimeCheck implements LootItemCondition {
	@Nullable
	private final Long period;
	private final IntRange value;

	private TimeCheck(@Nullable Long long_, IntRange intRange) {
		this.period = long_;
		this.value = intRange;
	}

	@Override
	public LootItemConditionType getType() {
		return LootItemConditions.TIME_CHECK;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.value.getReferencedContextParams();
	}

	public boolean test(LootContext lootContext) {
		ServerLevel serverLevel = lootContext.getLevel();
		long l = serverLevel.getDayTime();
		if (this.period != null) {
			l %= this.period;
		}

		return this.value.test(lootContext, (int)l);
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
		public void serialize(JsonObject jsonObject, TimeCheck timeCheck, JsonSerializationContext jsonSerializationContext) {
			jsonObject.addProperty("period", timeCheck.period);
			jsonObject.add("value", jsonSerializationContext.serialize(timeCheck.value));
		}

		public TimeCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
			Long long_ = jsonObject.has("period") ? GsonHelper.getAsLong(jsonObject, "period") : null;
			IntRange intRange = GsonHelper.getAsObject(jsonObject, "value", jsonDeserializationContext, IntRange.class);
			return new TimeCheck(long_, intRange);
		}
	}
}
