/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.storage.loot.LootContext;

public class LightningStrikeTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("lightning_strike");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        EntityPredicate.Composite composite2 = EntityPredicate.Composite.fromJson(jsonObject, "lightning", deserializationContext);
        EntityPredicate.Composite composite3 = EntityPredicate.Composite.fromJson(jsonObject, "bystander", deserializationContext);
        return new TriggerInstance(composite, composite2, composite3);
    }

    public void trigger(ServerPlayer serverPlayer, LightningBolt lightningBolt, List<Entity> list) {
        List list2 = list.stream().map(entity -> EntityPredicate.createContext(serverPlayer, entity)).collect(Collectors.toList());
        LootContext lootContext = EntityPredicate.createContext(serverPlayer, lightningBolt);
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(lootContext, list2));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final EntityPredicate.Composite lightning;
        private final EntityPredicate.Composite bystander;

        public TriggerInstance(EntityPredicate.Composite composite, EntityPredicate.Composite composite2, EntityPredicate.Composite composite3) {
            super(ID, composite);
            this.lightning = composite2;
            this.bystander = composite3;
        }

        public static TriggerInstance lighthingStrike(EntityPredicate entityPredicate, EntityPredicate entityPredicate2) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, EntityPredicate.Composite.wrap(entityPredicate), EntityPredicate.Composite.wrap(entityPredicate2));
        }

        public boolean matches(LootContext lootContext, List<LootContext> list) {
            if (!this.lightning.matches(lootContext)) {
                return false;
            }
            if (this.bystander != EntityPredicate.Composite.ANY) {
                if (list.stream().noneMatch(this.bystander::matches)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("lightning", this.lightning.toJson(serializationContext));
            jsonObject.add("bystander", this.bystander.toJson(serializationContext));
            return jsonObject;
        }
    }
}

