package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class FishingRodHookedTrigger extends SimpleCriterionTrigger<FishingRodHookedTrigger.TriggerInstance> {
	@Override
	public Codec<FishingRodHookedTrigger.TriggerInstance> codec() {
		return FishingRodHookedTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection) {
		LootContext lootContext = EntityPredicate.createContext(serverPlayer, (Entity)(fishingHook.getHookedIn() != null ? fishingHook.getHookedIn() : fishingHook));
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext, collection));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player, Optional<ItemPredicate> rod, Optional<ContextAwarePredicate> entity, Optional<ItemPredicate> item
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<FishingRodHookedTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(FishingRodHookedTrigger.TriggerInstance::player),
						ItemPredicate.CODEC.optionalFieldOf("rod").forGetter(FishingRodHookedTrigger.TriggerInstance::rod),
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("entity").forGetter(FishingRodHookedTrigger.TriggerInstance::entity),
						ItemPredicate.CODEC.optionalFieldOf("item").forGetter(FishingRodHookedTrigger.TriggerInstance::item)
					)
					.apply(instance, FishingRodHookedTrigger.TriggerInstance::new)
		);

		public static Criterion<FishingRodHookedTrigger.TriggerInstance> fishedItem(
			Optional<ItemPredicate> optional, Optional<EntityPredicate> optional2, Optional<ItemPredicate> optional3
		) {
			return CriteriaTriggers.FISHING_ROD_HOOKED
				.createCriterion(new FishingRodHookedTrigger.TriggerInstance(Optional.empty(), optional, EntityPredicate.wrap(optional2), optional3));
		}

		public boolean matches(ItemStack itemStack, LootContext lootContext, Collection<ItemStack> collection) {
			if (this.rod.isPresent() && !((ItemPredicate)this.rod.get()).test(itemStack)) {
				return false;
			} else if (this.entity.isPresent() && !((ContextAwarePredicate)this.entity.get()).matches(lootContext)) {
				return false;
			} else {
				if (this.item.isPresent()) {
					boolean bl = false;
					Entity entity = lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY);
					if (entity instanceof ItemEntity itemEntity && ((ItemPredicate)this.item.get()).test(itemEntity.getItem())) {
						bl = true;
					}

					for (ItemStack itemStack2 : collection) {
						if (((ItemPredicate)this.item.get()).test(itemStack2)) {
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
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntity(this.entity, ".entity");
		}
	}
}
