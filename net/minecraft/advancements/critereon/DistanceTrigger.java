/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.DistancePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class DistanceTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    final ResourceLocation id;

    public DistanceTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("start_position"));
        DistancePredicate distancePredicate = DistancePredicate.fromJson(jsonObject.get("distance"));
        return new TriggerInstance(this.id, composite, locationPredicate, distancePredicate);
    }

    public void trigger(ServerPlayer serverPlayer, Vec3 vec3) {
        Vec3 vec32 = serverPlayer.position();
        this.trigger(serverPlayer, (T triggerInstance) -> triggerInstance.matches(serverPlayer.getLevel(), vec3, vec32));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final LocationPredicate startPosition;
        private final DistancePredicate distance;

        public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite, LocationPredicate locationPredicate, DistancePredicate distancePredicate) {
            super(resourceLocation, composite);
            this.startPosition = locationPredicate;
            this.distance = distancePredicate;
        }

        public static TriggerInstance fallFromHeight(EntityPredicate.Builder builder, DistancePredicate distancePredicate, LocationPredicate locationPredicate) {
            return new TriggerInstance(CriteriaTriggers.FALL_FROM_HEIGHT.id, EntityPredicate.Composite.wrap(builder.build()), locationPredicate, distancePredicate);
        }

        public static TriggerInstance rideEntityInLava(EntityPredicate.Builder builder, DistancePredicate distancePredicate) {
            return new TriggerInstance(CriteriaTriggers.RIDE_ENTITY_IN_LAVA_TRIGGER.id, EntityPredicate.Composite.wrap(builder.build()), LocationPredicate.ANY, distancePredicate);
        }

        public static TriggerInstance travelledThroughNether(DistancePredicate distancePredicate) {
            return new TriggerInstance(CriteriaTriggers.NETHER_TRAVEL.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY, distancePredicate);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("start_position", this.startPosition.serializeToJson());
            jsonObject.add("distance", this.distance.serializeToJson());
            return jsonObject;
        }

        public boolean matches(ServerLevel serverLevel, Vec3 vec3, Vec3 vec32) {
            if (!this.startPosition.matches(serverLevel, vec3.x, vec3.y, vec3.z)) {
                return false;
            }
            return this.distance.matches(vec3.x, vec3.y, vec3.z, vec32.x, vec32.y, vec32.z);
        }
    }
}

