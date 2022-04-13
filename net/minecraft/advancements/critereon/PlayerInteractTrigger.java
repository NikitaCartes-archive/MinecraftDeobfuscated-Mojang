/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public class PlayerInteractTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("player_interacted_with_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
        EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "entity", deserializationContext);
        return new TriggerInstance(composite, itemPredicate, composite2);
    }

    public void trigger(ServerPlayer serverPlayer, ItemStack itemStack, Entity entity) {
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, entity);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack, lootContext));
    }

    @Override
    protected /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;
        private final EntityPredicate.Composite entity;

        public TriggerInstance(EntityPredicate.Composite composite, ItemPredicate itemPredicate, EntityPredicate.Composite composite2) {
            super(ID, composite);
            this.item = itemPredicate;
            this.entity = composite2;
        }

        public static TriggerInstance itemUsedOnEntity(EntityPredicate.Composite composite, ItemPredicate.Builder builder, EntityPredicate.Composite composite2) {
            return new TriggerInstance(composite, builder.build(), composite2);
        }

        public static TriggerInstance itemUsedOnEntity(ItemPredicate.Builder builder, EntityPredicate.Composite composite) {
            return TriggerInstance.itemUsedOnEntity(EntityPredicate.Composite.ANY, builder, composite);
        }

        public boolean matches(ItemStack itemStack, LootContext lootContext) {
            if (!this.item.matches(itemStack)) {
                return false;
            }
            return this.entity.matches(lootContext);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("item", this.item.serializeToJson());
            jsonObject.add("entity", this.entity.toJson(serializationContext));
            return jsonObject;
        }
    }
}

