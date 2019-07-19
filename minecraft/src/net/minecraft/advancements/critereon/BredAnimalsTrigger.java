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
import javax.annotation.Nullable;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgableMob;
import net.minecraft.world.entity.animal.Animal;

public class BredAnimalsTrigger implements CriterionTrigger<BredAnimalsTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("bred_animals");
	private final Map<PlayerAdvancements, BredAnimalsTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, BredAnimalsTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> listener) {
		BredAnimalsTrigger.PlayerListeners playerListeners = (BredAnimalsTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new BredAnimalsTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> listener) {
		BredAnimalsTrigger.PlayerListeners playerListeners = (BredAnimalsTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public BredAnimalsTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("parent"));
		EntityPredicate entityPredicate2 = EntityPredicate.fromJson(jsonObject.get("partner"));
		EntityPredicate entityPredicate3 = EntityPredicate.fromJson(jsonObject.get("child"));
		return new BredAnimalsTrigger.TriggerInstance(entityPredicate, entityPredicate2, entityPredicate3);
	}

	public void trigger(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
		BredAnimalsTrigger.PlayerListeners playerListeners = (BredAnimalsTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(serverPlayer, animal, animal2, agableMob);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
			List<CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(serverPlayer, animal, animal2, agableMob)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<BredAnimalsTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final EntityPredicate parent;
		private final EntityPredicate partner;
		private final EntityPredicate child;

		public TriggerInstance(EntityPredicate entityPredicate, EntityPredicate entityPredicate2, EntityPredicate entityPredicate3) {
			super(BredAnimalsTrigger.ID);
			this.parent = entityPredicate;
			this.partner = entityPredicate2;
			this.child = entityPredicate3;
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals() {
			return new BredAnimalsTrigger.TriggerInstance(EntityPredicate.ANY, EntityPredicate.ANY, EntityPredicate.ANY);
		}

		public static BredAnimalsTrigger.TriggerInstance bredAnimals(EntityPredicate.Builder builder) {
			return new BredAnimalsTrigger.TriggerInstance(builder.build(), EntityPredicate.ANY, EntityPredicate.ANY);
		}

		public boolean matches(ServerPlayer serverPlayer, Animal animal, @Nullable Animal animal2, @Nullable AgableMob agableMob) {
			return !this.child.matches(serverPlayer, agableMob)
				? false
				: this.parent.matches(serverPlayer, animal) && this.partner.matches(serverPlayer, animal2)
					|| this.parent.matches(serverPlayer, animal2) && this.partner.matches(serverPlayer, animal);
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			jsonObject.add("parent", this.parent.serializeToJson());
			jsonObject.add("partner", this.partner.serializeToJson());
			jsonObject.add("child", this.child.serializeToJson());
			return jsonObject;
		}
	}
}
