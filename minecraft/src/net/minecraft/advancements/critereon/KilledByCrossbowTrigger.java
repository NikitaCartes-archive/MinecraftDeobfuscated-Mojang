package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class KilledByCrossbowTrigger extends SimpleCriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("unique_entity_types"));
		return new KilledByCrossbowTrigger.TriggerInstance(entityPredicates, ints);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
		this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, collection, i));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate[] victims;
		private final MinMaxBounds.Ints uniqueEntityTypes;

		public TriggerInstance(EntityPredicate[] entityPredicates, MinMaxBounds.Ints ints) {
			super(KilledByCrossbowTrigger.ID);
			this.victims = entityPredicates;
			this.uniqueEntityTypes = ints;
		}

		public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(EntityPredicate.Builder... builders) {
			EntityPredicate[] entityPredicates = new EntityPredicate[builders.length];

			for (int i = 0; i < builders.length; i++) {
				EntityPredicate.Builder builder = builders[i];
				entityPredicates[i] = builder.build();
			}

			return new KilledByCrossbowTrigger.TriggerInstance(entityPredicates, MinMaxBounds.Ints.ANY);
		}

		public static KilledByCrossbowTrigger.TriggerInstance crossbowKilled(MinMaxBounds.Ints ints) {
			EntityPredicate[] entityPredicates = new EntityPredicate[0];
			return new KilledByCrossbowTrigger.TriggerInstance(entityPredicates, ints);
		}

		public boolean matches(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
			if (this.victims.length > 0) {
				List<Entity> list = Lists.<Entity>newArrayList(collection);

				for (EntityPredicate entityPredicate : this.victims) {
					boolean bl = false;
					Iterator<Entity> iterator = list.iterator();

					while (iterator.hasNext()) {
						Entity entity = (Entity)iterator.next();
						if (entityPredicate.matches(serverPlayer, entity)) {
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

			if (this.uniqueEntityTypes == MinMaxBounds.Ints.ANY) {
				return true;
			} else {
				Set<EntityType<?>> set = Sets.<EntityType<?>>newHashSet();

				for (Entity entity2 : collection) {
					set.add(entity2.getType());
				}

				return this.uniqueEntityTypes.matches(set.size()) && this.uniqueEntityTypes.matches(i);
			}
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("victims", EntityPredicate.serializeArrayToJson(this.victims));
			jsonObject.add("unique_entity_types", this.uniqueEntityTypes.serializeToJson());
			return jsonObject;
		}
	}
}
