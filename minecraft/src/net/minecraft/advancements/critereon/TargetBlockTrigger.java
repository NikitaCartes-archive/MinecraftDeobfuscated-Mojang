package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class TargetBlockTrigger extends SimpleCriterionTrigger<TargetBlockTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("target_hit");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public TargetBlockTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("signal_strength"));
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("projectile"));
		EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("shooter"));
		return new TargetBlockTrigger.TriggerInstance(ints, entityPredicate, entityPredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, Entity entity, Vec3 vec3, int i) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, entity, vec3, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MinMaxBounds.Ints signalStrength;
		private final EntityPredicate projectile;
		private final EntityPredicate shooter;

		public TriggerInstance(MinMaxBounds.Ints ints, EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
			super(TargetBlockTrigger.ID);
			this.signalStrength = ints;
			this.projectile = entityPredicate;
			this.shooter = entityPredicate2;
		}

		public static TargetBlockTrigger.TriggerInstance targetHit(MinMaxBounds.Ints ints) {
			return new TargetBlockTrigger.TriggerInstance(ints, EntityPredicate.ANY, EntityPredicate.ANY);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("signal_strength", this.signalStrength.serializeToJson());
			jsonObject.add("projectile", this.projectile.serializeToJson());
			jsonObject.add("shooter", this.shooter.serializeToJson());
			return jsonObject;
		}

		public boolean matches(ServerPlayer serverPlayer, Entity entity, Vec3 vec3, int i) {
			if (!this.signalStrength.matches(i)) {
				return false;
			} else {
				return !this.projectile.matches(serverPlayer, entity) ? false : this.shooter.matches(serverPlayer.getLevel(), vec3, serverPlayer);
			}
		}
	}
}
