/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class SummonedEntityTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("summoned_entity");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        EntityPredicate entityPredicate = EntityPredicate.fromJson(jsonObject.get("entity"));
        return new TriggerInstance(entityPredicate);
    }

    public void trigger(ServerPlayer serverPlayer, Entity entity) {
        this.trigger(serverPlayer.getAdvancements(), (T triggerInstance) -> triggerInstance.matches(serverPlayer, entity));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate entity;

        public TriggerInstance(EntityPredicate entityPredicate) {
            super(ID);
            this.entity = entityPredicate;
        }

        public static TriggerInstance summonedEntity(EntityPredicate.Builder builder) {
            return new TriggerInstance(builder.build());
        }

        public boolean matches(ServerPlayer serverPlayer, Entity entity) {
            return this.entity.matches(serverPlayer, entity);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("entity", this.entity.serializeToJson());
            return jsonObject;
        }
    }
}

