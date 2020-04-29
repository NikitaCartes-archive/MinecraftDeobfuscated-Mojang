/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MobEffectsPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class EffectsChangedTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("effects_changed");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        MobEffectsPredicate mobEffectsPredicate = MobEffectsPredicate.fromJson(jsonObject.get("effects"));
        return new TriggerInstance(composite, mobEffectsPredicate);
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final MobEffectsPredicate effects;

        public TriggerInstance(EntityPredicate.Composite composite, MobEffectsPredicate mobEffectsPredicate) {
            super(ID, composite);
            this.effects = mobEffectsPredicate;
        }

        public static TriggerInstance hasEffects(MobEffectsPredicate mobEffectsPredicate) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, mobEffectsPredicate);
        }

        public boolean matches(ServerPlayer serverPlayer) {
            return this.effects.matches(serverPlayer);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("effects", this.effects.serializeToJson());
            return jsonObject;
        }
    }
}

