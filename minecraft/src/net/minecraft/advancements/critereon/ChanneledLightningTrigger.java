package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;

public class ChanneledLightningTrigger extends SimpleCriterionTrigger<ChanneledLightningTrigger.TriggerInstance> {
	@Override
	public Codec<ChanneledLightningTrigger.TriggerInstance> codec() {
		return ChanneledLightningTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Collection<? extends Entity> collection) {
		List<LootContext> list = (List<LootContext>)collection.stream()
			.map(entity -> EntityPredicate.createContext(serverPlayer, entity))
			.collect(Collectors.toList());
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<ChanneledLightningTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(ChanneledLightningTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC.listOf(), "victims", List.of())
							.forGetter(ChanneledLightningTrigger.TriggerInstance::victims)
					)
					.apply(instance, ChanneledLightningTrigger.TriggerInstance::new)
		);

		public static Criterion<ChanneledLightningTrigger.TriggerInstance> channeledLightning(EntityPredicate.Builder... builders) {
			return CriteriaTriggers.CHANNELED_LIGHTNING.createCriterion(new ChanneledLightningTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(builders)));
		}

		public boolean matches(Collection<? extends LootContext> collection) {
			for (ContextAwarePredicate contextAwarePredicate : this.victims) {
				boolean bl = false;

				for (LootContext lootContext : collection) {
					if (contextAwarePredicate.matches(lootContext)) {
						bl = true;
						break;
					}
				}

				if (!bl) {
					return false;
				}
			}

			return true;
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntities(this.victims, ".victims");
		}
	}
}
