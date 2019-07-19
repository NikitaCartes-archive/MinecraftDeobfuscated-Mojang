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
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LocationTrigger
implements CriterionTrigger<TriggerInstance> {
    private final ResourceLocation id;
    private final Map<PlayerAdvancements, PlayerListeners> players = Maps.newHashMap();

    public LocationTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
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
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject);
        return new TriggerInstance(this.id, locationPredicate);
    }

    public void trigger(ServerPlayer serverPlayer) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(serverPlayer.getLevel(), serverPlayer.x, serverPlayer.y, serverPlayer.z);
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

        public void trigger(ServerLevel serverLevel, double d, double e, double f) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(serverLevel, d, e, f)) continue;
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
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation resourceLocation, LocationPredicate locationPredicate) {
            super(resourceLocation);
            this.location = locationPredicate;
        }

        public static TriggerInstance located(LocationPredicate locationPredicate) {
            return new TriggerInstance(CriteriaTriggers.LOCATION.id, locationPredicate);
        }

        public static TriggerInstance sleptInBed() {
            return new TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, LocationPredicate.ANY);
        }

        public static TriggerInstance raidWon() {
            return new TriggerInstance(CriteriaTriggers.RAID_WIN.id, LocationPredicate.ANY);
        }

        public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
            return this.location.matches(serverLevel, d, e, f);
        }

        @Override
        public JsonElement serializeToJson() {
            return this.location.serializeToJson();
        }
    }
}

