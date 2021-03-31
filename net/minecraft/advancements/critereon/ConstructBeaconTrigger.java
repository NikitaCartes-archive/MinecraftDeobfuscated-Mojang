/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ConstructBeaconTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("construct_beacon");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("level"));
        return new TriggerInstance(composite, ints);
    }

    public void trigger(ServerPlayer serverPlayer, int i) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(i));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final MinMaxBounds.Ints level;

        public TriggerInstance(EntityPredicate.Composite composite, MinMaxBounds.Ints ints) {
            super(ID, composite);
            this.level = ints;
        }

        public static TriggerInstance constructedBeacon() {
            return new TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY);
        }

        public static TriggerInstance constructedBeacon(MinMaxBounds.Ints ints) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, ints);
        }

        public boolean matches(int i) {
            return this.level.matches(i);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("level", this.level.serializeToJson());
            return jsonObject;
        }
    }
}

