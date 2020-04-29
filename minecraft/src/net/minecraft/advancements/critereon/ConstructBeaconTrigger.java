package net.minecraft.advancements.critereon;

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

	public ConstructBeaconTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
		return new ConstructBeaconTrigger.TriggerInstance(composite, ints);
	}

	public void trigger(ServerPlayer serverPlayer, BeaconBlockEntity beaconBlockEntity) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(beaconBlockEntity));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints level;

		public TriggerInstance(EntityPredicate.Composite composite, MinMaxBounds.Ints ints) {
			super(ConstructBeaconTrigger.ID, composite);
			this.level = ints;
		}

		public static ConstructBeaconTrigger.TriggerInstance constructedBeacon(MinMaxBounds.Ints ints) {
			return new ConstructBeaconTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ints);
		}

		public boolean matches(BeaconBlockEntity beaconBlockEntity) {
			return this.level.matches(beaconBlockEntity.getLevels());
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("level", this.level.serializeToJson());
			return jsonObject;
		}
	}
}
