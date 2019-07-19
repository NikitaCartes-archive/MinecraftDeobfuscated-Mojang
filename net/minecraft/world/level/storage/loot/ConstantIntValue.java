/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.RandomIntGenerator;

public final class ConstantIntValue
implements RandomIntGenerator {
    private final int value;

    public ConstantIntValue(int i) {
        this.value = i;
    }

    @Override
    public int getInt(Random random) {
        return this.value;
    }

    @Override
    public ResourceLocation getType() {
        return CONSTANT;
    }

    public static ConstantIntValue exactly(int i) {
        return new ConstantIntValue(i);
    }

    public static class Serializer
    implements JsonDeserializer<ConstantIntValue>,
    JsonSerializer<ConstantIntValue> {
        @Override
        public ConstantIntValue deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return new ConstantIntValue(GsonHelper.convertToInt(jsonElement, "value"));
        }

        @Override
        public JsonElement serialize(ConstantIntValue constantIntValue, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(constantIntValue.value);
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((ConstantIntValue)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

