package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("used_ender_eye");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public UsedEnderEyeTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		MinMaxBounds.Doubles doubles = MinMaxBounds.Doubles.fromJson(jsonObject.get("distance"));
		return new UsedEnderEyeTrigger.TriggerInstance(contextAwarePredicate, doubles);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos) {
		double d = serverPlayer.getX() - (double)blockPos.getX();
		double e = serverPlayer.getZ() - (double)blockPos.getZ();
		double f = d * d + e * e;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(f));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Doubles level;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, MinMaxBounds.Doubles doubles) {
			super(UsedEnderEyeTrigger.ID, contextAwarePredicate);
			this.level = doubles;
		}

		public boolean matches(double d) {
			return this.level.matchesSqr(d);
		}
	}
}
