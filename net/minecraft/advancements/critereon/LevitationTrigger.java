/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    static final ResourceLocation ID = new ResourceLocation("levitation");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
        return new TriggerInstance(composite, distancePredicate, ints);
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer, vec3, i));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final DistancePredicate distance;
        private final MinMaxBounds.Ints duration;

        public TriggerInstance(EntityPredicate.Composite composite, DistancePredicate distancePredicate, MinMaxBounds.Ints ints) {
            super(ID, composite);
            this.distance = distancePredicate;
            this.duration = ints;
        }

        public static TriggerInstance levitated(DistancePredicate distancePredicate) {
            return new TriggerInstance(EntityPredicate.Composite.ANY, distancePredicate, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
            if (!this.distance.matches(vec3.x, vec3.y, vec3.z, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ())) {
                return false;
            }
            return this.duration.matches(i);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("distance", this.distance.serializeToJson());
            jsonObject.add("duration", this.duration.serializeToJson());
            return jsonObject;
        }
    }
}

