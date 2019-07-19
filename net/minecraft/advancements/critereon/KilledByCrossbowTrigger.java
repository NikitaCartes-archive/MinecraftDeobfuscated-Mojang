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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class KilledByCrossbowTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("killed_by_crossbow");
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
        EntityPredicate[] entityPredicates = EntityPredicate.fromJsonArray(jsonObject.get("victims"));
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("unique_entity_types"));
        return new TriggerInstance(entityPredicates, ints);
    }

    public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(serverPlayer, collection, i);
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

        public void trigger(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(serverPlayer, collection, i)) continue;
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
        private final EntityPredicate[] victims;
        private final MinMaxBounds.Ints uniqueEntityTypes;

        public TriggerInstance(EntityPredicate[] entityPredicates, MinMaxBounds.Ints ints) {
            super(ID);
            this.victims = entityPredicates;
            this.uniqueEntityTypes = ints;
        }

        public static TriggerInstance crossbowKilled(EntityPredicate.Builder ... builders) {
            EntityPredicate[] entityPredicates = new EntityPredicate[builders.length];
            for (int i = 0; i < builders.length; ++i) {
                EntityPredicate.Builder builder = builders[i];
                entityPredicates[i] = builder.build();
            }
            return new TriggerInstance(entityPredicates, MinMaxBounds.Ints.ANY);
        }

        public static TriggerInstance crossbowKilled(MinMaxBounds.Ints ints) {
            EntityPredicate[] entityPredicates = new EntityPredicate[]{};
            return new TriggerInstance(entityPredicates, ints);
        }

        public boolean matches(ServerPlayer serverPlayer, Collection<Entity> collection, int i) {
            if (this.victims.length > 0) {
                ArrayList<Entity> list = Lists.newArrayList(collection);
                for (EntityPredicate entityPredicate : this.victims) {
                    boolean bl = false;
                    Iterator iterator = list.iterator();
                    while (iterator.hasNext()) {
                        Entity entity = (Entity)iterator.next();
                        if (!entityPredicate.matches(serverPlayer, entity)) continue;
                        iterator.remove();
                        bl = true;
                        break;
                    }
                    if (bl) continue;
                    return false;
                }
            }
            if (this.uniqueEntityTypes != MinMaxBounds.Ints.ANY) {
                HashSet<EntityType<?>> set = Sets.newHashSet();
                for (Entity entity2 : collection) {
                    set.add(entity2.getType());
                }
                return this.uniqueEntityTypes.matches(set.size()) && this.uniqueEntityTypes.matches(i);
            }
            return true;
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

