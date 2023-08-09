package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.Optional;
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
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		Optional<MobEffectsPredicate> optional2 = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
		Optional<ContextAwarePredicate> optional3 = EntityPredicate.fromJson(jsonObject, "source", deserializationContext);
		return new EffectsChangedTrigger.TriggerInstance(optional, optional2, optional3);
	}

	public void trigger(ServerPlayer serverPlayer, @Nullable Entity entity) {
		LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Optional<MobEffectsPredicate> effects;
		private final Optional<ContextAwarePredicate> source;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, Optional<MobEffectsPredicate> optional2, Optional<ContextAwarePredicate> optional3) {
			super(EffectsChangedTrigger.ID, optional);
			this.effects = optional2;
			this.source = optional3;
		}

		public static EffectsChangedTrigger.TriggerInstance hasEffects(MobEffectsPredicate.Builder builder) {
			return new EffectsChangedTrigger.TriggerInstance(Optional.empty(), builder.build(), Optional.empty());
		}

		public static EffectsChangedTrigger.TriggerInstance gotEffectsFrom(Optional<EntityPredicate> optional) {
			return new EffectsChangedTrigger.TriggerInstance(Optional.empty(), Optional.empty(), EntityPredicate.wrap(optional));
		}

		public boolean matches(ServerPlayer serverPlayer, @Nullable LootContext lootContext) {
			return this.effects.isPresent() && !((MobEffectsPredicate)this.effects.get()).matches((LivingEntity)serverPlayer)
				? false
				: !this.source.isPresent() || lootContext != null && ((ContextAwarePredicate)this.source.get()).matches(lootContext);
		}

		@Override
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			this.effects.ifPresent(mobEffectsPredicate -> jsonObject.add("effects", mobEffectsPredicate.serializeToJson()));
			this.source.ifPresent(contextAwarePredicate -> jsonObject.add("source", contextAwarePredicate.toJson()));
			return jsonObject;
		}
	}
}
