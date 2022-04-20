/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Function;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public class WrappedMinMaxBounds {
    public static final WrappedMinMaxBounds ANY = new WrappedMinMaxBounds(null, null);
    public static final SimpleCommandExceptionType ERROR_INTS_ONLY = new SimpleCommandExceptionType(Component.translatable("argument.range.ints"));
    @Nullable
    private final Float min;
    @Nullable
    private final Float max;

    public WrappedMinMaxBounds(@Nullable Float float_, @Nullable Float float2) {
        this.min = float_;
        this.max = float2;
    }

    public static WrappedMinMaxBounds exactly(float f) {
        return new WrappedMinMaxBounds(Float.valueOf(f), Float.valueOf(f));
    }

    public static WrappedMinMaxBounds between(float f, float g) {
        return new WrappedMinMaxBounds(Float.valueOf(f), Float.valueOf(g));
    }

    public static WrappedMinMaxBounds atLeast(float f) {
        return new WrappedMinMaxBounds(Float.valueOf(f), null);
    }

    public static WrappedMinMaxBounds atMost(float f) {
        return new WrappedMinMaxBounds(null, Float.valueOf(f));
    }

    public boolean matches(float f) {
        if (this.min != null && this.max != null && this.min.floatValue() > this.max.floatValue() && this.min.floatValue() > f && this.max.floatValue() < f) {
            return false;
        }
        if (this.min != null && this.min.floatValue() > f) {
            return false;
        }
        return this.max == null || !(this.max.floatValue() < f);
    }

    public boolean matchesSqr(double d) {
        if (this.min != null && this.max != null && this.min.floatValue() > this.max.floatValue() && (double)(this.min.floatValue() * this.min.floatValue()) > d && (double)(this.max.floatValue() * this.max.floatValue()) < d) {
            return false;
        }
        if (this.min != null && (double)(this.min.floatValue() * this.min.floatValue()) > d) {
            return false;
        }
        return this.max == null || !((double)(this.max.floatValue() * this.max.floatValue()) < d);
    }

    @Nullable
    public Float getMin() {
        return this.min;
    }

    @Nullable
    public Float getMax() {
        return this.max;
    }

    public JsonElement serializeToJson() {
        if (this == ANY) {
            return JsonNull.INSTANCE;
        }
        if (this.min != null && this.max != null && this.min.equals(this.max)) {
            return new JsonPrimitive(this.min);
        }
        JsonObject jsonObject = new JsonObject();
        if (this.min != null) {
            jsonObject.addProperty("min", this.min);
        }
        if (this.max != null) {
            jsonObject.addProperty("max", this.min);
        }
        return jsonObject;
    }

    public static WrappedMinMaxBounds fromJson(@Nullable JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return ANY;
        }
        if (GsonHelper.isNumberValue(jsonElement)) {
            float f = GsonHelper.convertToFloat(jsonElement, "value");
            return new WrappedMinMaxBounds(Float.valueOf(f), Float.valueOf(f));
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
        Float float_ = jsonObject.has("min") ? Float.valueOf(GsonHelper.getAsFloat(jsonObject, "min")) : null;
        Float float2 = jsonObject.has("max") ? Float.valueOf(GsonHelper.getAsFloat(jsonObject, "max")) : null;
        return new WrappedMinMaxBounds(float_, float2);
    }

    public static WrappedMinMaxBounds fromReader(StringReader stringReader, boolean bl) throws CommandSyntaxException {
        return WrappedMinMaxBounds.fromReader(stringReader, bl, float_ -> float_);
    }

    public static WrappedMinMaxBounds fromReader(StringReader stringReader, boolean bl, Function<Float, Float> function) throws CommandSyntaxException {
        Float float2;
        if (!stringReader.canRead()) {
            throw MinMaxBounds.ERROR_EMPTY.createWithContext(stringReader);
        }
        int i = stringReader.getCursor();
        Float float_ = WrappedMinMaxBounds.optionallyFormat(WrappedMinMaxBounds.readNumber(stringReader, bl), function);
        if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
            stringReader.skip();
            stringReader.skip();
            float2 = WrappedMinMaxBounds.optionallyFormat(WrappedMinMaxBounds.readNumber(stringReader, bl), function);
            if (float_ == null && float2 == null) {
                stringReader.setCursor(i);
                throw MinMaxBounds.ERROR_EMPTY.createWithContext(stringReader);
            }
        } else {
            if (!bl && stringReader.canRead() && stringReader.peek() == '.') {
                stringReader.setCursor(i);
                throw ERROR_INTS_ONLY.createWithContext(stringReader);
            }
            float2 = float_;
        }
        if (float_ == null && float2 == null) {
            stringReader.setCursor(i);
            throw MinMaxBounds.ERROR_EMPTY.createWithContext(stringReader);
        }
        return new WrappedMinMaxBounds(float_, float2);
    }

    @Nullable
    private static Float readNumber(StringReader stringReader, boolean bl) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && WrappedMinMaxBounds.isAllowedNumber(stringReader, bl)) {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        if (string.isEmpty()) {
            return null;
        }
        try {
            return Float.valueOf(Float.parseFloat(string));
        } catch (NumberFormatException numberFormatException) {
            if (bl) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidDouble().createWithContext(stringReader, string);
            }
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.readerInvalidInt().createWithContext(stringReader, string);
        }
    }

    private static boolean isAllowedNumber(StringReader stringReader, boolean bl) {
        char c = stringReader.peek();
        if (c >= '0' && c <= '9' || c == '-') {
            return true;
        }
        if (bl && c == '.') {
            return !stringReader.canRead(2) || stringReader.peek(1) != '.';
        }
        return false;
    }

    @Nullable
    private static Float optionallyFormat(@Nullable Float float_, Function<Float, Float> function) {
        return float_ == null ? null : function.apply(float_);
    }
}

