package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;

public class CuredZombieVillagerTrigger implements CriterionTrigger<CuredZombieVillagerTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("cured_zombie_villager");
	private final Map<PlayerAdvancements, CuredZombieVillagerTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, CuredZombieVillagerTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> listener) {
		CuredZombieVillagerTrigger.PlayerListeners playerListeners = (CuredZombieVillagerTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new CuredZombieVillagerTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> listener) {
		CuredZombieVillagerTrigger.PlayerListeners playerListeners = (CuredZombieVillagerTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public CuredZombieVillagerTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("zombie"));
		EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("villager"));
		return new CuredZombieVillagerTrigger.TriggerInstance(entityPredicate, entityPredicate2);
	}

	public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
		CuredZombieVillagerTrigger.PlayerListeners playerListeners = (CuredZombieVillagerTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, zombie, villager);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
			List<CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, zombie, villager)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<CuredZombieVillagerTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate zombie;
		private final EntityPredicate villager;

		public TriggerInstance(EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
			super(CuredZombieVillagerTrigger.ID);
			this.zombie = entityPredicate;
			this.villager = entityPredicate2;
		}

		public static CuredZombieVillagerTrigger.TriggerInstance curedZombieVillager() {
			return new CuredZombieVillagerTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Zombie zombie, Villager villager) {
			return !this.zombie.matches(serverPlayer, zombie) ? false : this.villager.matches(serverPlayer, villager);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("zombie", this.zombie.serializeToJson());
			jsonObject.add("villager", this.villager.serializeToJson());
			return jsonObject;
		}
	}
}
