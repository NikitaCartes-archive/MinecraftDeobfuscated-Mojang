package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.alchemy.Potion;

public class BrewedPotionTrigger extends SimpleCriterionTrigger<BrewedPotionTrigger.TriggerInstance> {
	@Override
	public Codec<BrewedPotionTrigger.TriggerInstance> codec() {
		return BrewedPotionTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Holder<Potion> holder) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(holder));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Potion>> potion) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<BrewedPotionTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(BrewedPotionTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(BuiltInRegistries.POTION.holderByNameCodec(), "potion").forGetter(BrewedPotionTrigger.TriggerInstance::potion)
					)
					.apply(instance, BrewedPotionTrigger.TriggerInstance::new)
		);

		public static Criterion<BrewedPotionTrigger.TriggerInstance> brewedPotion() {
			return CriteriaTriggers.BREWED_POTION.createCriterion(new BrewedPotionTrigger.TriggerInstance(Optional.empty(), Optional.empty()));
		}

		public boolean matches(Holder<Potion> holder) {
			return !this.potion.isPresent() || ((Holder)this.potion.get()).equals(holder);
		}
	}
}
