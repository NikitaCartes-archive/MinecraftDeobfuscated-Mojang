package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LocationTrigger extends SimpleCriterionTrigger<LocationTrigger.TriggerInstance> {
	private final ResourceLocation id;

	public LocationTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public LocationTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject);
		return new LocationTrigger.TriggerInstance(this.id, locationPredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(
			serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer.getLevel(), serverPlayer.x, serverPlayer.y, serverPlayer.z)
		);
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final LocationPredicate location;

		public TriggerInstance(ResourceLocation resourceLocation, LocationPredicate locationPredicate) {
			super(resourceLocation);
			this.location = locationPredicate;
		}

		public static LocationTrigger.TriggerInstance located(LocationPredicate locationPredicate) {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, locationPredicate);
		}

		public static LocationTrigger.TriggerInstance sleptInBed() {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, LocationPredicate.ANY);
		}

		public static LocationTrigger.TriggerInstance raidWon() {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, LocationPredicate.ANY);
		}

		public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
			return this.location.matches(serverLevel, d, e, f);
		}

		@Override
		public JsonElement serializeToJson() {
			return this.location.serializeToJson();
		}
	}
}
