package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class KilledByCrossbowTrigger implements CriterionTrigger<KilledByCrossbowTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");
	private final Map<PlayerAdvancements, KilledByCrossbowTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, KilledByCrossbowTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance> listener) {
		KilledByCrossbowTrigger.PlayerListeners playerListeners = (KilledByCrossbowTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new KilledByCrossbowTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance> listener) {
		KilledByCrossbowTrigger.PlayerListeners playerListeners = (KilledByCrossbowTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners != null) {
			playerListeners.removeListener(listener);
			if (playerListeners.isEmpty()) {
				this.players.remove(playerAdvancements);
			}
		}
	}

	@Override
	public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
		this.players.remove(playerAdvancements);
	}

	public KilledByCrossbowTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
		MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("unique_entity_types"));
		return new KilledByCrossbowTrigger.TriggerInstance(entityPredicates, ints);
	}

	public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
		KilledByCrossbowTrigger.PlayerListeners playerListeners = (KilledByCrossbowTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, collection, i);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
			List<CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, collection, i)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<KilledByCrossbowTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
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
