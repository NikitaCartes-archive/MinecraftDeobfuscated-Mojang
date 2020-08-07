package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LocationTrigger extends SimpleCriterionTrigger<LocationTrigger.TriggerInstance> {
	private final ResourceLocation id;

	public LocationTrigger(ResourceLocation resourceLocation) {
		this.id = resourceLocation;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	public LocationTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "location", jsonObject);
		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject2);
		return new LocationTrigger.TriggerInstance(this.id, composite, locationPredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.getLevel(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final LocationPredicate location;

		public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite, LocationPredicate locationPredicate) {
			super(resourceLocation, composite);
			this.location = locationPredicate;
		}

		public static LocationTrigger.TriggerInstance located(LocationPredicate locationPredicate) {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.ANY, locationPredicate);
		}

		public static LocationTrigger.TriggerInstance sleptInBed() {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
		}

		public static LocationTrigger.TriggerInstance raidWon() {
			return new LocationTrigger.TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
		}

		public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
			return this.location.matches(serverLevel, d, e, f);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("location", this.location.serializeToJson());
			return jsonObject;
		}
	}
}
