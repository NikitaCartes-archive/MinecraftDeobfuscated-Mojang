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
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class UsedTotemTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("used_totem");
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
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        return new TriggerInstance(itemPredicate);
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(itemStack);
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

        public void trigger(ItemStack itemStack) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(itemStack)) continue;
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
        private final ItemPredicate item;

        public TriggerInstance(ItemPredicate itemPredicate) {
            super(ID);
            this.item = itemPredicate;
        }

        public static TriggerInstance usedTotem(ItemLike itemLike) {
            return new TriggerInstance(ItemPredicate.Builder.item().of(itemLike).build());
        }

        public boolean matches(ItemStack itemStack) {
            return this.item.matches(itemStack);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("item", this.item.serializeToJson());
            return jsonObject;
        }
    }
}

