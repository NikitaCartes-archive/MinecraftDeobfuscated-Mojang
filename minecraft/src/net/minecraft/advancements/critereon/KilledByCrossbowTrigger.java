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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
	@Override
	public Codec<KilledByCrossbowTrigger.TriggerInstance> codec() {
		return KilledByCrossbowTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection) {
		List<LootContext> list = Lists.<LootContext>newArrayList();
		Set<EntityType<?>> set = Sets.<EntityType<?>>newHashSet();

		for (Entity entity : collection) {
			set.add(entity.getType());
			list.add(EntityPredicate.createContext(serverPlayer, entity));
		}

		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(list, set.size()));
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, List<ContextAwarePredicate> victims, MinMaxBounds.Ints uniqueEntityTypes)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<KilledByCrossbowTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(KilledByCrossbowTrigger.TriggerInstance::player),
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC.listOf(), "victims", List.of())
							.forGetter(KilledByCrossbowTrigger.TriggerInstance::victims),
						ExtraCodecs.strictOptionalField(MinMaxBounds.Ints.CODEC, "unique_entity_types", MinMaxBounds.Ints.ANY)
							.forGetter(KilledByCrossbowTrigger.TriggerInstance::uniqueEntityTypes)
					)
					.apply(instance, KilledByCrossbowTrigger.TriggerInstance::new)
		);

		public static Criterion<KilledByCrossbowTrigger.TriggerInstance> crossbowKilled(EntityPredicate.Builder... builders) {
			return CriteriaTriggers.KILLED_BY_CROSSBOW
				.createCriterion(new KilledByCrossbowTrigger.TriggerInstance(Optional.empty(), EntityPredicate.wrap(builders), MinMaxBounds.Ints.ANY));
		}

		public static Criterion<KilledByCrossbowTrigger.TriggerInstance> crossbowKilled(MinMaxBounds.Ints ints) {
			return CriteriaTriggers.KILLED_BY_CROSSBOW.createCriterion(new KilledByCrossbowTrigger.TriggerInstance(Optional.empty(), List.of(), ints));
		}

		public boolean matches(Collection<LootContext> collection, int i) {
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
		}

		@Override
		public void validate(CriterionValidator criterionValidator) {
			SimpleCriterionTrigger.SimpleInstance.super.validate(criterionValidator);
			criterionValidator.validateEntities(this.victims, ".victims");
		}
	}
}
