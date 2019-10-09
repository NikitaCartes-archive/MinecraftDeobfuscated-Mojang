/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class LocationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    private final ResourceLocation id;

    public LocationTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject);
        return new TriggerInstance(this.id, locationPredicate);
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer.getAdvancements(), triggerInstance -> triggerInstance.matches(serverPlayer.getLevel(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()));
    }

    @Override
    public /* synthetic */ CriterionTriggerInstance createInstance(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
        return this.createInstance(jsonObject, jsonDeserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation resourceLocation, LocationPredicate locationPredicate) {
            super(resourceLocation);
            this.location = locationPredicate;
        }

        public static TriggerInstance located(LocationPredicate locationPredicate) {
            return new TriggerInstance(CriteriaTriggers.LOCATION.id, locationPredicate);
        }

        public static TriggerInstance sleptInBed() {
            return new TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, LocationPredicate.ANY);
        }

        public static TriggerInstance raidWon() {
            return new TriggerInstance(CriteriaTriggers.RAID_WIN.id, LocationPredicate.ANY);
        }

        public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
            return this.location.matches(serverLevel, d, e, f);
        }

        @Override
        public JsonElement serializeToJson() {
            return this.location.serializeToJson();
        }
    }
}

