package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("effects_changed");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public EffectsChangedTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
		return new EffectsChangedTrigger.TriggerInstance(mobEffectsPredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MobEffectsPredicate effects;

		public TriggerInstance(MobEffectsPredicate mobEffectsPredicate) {
			super(EffectsChangedTrigger.ID);
			this.effects = mobEffectsPredicate;
		}

		public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate mobEffectsPredicate) {
			return new EffectsChangedTrigger.TriggerInstance(mobEffectsPredicate);
		}

		public boolean matches(ServerPlayer serverPlayer) {
			return this.effects.matches((LivingEntity)serverPlayer);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("effects", this.effects.serializeToJson());
			return jsonObject;
		}
	}
}
