/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

public class EnterBlockTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("enter_block");
    private final Map<PlayerAdvancements, PlayerListeners> players = Maps.newHashMap();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TriggerInstance> listener) {
        PlayerListeners playerListeners = this.players.get(playerAdvancements);
        if (playerListeners == null) {
            playerListeners = new PlayerListeners(playerAdvancements);
            this.players.put(playerAdvancements, playerListeners);
        }
        playerListeners.addListener(listener);
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TriggerInstance> listener) {
        PlayerListeners playerListeners = this.players.get(playerAdvancements);
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

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        Block block = null;
        if (jsonObject.has("block")) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            block = (Block)Registry.BLOCK.getOptional(resourceLocation).orElseThrow(() -> new JsonSyntaxException("Unknown block type '" + resourceLocation + "'"));
        }
        HashMap<Property<?>, ?> map = null;
        if (jsonObject.has("state")) {
            if (block == null) {
                throw new JsonSyntaxException("Can't define block state without a specific block type");
            }
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (Map.Entry<String, JsonElement> entry : GsonHelper.getAsJsonObject(jsonObject, "state").entrySet()) {
                Property<?> property = stateDefinition.getProperty(entry.getKey());
                if (property == null) {
                    throw new JsonSyntaxException("Unknown block state property '" + entry.getKey() + "' for block '" + Registry.BLOCK.getKey(block) + "'");
                }
                String string = GsonHelper.convertToString(entry.getValue(), entry.getKey());
                Optional<?> optional = property.getValue(string);
                if (optional.isPresent()) {
                    if (map == null) {
                        map = Maps.newHashMap();
                    }
                    map.put(property, optional.get());
                    continue;
                }
                throw new JsonSyntaxException("Invalid block state value '" + string + "' for property '" + entry.getKey() + "' on block '" + Registry.BLOCK.getKey(block) + "'");
            }
        }
        return new TriggerInstance(block, map);
    }

    public void trigger(ServerPlayer serverPlayer, BlockState blockState) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(blockState);
        }
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    static class PlayerListeners {
        private final PlayerAdvancements player;
        private final Set<CriterionTrigger.Listener<TriggerInstance>> listeners = Sets.newHashSet();

        public PlayerListeners(PlayerAdvancements playerAdvancements) {
            this.player = playerAdvancements;
        }

        public boolean isEmpty() {
            return this.listeners.isEmpty();
        }

        public void addListener(CriterionTrigger.Listener<TriggerInstance> listener) {
            this.listeners.add(listener);
        }

        public void removeListener(CriterionTrigger.Listener<TriggerInstance> listener) {
            this.listeners.remove(listener);
        }

        public void trigger(BlockState blockState) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(blockState)) continue;
                if (list == null) {
                    list = Lists.newArrayList();
                }
                list.add(listener);
            }
            if (list != null) {
                for (CriterionTrigger.Listener<TriggerInstance> listener : list) {
                    listener.run(this.player);
                }
            }
        }
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final Block block;
        private final Map<Property<?>, Object> state;

        public TriggerInstance(@Nullable Block block, @Nullable Map<Property<?>, Object> map) {
            super(ID);
            this.block = block;
            this.state = map;
        }

        public static TriggerInstance entersBlock(Block block) {
            return new TriggerInstance(block, null);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            if (this.block != null) {
                jsonObject.addProperty("block", Registry.BLOCK.getKey(this.block).toString());
                if (this.state != null && !this.state.isEmpty()) {
                    JsonObject jsonObject2 = new JsonObject();
                    for (Map.Entry<Property<?>, Object> entry : this.state.entrySet()) {
                        jsonObject2.addProperty(entry.getKey().getName(), Util.getPropertyName(entry.getKey(), entry.getValue()));
                    }
                    jsonObject.add("state", jsonObject2);
                }
            }
            return jsonObject;
        }

        public boolean matches(BlockState blockState) {
            if (this.block != null && blockState.getBlock() != this.block) {
                return false;
            }
            if (this.state != null) {
                for (Map.Entry<Property<?>, Object> entry : this.state.entrySet()) {
                    if (blockState.getValue(entry.getKey()) == entry.getValue()) continue;
                    return false;
                }
            }
            return true;
        }
    }
}

