package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;

public class EffectsChangedTrigger extends SimpleCriterionTrigger<EffectsChangedTrigger.TriggerInstance> {
	@Override
	public Codec<EffectsChangedTrigger.TriggerInstance> codec() {
		return EffectsChangedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, @Nullable Entity entity) {
		LootContext lootContext = entity != null ? EntityPredicate.createContext(serverPlayer, entity) : null;
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, lootContext));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<MobEffectsPredicate> effects, Optional<ContextAwarePredicate> source)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<EffectsChangedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(EffectsChangedTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(MobEffectsPredicate.CODEC, "effects").forGetter(EffectsChangedTrigger.TriggerInstance::effects),
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "source").forGetter(EffectsChangedTrigger.TriggerInstance::source)
					)
					.apply(instance, EffectsChangedTrigger.TriggerInstance::new)
		);

		public static Criterion<EffectsChangedTrigger.TriggerInstance> hasEffects(MobEffectsPredicate.Builder builder) {
			return CriteriaTriggers.EFFECTS_CHANGED.createCriterion(new EffectsChangedTrigger.TriggerInstance(Optional.empty(), builder.build(), Optional.empty()));
		}

		public static Criterion<EffectsChangedTrigger.TriggerInstance> gotEffectsFrom(EntityPredicate.Builder builder) {
			return CriteriaTriggers.EFFECTS_CHANGED
				.createCriterion(new EffectsChangedTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(EntityPredicate.wrap(builder.build()))));
		}

		public boolean matches(ServerPlayer serverPlayer, @Nullable LootContext lootContext) {
			return this.effects.isPresent() && !((MobEffectsPredicate)this.effects.get()).matches((LivingEntity)serverPlayer)
				? false
				: !this.source.isPresent() || lootContext != null && ((ContextAwarePredicate)this.source.get()).matches(lootContext);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.source, ".source");
		}
	}
}
