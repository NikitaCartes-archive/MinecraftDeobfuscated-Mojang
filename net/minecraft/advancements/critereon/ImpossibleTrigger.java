/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;

public class ImpossibleTrigger
implements CriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("impossible");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TriggerInstance> listener) {
    }

    @Override
    public void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<TriggerInstance> listener) {
    }

    @Override
    public void removePlayerListeners(PlayerAdvancements playerAdvancements) {
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, DeserializationContext deserializationContext) {
        return new TriggerInstance();
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, deserializationContext);
    }

    public static class TriggerInstance
    implements CriterionTriggerInstance {
        @Override
        public ResourceLocation getCriterion() {
            return ID;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            return new JsonObject();
        }
    }
}

