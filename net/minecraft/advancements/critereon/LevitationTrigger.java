/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LevitationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation("levitation");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
        MinMaxBounds.Ints ints = MinMaxBounds.Ints.fromJson(jsonObject.get("duration"));
        return new TriggerInstance(distancePredicate, ints);
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3, int i) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer, vec3, i));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final DistancePredicate distance;
        private final MinMaxBounds.Ints duration;

        public TriggerInstance(DistancePredicate distancePredicate, MinMaxBounds.Ints ints) {
            super(ID);
            this.distance = distancePredicate;
            this.duration = ints;
        }

        public static TriggerInstance levitated(DistancePredicate distancePredicate) {
            return new TriggerInstance(distancePredicate, MinMaxBounds.Ints.ANY);
        }

        public boolean matches(ServerPlayer serverPlayer, Vec3 vec3, int i) {
            if (!this.distance.matches(vec3.x, vec3.y, vec3.z, serverPlayer.x, serverPlayer.y, serverPlayer.z)) {
                return false;
            }
            return this.duration.matches(i);
        }

        @Override
        public JsonElement serializeToJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("distance", this.distance.serializeToJson());
            jsonObject.add("duration", this.duration.serializeToJson());
            return jsonObject;
        }
    }
}

