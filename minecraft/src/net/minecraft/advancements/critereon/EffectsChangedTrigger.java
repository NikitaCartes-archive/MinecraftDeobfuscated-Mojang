package net.minecraft.advancements.critereon;

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

	public EffectsChangedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext
	) {
		MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
		return new EffectsChangedTrigger.TriggerInstance(composite, mobEffectsPredicate);
	}

	public void trigger(ServerPlayer serverPlayer) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MobEffectsPredicate effects;

		public TriggerInstance(EntityPredicate.Composite composite, MobEffectsPredicate mobEffectsPredicate) {
			super(EffectsChangedTrigger.ID, composite);
			this.effects = mobEffectsPredicate;
		}

		public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate mobEffectsPredicate) {
			return new EffectsChangedTrigger.TriggerInstance(EntityPredicate.Composite.ANY, mobEffectsPredicate);
		}

		public boolean matches(ServerPlayer serverPlayer) {
			return this.effects.matches((LivingEntity)serverPlayer);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("effects", this.effects.serializeToJson());
			return jsonObject;
		}
	}
}
