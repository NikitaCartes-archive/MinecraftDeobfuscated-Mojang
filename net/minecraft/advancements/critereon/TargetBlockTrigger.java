/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TargetBlockTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("target_hit");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("signalStrength"));
        return new TriggerInstance(ints);
    }

    public void trigger(ServerPlayer serverPlayer, int i) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(i));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints signalStrength;

        public TriggerInstance(MinMaxBounds.Ints ints) {
            super(ID);
            this.signalStrength = ints;
        }

        public static TriggerInstance targetHit(MinMaxBounds.Ints ints) {
            return new TriggerInstance(ints);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("signalStrength", this.signalStrength.serializeToJson());
            return jsonObject;
        }

        public boolean matches(int i) {
            return this.signalStrength.matches(i);
        }
    }
}

