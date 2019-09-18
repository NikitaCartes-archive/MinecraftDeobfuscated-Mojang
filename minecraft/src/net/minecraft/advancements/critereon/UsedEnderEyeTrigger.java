package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class UsedEnderEyeTrigger extends SimpleCriterionTrigger<UsedEnderEyeTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("used_ender_eye");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public UsedEnderEyeTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MinMaxBounds.Floats floats = MinMaxBounds.Floats.fromJson(jsonObject.get("distance"));
		return new UsedEnderEyeTrigger.TriggerInstance(floats);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos) {
		double d = serverPlayer.x - (double)blockPos.getX();
		double e = serverPlayer.z - (double)blockPos.getZ();
		double f = d * d + e * e;
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(f));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Floats level;

		public TriggerInstance(MinMaxBounds.Floats floats) {
			super(UsedEnderEyeTrigger.ID);
			this.level = floats;
		}

		public boolean matches(double d) {
			return this.level.matchesSqr(d);
		}
	}
}
