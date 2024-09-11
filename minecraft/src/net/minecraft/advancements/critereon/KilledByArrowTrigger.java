package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByArrowTrigger extends SimpleCriterionTrigger<KilledByArrowTrigger.TriggerInstance> {
	@Override
	public Codec<KilledByArrowTrigger.TriggerInstance> codec() {
		return KilledByArrowTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, @Nullable ItemStack itemStack) {
		List<LootContext> list = Lists.<LootContext>newArrayList();
		Set<EntityType<?>> set = Sets.<EntityType<?>>newHashSet();

		for (Entity entity : collection) {
			set.add(entity.getType());
			list.add(EntityPredicate.createContext(serverPlayer, entity));
		}

		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list, set.size(), itemStack));
	}

	public static record TriggerInstance(
		Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes, Optional<ItemPredicate> firedFromWeapon
	) implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<KilledByArrowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(KilledByArrowTrigger.TriggerInstance::player),
						EntityPredicate.ADVANCEMENT_CODEC.listOf().optionalFieldOf("victims", List.of()).forGetter(KilledByArrowTrigger.TriggerInstance::victims),
						MinMaxBounds.Ints.CODEC.optionalFieldOf("unique_entity_types", MinMaxBounds.Ints.ANY).forGetter(KilledByArrowTrigger.TriggerInstance::uniqueEntityTypes),
						ItemPredicate.CODEC.optionalFieldOf("fired_from_weapon").forGetter(KilledByArrowTrigger.TriggerInstance::firedFromWeapon)
					)
					.apply(instance, KilledByArrowTrigger.TriggerInstance::new)
		);

		public static Criterion<KilledByArrowTrigger.TriggerInstance> crossbowKilled(HolderGetter<Item> holderGetter, EntityPredicate.Builder... builders) {
			return CriteriaTriggers.KILLED_BY_ARROW
				.createCriterion(
					new KilledByArrowTrigger.TriggerInstance(
						Optional.empty(),
						EntityPredicate.wrap(builders),
						MinMaxBounds.Ints.ANY,
						Optional.of(ItemPredicate.Builder.item().of(holderGetter, Items.CROSSBOW).build())
					)
				);
		}

		public static Criterion<KilledByArrowTrigger.TriggerInstance> crossbowKilled(HolderGetter<Item> holderGetter, MinMaxBounds.Ints ints) {
			return CriteriaTriggers.KILLED_BY_ARROW
				.createCriterion(
					new KilledByArrowTrigger.TriggerInstance(
						Optional.empty(), List.of(), ints, Optional.of(ItemPredicate.Builder.item().of(holderGetter, Items.CROSSBOW).build())
					)
				);
		}

		public boolean matches(Collection<LootContext> collection, int i, @Nullable ItemStack itemStack) {
			if (!this.firedFromWeapon.isPresent() || itemStack != null && ((ItemPredicate)this.firedFromWeapon.get()).test(itemStack)) {
				if (!this.victims.isEmpty()) {
					List<LootContext> list = Lists.<LootContext>newArrayList(collection);

					for (ContextAwarePredicate contextAwarePredicate : this.victims) {
						boolean bl = false;
						Iterator<LootContext> iterator = list.iterator();

						while (iterator.hasNext()) {
							LootContext lootContext = (LootContext)iterator.next();
							if (contextAwarePredicate.matches(lootContext)) {
								iterator.remove();
								bl = true;
								break;
							}
						}

						if (!bl) {
							return false;
						}
					}
				}

				return this.uniqueEntityTypes.matches(i);
			} else {
				return false;
			}
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntities(this.victims, ".victims");
		}
	}
}
