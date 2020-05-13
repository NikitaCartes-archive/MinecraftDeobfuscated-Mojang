/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class LootTableTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("player_generates_container_loot");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "loot_table"));
        return new TriggerInstance(composite, resourceLocation);
    }

    public void trigger(ServerPlayer serverPlayer, ResourceLocation resourceLocation) {
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(resourceLocation));
    }

    @Override
    protected /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final ResourceLocation lootTable;

        public TriggerInstance(EntityPredicate.Composite composite, ResourceLocation resourceLocation) {
            super(ID, composite);
            this.lootTable = resourceLocation;
        }

        public static TriggerInstance lootTableUsed(ResourceLocation resourceLocation) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, resourceLocation);
        }

        public boolean matches(ResourceLocation resourceLocation) {
            return this.lootTable.equals(resourceLocation);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.addProperty("loot_table", this.lootTable.toString());
            return jsonObject;
        }
    }
}

