package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;

public class ConstructBeaconTrigger extends SimpleCriterionTrigger<ConstructBeaconTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("construct_beacon");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public ConstructBeaconTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
		return new ConstructBeaconTrigger.TriggerInstance(ints);
	}

	public void trigger(ServerPlayer serverPlayer, BeaconBlockEntity beaconBlockEntity) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(beaconBlockEntity));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints level;

		public TriggerInstance(MinMaxBounds.Ints ints) {
			super(ConstructBeaconTrigger.ID);
			this.level = ints;
		}

		public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints ints) {
			return new ConstructBeaconTrigger.TriggerInstance(ints);
		}

		public boolean matches(BeaconBlockEntity beaconBlockEntity) {
			return this.level.matches(beaconBlockEntity.getLevels());
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("level", this.level.serializeToJson());
			return jsonObject;
		}
	}
}
