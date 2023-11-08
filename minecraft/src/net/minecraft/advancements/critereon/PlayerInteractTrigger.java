package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger extends SimpleCriterionTrigger<PlayerInteractTrigger.TriggerInstance> {
	@Override
	public Codec<PlayerInteractTrigger.TriggerInstance> codec() {
		return PlayerInteractTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, Entity entity) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, Optional<ItemPredicate> item, Optional<ContextAwarePredicate> entity)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<PlayerInteractTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PlayerInteractTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(ItemPredicate.CODEC, "item").forGetter(PlayerInteractTrigger.TriggerInstance::item),
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "entity").forGetter(PlayerInteractTrigger.TriggerInstance::entity)
					)
					.apply(instance, PlayerInteractTrigger.TriggerInstance::new)
		);

		public static Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(
			Optional<ContextAwarePredicate> optional, ItemPredicate.Builder builder, Optional<ContextAwarePredicate> optional2
		) {
			return CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY
				.createCriterion(new PlayerInteractTrigger.TriggerInstance(optional, Optional.of(builder.build()), optional2));
		}

		public static Criterion<PlayerInteractTrigger.TriggerInstance> itemUsedOnEntity(ItemPredicate.Builder builder, Optional<ContextAwarePredicate> optional) {
			return itemUsedOnEntity(Optional.empty(), builder, optional);
		}

		public boolean matches(ItemStack itemStack, LootContext lootContext) {
			return this.item.isPresent() && !((ItemPredicate)this.item.get()).matches(itemStack)
				? false
				: this.entity.isEmpty() || ((ContextAwarePredicate)this.entity.get()).matches(lootContext);
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.entity, ".entity");
		}
	}
}
