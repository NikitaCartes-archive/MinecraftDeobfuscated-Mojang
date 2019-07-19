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
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class NetherTravelTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("nether_travel");
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
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("entered"));
        LocationPredicate locationPredicate2 = LocationPredicate.fromJson(jsonObject.get("exited"));
        DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
        return new TriggerInstance(locationPredicate, locationPredicate2, distancePredicate);
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(serverPlayer.getLevel(), vec3, serverPlayer.x, serverPlayer.y, serverPlayer.z);
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

        public void trigger(ServerLevel serverLevel, Vec3 vec3, double d, double e, double f) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(serverLevel, vec3, d, e, f)) continue;
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
        private final LocationPredicate entered;
        private final LocationPredicate exited;
        private final DistancePredicate distance;

        public TriggerInstance(LocationPredicate locationPredicate, LocationPredicate locationPredicate2, DistancePredicate distancePredicate) {
            super(ID);
            this.entered = locationPredicate;
            this.exited = locationPredicate2;
            this.distance = distancePredicate;
        }

        public static TriggerInstance travelledThroughNether(DistancePredicate distancePredicate) {
            return new TriggerInstance(LocationPredicate.ANY, LocationPredicate.ANY, distancePredicate);
        }

        public boolean matches(ServerLevel serverLevel, Vec3 vec3, double d, double e, double f) {
            if (!this.entered.matches(serverLevel, vec3.x, vec3.y, vec3.z)) {
                return false;
            }
            if (!this.exited.matches(serverLevel, d, e, f)) {
                return false;
            }
            return this.distance.matches(vec3.x, vec3.y, vec3.z, d, e, f);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("entered", this.entered.serializeToJson());
            jsonObject.add("exited", this.exited.serializeToJson());
            jsonObject.add("distance", this.distance.serializeToJson());
            return jsonObject;
        }
    }
}

