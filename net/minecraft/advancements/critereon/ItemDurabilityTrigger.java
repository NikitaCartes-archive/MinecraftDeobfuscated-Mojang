/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class ItemDurabilityTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("item_durability_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("durability"));
        MinMaxBounds.Ints ints2 = MinMaxBounds.Ints.fromJson(jsonObject.get("delta"));
        return new TriggerInstance(composite, itemPredicate, ints, ints2);
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, int i) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, i));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final MinMaxBounds.Ints durability;
        private final MinMaxBounds.Ints delta;

        public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate, MinMaxBounds.Ints ints, MinMaxBounds.Ints ints2) {
            super(ID, composite);
            this.item = itemPredicate;
            this.durability = ints;
            this.delta = ints2;
        }

        public static TriggerInstance changedDurability(EntityPredicate.Composite composite, ItemPredicate itemPredicate, MinMaxBounds.Ints ints) {
            return new TriggerInstance(composite, itemPredicate, ints, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ItemStack itemStack, int i) {
            if (!this.item.matches(itemStack)) {
                return false;
            }
            if (!this.durability.matches(itemStack.getMaxDamage() - i)) {
                return false;
            }
            return this.delta.matches(itemStack.getDamageValue() - i);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("item", this.item.serializeToJson());
            jsonObject.add("durability", this.durability.serializeToJson());
            jsonObject.add("delta", this.delta.serializeToJson());
            return jsonObject;
        }
    }
}

