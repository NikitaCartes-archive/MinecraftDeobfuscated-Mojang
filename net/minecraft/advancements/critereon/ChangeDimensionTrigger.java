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
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

public class ChangeDimensionTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("changed_dimension");
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
        DimensionType dimensionType = jsonObject.has("from") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "from"))) : null;
        DimensionType dimensionType2 = jsonObject.has("to") ? DimensionType.getByName(new ResourceLocation(GsonHelper.getAsString(jsonObject, "to"))) : null;
        return new TriggerInstance(dimensionType, dimensionType2);
    }

    public void trigger(ServerPlayer serverPlayer, DimensionType dimensionType, DimensionType dimensionType2) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(dimensionType, dimensionType2);
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

        public void trigger(DimensionType dimensionType, DimensionType dimensionType2) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(dimensionType, dimensionType2)) continue;
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
        @Nullable
        private final DimensionType from;
        @Nullable
        private final DimensionType to;

        public TriggerInstance(@Nullable DimensionType dimensionType, @Nullable DimensionType dimensionType2) {
            super(ID);
            this.from = dimensionType;
            this.to = dimensionType2;
        }

        public static TriggerInstance changedDimensionTo(DimensionType dimensionType) {
            return new TriggerInstance(null, dimensionType);
        }

        public boolean matches(DimensionType dimensionType, DimensionType dimensionType2) {
            if (this.from != null && this.from != dimensionType) {
                return false;
            }
            return this.to == null || this.to == dimensionType2;
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

