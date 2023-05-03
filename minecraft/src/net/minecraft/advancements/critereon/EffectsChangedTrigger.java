package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("effects_changed");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public EffectsChangedTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
		ContextAwarePredicate contextAwarePredicate2 = EntityPredicate.fromJson(jsonObject, "source", deserializationContext);
		return new EffectsChangedTrigger.TriggerInstance(contextAwarePredicate, mobEffectsPredicate, contextAwarePredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, @Nullable Entity entity) {
		LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final MobEffectsPredicate effects;
		private final ContextAwarePredicate source;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, MobEffectsPredicate mobEffectsPredicate, ContextAwarePredicate contextAwarePredicate2) {
			super(EffectsChangedTrigger.ID, contextAwarePredicate);
			this.effects = mobEffectsPredicate;
			this.source = contextAwarePredicate2;
		}

		public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate mobEffectsPredicate) {
			return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.ANY, mobEffectsPredicate, ContextAwarePredicate.ANY);
		}

		public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(EntityPredicate entityPredicate) {
			return new EffectsChangedTrigger.TriggerInstance(ContextAwarePredicate.ANY, MobEffectsPredicate.ANY, EntityPredicate.wrap(entityPredicate));
		}

		public boolean matches(ServerPlayer serverPlayer, @Nullable LootContext lootContext) {
			return !this.effects.matches((LivingEntity)serverPlayer)
				? false
				: this.source == ContextAwarePredicate.ANY || lootContext != null && this.source.matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("effects", this.effects.serializeToJson());
			jsonObject.add("source", this.source.toJson(serializationContext));
			return jsonObject;
		}
	}
}
