package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("target_hit");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TargetBlockTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("signalStrength"));
		return new TargetBlockTrigger.TriggerInstance(ints);
	}

	public void trigger(ServerPlayer serverPlayer, int i) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints signalStrength;

		public TriggerInstance(MinMaxBounds.Ints ints) {
			super(TargetBlockTrigger.ID);
			this.signalStrength = ints;
		}

		public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints ints) {
			return new TargetBlockTrigger.TriggerInstance(ints);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("signalStrength", this.signalStrength.serializeToJson());
			return jsonObject;
		}

		public boolean matches(int i) {
			return this.signalStrength.matches(i);
		}
	}
}
