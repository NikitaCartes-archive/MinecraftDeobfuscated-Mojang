/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class LocationCheck
implements LootItemCondition {
    private final LocationPredicate predicate;

    private LocationCheck(LocationPredicate locationPredicate) {
        this.predicate = locationPredicate;
    }

    @Override
    public boolean test(LootContext lootContext) {
        BlockPos blockPos = lootContext.getParamOrNull(LootContextParams.BLOCK_POS);
        return blockPos != null && this.predicate.matches(lootContext.getLevel(), blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static LootItemCondition.Builder checkLocation(LocationPredicate.Builder builder) {
        return () -> new LocationCheck(builder.build());
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<LocationCheck> {
        public Serializer() {
            super(new ResourceLocation("location_check"), LocationCheck.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, LocationCheck locationCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.add("predicate", locationCheck.predicate.serializeToJson());
        }

        @Override
        public LocationCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            LocationPredicate locationPredicate = LocationPredicate.fromJson(jsonObject.get("predicate"));
            return new LocationCheck(locationPredicate);
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

