/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.RandomValueBounds;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.jetbrains.annotations.Nullable;

public class TimeCheck
implements LootItemCondition {
    @Nullable
    private final Long period;
    private final RandomValueBounds value;

    private TimeCheck(@Nullable Long long_, RandomValueBounds randomValueBounds) {
        this.period = long_;
        this.value = randomValueBounds;
    }

    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.TIME_CHECK;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel serverLevel = lootContext.getLevel();
        long l = serverLevel.getDayTime();
        if (this.period != null) {
            l %= this.period.longValue();
        }
        return this.value.matchesValue((int)l);
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<TimeCheck> {
        @Override
        public void serialize(JsonObject jsonObject, TimeCheck timeCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("period", timeCheck.period);
            jsonObject.add("value", jsonSerializationContext.serialize(timeCheck.value));
        }

        @Override
        public TimeCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Long long_ = jsonObject.has("period") ? Long.valueOf(GsonHelper.getAsLong(jsonObject, "period")) : null;
            RandomValueBounds randomValueBounds = GsonHelper.getAsObject(jsonObject, "value", jsonDeserializationContext, RandomValueBounds.class);
            return new TimeCheck(long_, randomValueBounds);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

