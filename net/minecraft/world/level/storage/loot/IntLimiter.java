/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.function.IntUnaryOperator;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public class IntLimiter
implements IntUnaryOperator {
    private final Integer min;
    private final Integer max;
    private final IntUnaryOperator op;

    private IntLimiter(@Nullable Integer integer, @Nullable Integer integer2) {
        this.min = integer;
        this.max = integer2;
        if (integer == null) {
            if (integer2 == null) {
                this.op = i -> i;
            } else {
                int i2 = integer2;
                this.op = j -> Math.min(i2, j);
            }
        } else {
            int i3 = integer;
            if (integer2 == null) {
                this.op = j -> Math.max(i3, j);
            } else {
                int j2 = integer2;
                this.op = k -> Mth.clamp(k, i3, j2);
            }
        }
    }

    public static IntLimiter clamp(int i, int j) {
        return new IntLimiter(i, j);
    }

    public static IntLimiter lowerBound(int i) {
        return new IntLimiter(i, null);
    }

    public static IntLimiter upperBound(int i) {
        return new IntLimiter(null, i);
    }

    @Override
    public int applyAsInt(int i) {
        return this.op.applyAsInt(i);
    }

    public static class Serializer
    implements JsonDeserializer<IntLimiter>,
    JsonSerializer<IntLimiter> {
        @Override
        public IntLimiter deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
            Integer integer = jsonObject.has("min") ? Integer.valueOf(GsonHelper.getAsInt(jsonObject, "min")) : null;
            Integer integer2 = jsonObject.has("max") ? Integer.valueOf(GsonHelper.getAsInt(jsonObject, "max")) : null;
            return new IntLimiter(integer, integer2);
        }

        @Override
        public JsonElement serialize(IntLimiter intLimiter, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (intLimiter.max != null) {
                jsonObject.addProperty("max", intLimiter.max);
            }
            if (intLimiter.min != null) {
                jsonObject.addProperty("min", intLimiter.min);
            }
            return jsonObject;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((IntLimiter)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

