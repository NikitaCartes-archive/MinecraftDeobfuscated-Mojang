/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import org.jetbrains.annotations.Nullable;

public class IntRange {
    @Nullable
    private final NumberProvider min;
    @Nullable
    private final NumberProvider max;
    private final IntLimiter limiter;
    private final IntChecker predicate;

    public Set<LootContextParam<?>> getReferencedContextParams() {
        ImmutableSet.Builder builder = ImmutableSet.builder();
        if (this.min != null) {
            builder.addAll(this.min.getReferencedContextParams());
        }
        if (this.max != null) {
            builder.addAll(this.max.getReferencedContextParams());
        }
        return builder.build();
    }

    private IntRange(@Nullable NumberProvider numberProvider, @Nullable NumberProvider numberProvider2) {
        this.min = numberProvider;
        this.max = numberProvider2;
        if (numberProvider == null) {
            if (numberProvider2 == null) {
                this.limiter = (lootContext, i) -> i;
                this.predicate = (lootContext, i) -> true;
            } else {
                this.limiter = (lootContext, i) -> Math.min(numberProvider2.getInt(lootContext), i);
                this.predicate = (lootContext, i) -> i <= numberProvider2.getInt(lootContext);
            }
        } else if (numberProvider2 == null) {
            this.limiter = (lootContext, i) -> Math.max(numberProvider.getInt(lootContext), i);
            this.predicate = (lootContext, i) -> i >= numberProvider.getInt(lootContext);
        } else {
            this.limiter = (lootContext, i) -> Mth.clamp(i, numberProvider.getInt(lootContext), numberProvider2.getInt(lootContext));
            this.predicate = (lootContext, i) -> i >= numberProvider.getInt(lootContext) && i <= numberProvider2.getInt(lootContext);
        }
    }

    public static IntRange exact(int i) {
        ConstantValue constantValue = ConstantValue.exactly(i);
        return new IntRange(constantValue, constantValue);
    }

    public static IntRange range(int i, int j) {
        return new IntRange(ConstantValue.exactly(i), ConstantValue.exactly(j));
    }

    public static IntRange lowerBound(int i) {
        return new IntRange(ConstantValue.exactly(i), null);
    }

    public static IntRange upperBound(int i) {
        return new IntRange(null, ConstantValue.exactly(i));
    }

    public int clamp(LootContext lootContext, int i) {
        return this.limiter.apply(lootContext, i);
    }

    public boolean test(LootContext lootContext, int i) {
        return this.predicate.test(lootContext, i);
    }

    public static class Serializer
    implements JsonDeserializer<IntRange>,
    JsonSerializer<IntRange> {
        @Override
        public IntRange deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
            if (jsonElement.isJsonPrimitive()) {
                return IntRange.exact(jsonElement.getAsInt());
            }
            JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
            NumberProvider numberProvider = jsonObject.has("min") ? GsonHelper.getAsObject(jsonObject, "min", jsonDeserializationContext, NumberProvider.class) : null;
            NumberProvider numberProvider2 = jsonObject.has("max") ? GsonHelper.getAsObject(jsonObject, "max", jsonDeserializationContext, NumberProvider.class) : null;
            return new IntRange(numberProvider, numberProvider2);
        }

        @Override
        public JsonElement serialize(IntRange intRange, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            if (Objects.equals(intRange.max, intRange.min)) {
                return jsonSerializationContext.serialize(intRange.min);
            }
            if (intRange.max != null) {
                jsonObject.add("max", jsonSerializationContext.serialize(intRange.max));
            }
            if (intRange.min != null) {
                jsonObject.add("min", jsonSerializationContext.serialize(intRange.min));
            }
            return jsonObject;
        }

        @Override
        public /* synthetic */ JsonElement serialize(Object object, Type type, JsonSerializationContext jsonSerializationContext) {
            return this.serialize((IntRange)object, type, jsonSerializationContext);
        }

        @Override
        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }

    @FunctionalInterface
    static interface IntLimiter {
        public int apply(LootContext var1, int var2);
    }

    @FunctionalInterface
    static interface IntChecker {
        public boolean test(LootContext var1, int var2);
    }
}

