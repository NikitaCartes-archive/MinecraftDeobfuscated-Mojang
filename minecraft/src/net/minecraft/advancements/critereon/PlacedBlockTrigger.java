package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

public class PlacedBlockTrigger implements CriterionTrigger<PlacedBlockTrigger.TriggerInstance> {
	private static final ResourceLocation ID = new ResourceLocation("placed_block");
	private final Map<PlayerAdvancements, PlacedBlockTrigger.PlayerListeners> players = Maps.<PlayerAdvancements, PlacedBlockTrigger.PlayerListeners>newHashMap();

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance> listener) {
		PlacedBlockTrigger.PlayerListeners playerListeners = (PlacedBlockTrigger.PlayerListeners)this.players.get(playerAdvancements);
		if (playerListeners == null) {
			playerListeners = new PlacedBlockTrigger.PlayerListeners(playerAdvancements);
			this.players.put(playerAdvancements, playerListeners);
		}

		playerListeners.addListener(listener);
	}

	@Override
	public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance> listener) {
		PlacedBlockTrigger.PlayerListeners playerListeners = (PlacedBlockTrigger.PlayerListeners)this.players.get(playerAdvancements);
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

	public PlacedBlockTrigger.TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
		Block block = null;
		if (jsonObject.has("block")) {
			ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
			block = (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
		}

		Map<Property<?>, Object> map = null;
		if (jsonObject.has("state")) {
			if (block == null) {
				throw new JsonSyntaxException("Can't define block state without a specific block type");
			}

			StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();

			for (Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(jsonObject, "state").entrySet()) {
				Property<?> property = stateDefinition.getProperty((String)entry.getKey());
				if (property == null) {
					throw new JsonSyntaxException("Unknown block state property '" + (String)entry.getKey() + "' for block '" + Registry.BLOCK.getKey(block) + "'");
				}

				String string = GsonHelper.convertToString((JsonElement)entry.getValue(), (String)entry.getKey());
				Optional<?> optional = property.getValue(string);
				if (!optional.isPresent()) {
					throw new JsonSyntaxException(
						"Invalid block state value '" + string + "' for property '" + (String)entry.getKey() + "' on block '" + Registry.BLOCK.getKey(block) + "'"
					);
				}

				if (map == null) {
					map = Maps.<Property<?>, Object>newHashMap();
				}

				map.put(property, optional.get());
			}
		}

		LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("location"));
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		return new PlacedBlockTrigger.TriggerInstance(block, map, locationPredicate, itemPredicate);
	}

	public void trigger(ServerPlayer serverPlayer, BlockPos blockPos, ItemStack itemStack) {
		BlockState blockState = serverPlayer.level.getBlockState(blockPos);
		PlacedBlockTrigger.PlayerListeners playerListeners = (PlacedBlockTrigger.PlayerListeners)this.players.get(serverPlayer.getAdvancements());
		if (playerListeners != null) {
			playerListeners.trigger(blockState, blockPos, serverPlayer.getLevel(), itemStack);
		}
	}

	static class PlayerListeners {
		private final PlayerAdvancements player;
		private final Set<CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance>> listeners = Sets.<CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance>>newHashSet();

		public PlayerListeners(PlayerAdvancements playerAdvancements) {
			this.player = playerAdvancements;
		}

		public boolean isEmpty() {
			return this.listeners.isEmpty();
		}

		public void addListener(CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance> listener) {
			this.listeners.add(listener);
		}

		public void removeListener(CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance> listener) {
			this.listeners.remove(listener);
		}

		public void trigger(BlockState blockState, BlockPos blockPos, ServerLevel serverLevel, ItemStack itemStack) {
			List<CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance>> list = null;

			for (CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance> listener : this.listeners) {
				if (listener.getTriggerInstance().matches(blockState, blockPos, serverLevel, itemStack)) {
					if (list == null) {
						list = Lists.<CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance>>newArrayList();
					}

					list.add(listener);
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<PlacedBlockTrigger.TriggerInstance> listenerx : list) {
					listenerx.run(this.player);
				}
			}
		}
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		private final Block block;
		private final Map<Property<?>, Object> state;
		private final LocationPredicate location;
		private final ItemPredicate item;

		public TriggerInstance(@Nullable Block block, @Nullable Map<Property<?>, Object> map, LocationPredicate locationPredicate, ItemPredicate itemPredicate) {
			super(PlacedBlockTrigger.ID);
			this.block = block;
			this.state = map;
			this.location = locationPredicate;
			this.item = itemPredicate;
		}

		public static PlacedBlockTrigger.TriggerInstance placedBlock(Block block) {
			return new PlacedBlockTrigger.TriggerInstance(block, null, LocationPredicate.ANY, ItemPredicate.ANY);
		}

		public boolean matches(BlockState blockState, BlockPos blockPos, ServerLevel serverLevel, ItemStack itemStack) {
			if (this.block != null && blockState.getBlock() != this.block) {
				return false;
			} else {
				if (this.state != null) {
					for (Entry<Property<?>, Object> entry : this.state.entrySet()) {
						if (blockState.getValue((Property)entry.getKey()) != entry.getValue()) {
							return false;
						}
					}
				}

				return !this.location.matches(serverLevel, (float)blockPos.getX(), (float)blockPos.getY(), (float)blockPos.getZ()) ? false : this.item.matches(itemStack);
			}
		}

		@Override
		public JsonElement serializeToJson() {
			JsonObject jsonObject = new JsonObject();
			if (this.block != null) {
				jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
			}

			if (this.state != null) {
				JsonObject jsonObject2 = new JsonObject();

				for (Entry<Property<?>, Object> entry : this.state.entrySet()) {
					jsonObject2.addProperty(((Property)entry.getKey()).getName(), Util.getPropertyName((Property)entry.getKey(), entry.getValue()));
				}

				jsonObject.add("state", jsonObject2);
			}

			jsonObject.add("location", this.location.serializeToJson());
			jsonObject.add("item", this.item.serializeToJson());
			return jsonObject;
		}
	}
}
