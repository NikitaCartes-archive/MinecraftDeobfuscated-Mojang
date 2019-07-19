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
import net.minecraft.advancements.critereon.DamageSourcePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class KilledTrigger
implements CriterionTrigger<TriggerInstance> {
    private final Map<PlayerAdvancements, PlayerListeners> players = Maps.newHashMap();
    private final ResourceLocation id;

    public KilledTrigger(ResourceLocation resourceLocation) {
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
        return new TriggerInstance(this.id, EntityPredicate.fromJson(jsonObject.get("entity")), DamageSourcePredicate.fromJson(jsonObject.get("killing_blow")));
    }

    public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(serverPlayer, entity, damageSource);
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

        public void trigger(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(serverPlayer, entity, damageSource)) continue;
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
        private final EntityPredicate entityPredicate;
        private final DamageSourcePredicate killingBlow;

        public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate entityPredicate, DamageSourcePredicate damageSourcePredicate) {
            super(resourceLocation);
            this.entityPredicate = entityPredicate;
            this.killingBlow = damageSourcePredicate;
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate.Builder builder) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, builder.build(), DamageSourcePredicate.ANY);
        }

        public static TriggerInstance playerKilledEntity() {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
        }

        public static TriggerInstance playerKilledEntity(EntityPredicate.Builder builder, DamageSourcePredicate.Builder builder2) {
            return new TriggerInstance(CriteriaTriggers.PLAYER_KILLED_ENTITY.id, builder.build(), builder2.build());
        }

        public static TriggerInstance entityKilledPlayer() {
            return new TriggerInstance(CriteriaTriggers.ENTITY_KILLED_PLAYER.id, EntityPredicate.ANY, DamageSourcePredicate.ANY);
        }

        public boolean matches(ServerPlayer serverPlayer, Entity entity, DamageSource damageSource) {
            if (!this.killingBlow.matches(serverPlayer, damageSource)) {
                return false;
            }
            return this.entityPredicate.matches(serverPlayer, entity);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("entity", this.entityPredicate.serializeToJson());
            jsonObject.add("killing_blow", this.killingBlow.serializeToJson());
            return jsonObject;
        }
    }
}

