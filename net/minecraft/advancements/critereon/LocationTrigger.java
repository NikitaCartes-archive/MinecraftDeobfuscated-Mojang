/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class LocationTrigger
extends SimpleCriterionTrigger<TriggerInstance> {
    final ResourceLocation id;

    public LocationTrigger(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public TriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "location", jsonObject);
        LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject2);
        return new TriggerInstance(this.id, composite, locationPredicate);
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(serverPlayer.getLevel(), serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ()));
    }

    @Override
    public /* synthetic */ AbstractCriterionTriggerInstance createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return this.createInstance(jsonObject, composite, deserializationContext);
    }

    public static class TriggerInstance
    extends AbstractCriterionTriggerInstance {
        private final LocationPredicate location;

        public TriggerInstance(ResourceLocation resourceLocation, EntityPredicate.Composite composite, LocationPredicate locationPredicate) {
            super(resourceLocation, composite);
            this.location = locationPredicate;
        }

        public static TriggerInstance located(LocationPredicate locationPredicate) {
            return new TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.ANY, locationPredicate);
        }

        public static TriggerInstance located(EntityPredicate entityPredicate) {
            return new TriggerInstance(CriteriaTriggers.LOCATION.id, EntityPredicate.Composite.wrap(entityPredicate), LocationPredicate.ANY);
        }

        public static TriggerInstance sleptInBed() {
            return new TriggerInstance(CriteriaTriggers.SLEPT_IN_BED.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
        }

        public static TriggerInstance raidWon() {
            return new TriggerInstance(CriteriaTriggers.RAID_WIN.id, EntityPredicate.Composite.ANY, LocationPredicate.ANY);
        }

        public static TriggerInstance walkOnBlockWithEquipment(Block block, Item item) {
            return TriggerInstance.located(EntityPredicate.Builder.entity().equipment(EntityEquipmentPredicate.Builder.equipment().feet(ItemPredicate.Builder.item().of(item).build()).build()).steppingOn(LocationPredicate.Builder.location().setBlock(BlockPredicate.Builder.block().of(block).build()).build()).build());
        }

        public boolean matches(ServerLevel serverLevel, double d, double e, double f) {
            return this.location.matches(serverLevel, d, e, f);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            JsonObject jsonObject = super.serializeToJson(serializationContext);
            jsonObject.add("location", this.location.serializeToJson());
            return jsonObject;
        }
    }
}

