/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ConsumeItemTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("consume_item");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return new TriggerInstance(composite, ItemPredicate.fromJson(jsonObject.get("item")));
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(itemStack));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate) {
            super(ID, composite);
            this.item = itemPredicate;
        }

        public static TriggerInstance usedItem() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, ItemPredicate.ANY);
        }

        public static TriggerInstance usedItem(ItemLike itemLike) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, new ItemPredicate(null, itemLike.asItem(), MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, NbtPredicate.ANY));
        }

        public boolean matches(ItemStack itemStack) {
            return this.item.matches(itemStack);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("item", this.item.serializeToJson());
            return jsonObject;
        }
    }
}

