package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootContext;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
	static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public KilledByCrossbowTrigger.TriggerInstance createInstance(
		JsonObject jsonObject, ContextAwarePredicate contextAwarePredicate, DeserializationContext deserializationContext
	) {
		ContextAwarePredicate[] contextAwarePredicates = EntityPredicate.fromJsonArray(jsonObject, "victims", deserializationContext);
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("unique_entity_types"));
		return new KilledByCrossbowTrigger.TriggerInstance(contextAwarePredicate, contextAwarePredicates, ints);
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
		private final ContextAwarePredicate[] victims;
		private final MinMaxBounds.Ints uniqueEntityTypes;

		public TriggerInstance(ContextAwarePredicate contextAwarePredicate, ContextAwarePredicate[] contextAwarePredicates, MinMaxBounds.Ints ints) {
			super(KilledByCrossbowTrigger.ID, contextAwarePredicate);
			this.victims = contextAwarePredicates;
			this.uniqueEntityTypes = ints;
		}

		public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... builders) {
			ContextAwarePredicate[] contextAwarePredicates = new ContextAwarePredicate[builders.length];

			for (int i = 0; i < builders.length; i++) {
				EntityPredicate.Builder builder = builders[i];
				contextAwarePredicates[i] = EntityPredicate.wrap(builder.build());
			}

			return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, contextAwarePredicates, MinMaxBounds.Ints.ANY);
		}

		public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints ints) {
			ContextAwarePredicate[] contextAwarePredicates = new ContextAwarePredicate[0];
			return new KilledByCrossbowTrigger.TriggerInstance(ContextAwarePredicate.ANY, contextAwarePredicates, ints);
		}

		public boolean matches(Collection<LootContext> collection, int i) {
			if (this.victims.length > 0) {
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
		public JsonObject serializeToJson(SerializationContext serializationContext) {
			JsonObject jsonObject = super.serializeToJson(serializationContext);
			jsonObject.add("victims", ContextAwarePredicate.toJson(this.victims, serializationContext));
			jsonObject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
			return jsonObject;
		}
	}
}
