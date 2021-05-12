package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("target_hit");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TargetBlockTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("signal_strength"));
		EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "projectile", deserializationContext);
		return new TargetBlockTrigger.TriggerInstance(composite, ints, composite2);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, Vec3 vec3, int i) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, vec3, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints signalStrength;
		private final EntityPredicate.Composite projectile;

		public TriggerInstance(EntityPredicate.Composite composite, MinMaxBounds.Ints ints, EntityPredicate.Composite composite2) {
			super(TargetBlockTrigger.ID, composite);
			this.signalStrength = ints;
			this.projectile = composite2;
		}

		public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints ints, EntityPredicate.Composite composite) {
			return new TargetBlockTrigger.TriggerInstance(EntityPredicate.Composite.ANY, ints, composite);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("signal_strength", this.signalStrength.serializeToJson());
			jsonObject.add("projectile", this.projectile.toJson(serializationContext));
			return jsonObject;
		}

		public boolean matches(LootContext lootContext, Vec3 vec3, int i) {
			return !this.signalStrength.matches(i) ? false : this.projectile.matches(lootContext);
		}
	}
}
