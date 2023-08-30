package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
	public UsedEnderEyeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromJson(jsonObject.get("distance"));
		return new UsedEnderEyeTrigger.TriggerInstance(optional, doubles);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos) {
		double d = serverPlayer.getX() - (double)blockPos.getX();
		double e = serverPlayer.getZ() - (double)blockPos.getZ();
		double f = d * d + e * e;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(f));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Doubles level;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, MinMaxBounds.Doubles doubles) {
			super(optional);
			this.level = doubles;
		}

		public boolean matches(double d) {
			return this.level.matchesSqr(d);
		}
	}
}
