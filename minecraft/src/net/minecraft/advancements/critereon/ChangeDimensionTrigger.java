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
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;

public class ChangeDimensionTrigger implements CriterionTrigger<ChangeDimensionTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("changed_dimension");
	private final Map<PlayerAdvancements, ChangeDimensionTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, ChangeDimensionTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> listener) {
		ChangeDimensionTrigger.PlayerListeners playerListeners = (ChangeDimensionTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new ChangeDimensionTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> listener) {
		ChangeDimensionTrigger.PlayerListeners playerListeners = (ChangeDimensionTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public ChangeDimensionTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		DimensionType dimensionType = jsonObject.has("from") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "from"))) : null;
		DimensionType dimensionType2 = jsonObject.has("to") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "to"))) : null;
		return new ChangeDimensionTrigger.TriggerInstance(dimensionType, dimensionType2);
	}

	public void trigger(ServerPlayer serverPlayer, DimensionType dimensionType, DimensionType dimensionType2) {
		ChangeDimensionTrigger.PlayerListeners playerListeners = (ChangeDimensionTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(dimensionType, dimensionType2);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(DimensionType dimensionType, DimensionType dimensionType2) {
			List<CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(dimensionType, dimensionType2)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<ChangeDimensionTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final DimensionType from;
		@Nullable
		private final DimensionType to;

		public TriggerInstance(@Nullable DimensionType dimensionType, @Nullable DimensionType dimensionType2) {
			super(ChangeDimensionTrigger.ID);
			this.from = dimensionType;
			this.to = dimensionType2;
		}

		public static ChangeDimensionTrigger.TriggerInstance changedDimensionTo(DimensionType dimensionType) {
			return new ChangeDimensionTrigger.TriggerInstance(null, dimensionType);
		}

		public boolean matches(DimensionType dimensionType, DimensionType dimensionType2) {
			return this.from != null && this.from != dimensionType ? false : this.to == null || this.to == dimensionType2;
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.from != null) {
				jsonObject.addProperty("from", DimensionType.getName(this.from).toString());
			}

			if (this.to != null) {
				jsonObject.addProperty("to", DimensionType.getName(this.to).toString());
			}

			return jsonObject;
		}
	}
}
