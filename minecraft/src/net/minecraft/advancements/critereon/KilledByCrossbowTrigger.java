package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
	public KilledByCrossbowTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext
	) {
		List<ContextAwarePredicate> list = EntityPredicate.fromJsonArray(jsonObject, "victims", deserializationContext);
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("unique_entity_types"));
		return new KilledByCrossbowTrigger.TriggerInstance(optional, list, ints);
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

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final List<ContextAwarePredicate> victims;
		private final MinMaxBounds.Ints uniqueEntityTypes;

		public TriggerInstance(Optional<ContextAwarePredicate> optional, List<ContextAwarePredicate> list, MinMaxBounds.Ints ints) {
			super(optional);
			this.victims = list;
			this.uniqueEntityTypes = ints;
		}

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
		public JsonObject serializeToJson() {
			JsonObject jsonObject = super.serializeToJson();
			jsonObject.add("victims", ContextAwarePredicate.toJson(this.victims));
			jsonObject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
			return jsonObject;
		}
	}
}
