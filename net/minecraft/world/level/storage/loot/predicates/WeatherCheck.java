/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.predicates;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

public class WeatherCheck
implements LootItemCondition {
    @Nullable
    private final Boolean isRaining;
    @Nullable
    private final Boolean isThundering;

    private WeatherCheck(@Nullable Boolean boolean_, @Nullable Boolean boolean2) {
        this.isRaining = boolean_;
        this.isThundering = boolean2;
    }

    @Override
    public boolean test(LootContext lootContext) {
        ServerLevel serverLevel = lootContext.getLevel();
        if (this.isRaining != null && this.isRaining.booleanValue() != serverLevel.isRaining()) {
            return false;
        }
        return this.isThundering == null || this.isThundering.booleanValue() == serverLevel.isThundering();
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((LootContext)object);
    }

    public static class Serializer
    extends LootItemCondition.Serializer<WeatherCheck> {
        public Serializer() {
            super(new ResourceLocation("weather_check"), WeatherCheck.class);
        }

        @Override
        public void serialize(JsonObject jsonObject, WeatherCheck weatherCheck, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("raining", weatherCheck.isRaining);
            jsonObject.addProperty("thundering", weatherCheck.isThundering);
        }

        @Override
        public WeatherCheck deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            Boolean boolean_ = jsonObject.has("raining") ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, "raining")) : null;
            Boolean boolean2 = jsonObject.has("thundering") ? Boolean.valueOf(GsonHelper.getAsBoolean(jsonObject, "thundering")) : null;
            return new WeatherCheck(boolean_, boolean2);
        }

        @Override
        public /* synthetic */ LootItemCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

