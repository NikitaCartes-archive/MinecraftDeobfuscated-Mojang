/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.GsonAdapterFactory;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public final class ConstantValue
implements NumberProvider {
    final float value;

    ConstantValue(float f) {
        this.value = f;
    }

    @Override
    public LootNumberProviderType getType() {
        return NumberProviders.CONSTANT;
    }

    @Override
    public float getFloat(LootContext lootContext) {
        return this.value;
    }

    public static ConstantValue exactly(float f) {
        return new ConstantValue(f);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        return Float.compare(((ConstantValue)object).value, this.value) == 0;
    }

    public int hashCode() {
        return this.value != 0.0f ? Float.floatToIntBits(this.value) : 0;
    }

    public static class InlineSerializer
    implements GsonAdapterFactory.InlineSerializer<ConstantValue> {
        @Override
        public JsonElement serialize(ConstantValue constantValue, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(Float.valueOf(constantValue.value));
        }

        @Override
        public ConstantValue deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            return new ConstantValue(GsonHelper.convertToFloat(jsonElement, "value"));
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonElement, jsonDeserializationContext);
        }
    }

    public static class Serializer
    implements net.minecraft.world.level.storage.loot.Serializer<ConstantValue> {
        @Override
        public void serialize(JsonObject jsonObject, ConstantValue constantValue, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("value", Float.valueOf(constantValue.value));
        }

        @Override
        public ConstantValue deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            float f = GsonHelper.getAsFloat(jsonObject, "value");
            return new ConstantValue(f);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return this.deserialize(jsonObject, jsonDeserializationContext);
        }
    }
}

