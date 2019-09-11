/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class InventoryChangeTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("inventory_changed");
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
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "slots", new JsonObject());
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject2.get("occupied"));
        MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject2.get("full"));
        MinMaxBounds.Ints ints3 = MinMaxBounds.Ints.fromJson(jsonObject2.get("empty"));
        ItemPredicate[] itemPredicates = ItemPredicate.fromJsonArray(jsonObject.get("items"));
        return new TriggerInstance(ints, ints2, ints3, itemPredicates);
    }

    public void trigger(ServerPlayer serverPlayer, Inventory inventory) {
        PlayerListeners playerListeners = this.players.get(serverPlayer.getAdvancements());
        if (playerListeners != null) {
            playerListeners.trigger(inventory);
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

        public void trigger(Inventory inventory) {
            ArrayList<CriterionTrigger.Listener<TriggerInstance>> list = null;
            for (CriterionTrigger.Listener<TriggerInstance> listener : this.listeners) {
                if (!listener.getTriggerInstance().matches(inventory)) continue;
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
        private final MinMaxBounds.Ints slotsOccupied;
        private final MinMaxBounds.Ints slotsFull;
        private final MinMaxBounds.Ints slotsEmpty;
        private final ItemPredicate[] predicates;

        public TriggerInstance(MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2, MinMaxBounds.Ints ints3, ItemPredicate[] itemPredicates) {
            super(ID);
            this.slotsOccupied = ints;
            this.slotsFull = ints2;
            this.slotsEmpty = ints3;
            this.predicates = itemPredicates;
        }

        public static TriggerInstance hasItem(ItemPredicate ... itemPredicates) {
            return new TriggerInstance(MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, itemPredicates);
        }

        public static TriggerInstance hasItem(ItemLike ... itemLikes) {
            ItemPredicate[] itemPredicates = new ItemPredicate[itemLikes.length];
            for (int i = 0; i < itemLikes.length; ++i) {
                itemPredicates[i] = new ItemPredicate(null, itemLikes[i].asItem(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY);
            }
            return TriggerInstance.hasItem(itemPredicates);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            if (!(this.slotsOccupied.isAny() && this.slotsFull.isAny() && this.slotsEmpty.isAny())) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.add("occupied", this.slotsOccupied.serializeToJson());
                jsonObject2.add("full", this.slotsFull.serializeToJson());
                jsonObject2.add("empty", this.slotsEmpty.serializeToJson());
                jsonObject.add("slots", jsonObject2);
            }
            if (this.predicates.length > 0) {
                JsonArray jsonArray = new JsonArray();
                for (ItemPredicate itemPredicate : this.predicates) {
                    jsonArray.add(itemPredicate.serializeToJson());
                }
                jsonObject.add("items", jsonArray);
            }
            return jsonObject;
        }

        public boolean matches(Inventory inventory) {
            int i = 0;
            int j = 0;
            int k = 0;
            ArrayList<ItemPredicate> list = Lists.newArrayList(this.predicates);
            for (int l = 0; l < inventory.getContainerSize(); ++l) {
                ItemStack itemStack = inventory.getItem(l);
                if (itemStack.isEmpty()) {
                    ++j;
                    continue;
                }
                ++k;
                if (itemStack.getCount() >= itemStack.getMaxStackSize()) {
                    ++i;
                }
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    ItemPredicate itemPredicate = (ItemPredicate)iterator.next();
                    if (!itemPredicate.matches(itemStack)) continue;
                    iterator.remove();
                }
            }
            if (!this.slotsFull.matches(i)) {
                return false;
            }
            if (!this.slotsEmpty.matches(j)) {
                return false;
            }
            if (!this.slotsOccupied.matches(k)) {
                return false;
            }
            return list.isEmpty();
        }
    }
}

