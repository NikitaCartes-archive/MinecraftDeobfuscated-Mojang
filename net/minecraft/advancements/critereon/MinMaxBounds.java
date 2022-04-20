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
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;

public abstract class MinMaxBounds<T extends Number> {
    public static final SimpleCommandExceptionType ERROR_EMPTY = new SimpleCommandExceptionType(Component.translatable("argument.range.empty"));
    public static final SimpleCommandExceptionType ERROR_SWAPPED = new SimpleCommandExceptionType(Component.translatable("argument.range.swapped"));
    @Nullable
    protected final T min;
    @Nullable
    protected final T max;

    protected MinMaxBounds(@Nullable T number, @Nullable T number2) {
        this.min = number;
        this.max = number2;
    }

    @Nullable
    public T getMin() {
        return this.min;
    }

    @Nullable
    public T getMax() {
        return this.max;
    }

    public boolean isAny() {
        return this.min == null && this.max == null;
    }

    public JsonElement serializeToJson() {
        if (this.isAny()) {
            return JsonNull.INSTANCE;
        }
        if (this.min != null && this.min.equals(this.max)) {
            return new JsonPrimitive((Number)this.min);
        }
        JsonObject jsonObject = new JsonObject();
        if (this.min != null) {
            jsonObject.addProperty("min", (Number)this.min);
        }
        if (this.max != null) {
            jsonObject.addProperty("max", (Number)this.max);
        }
        return jsonObject;
    }

    protected static <T extends Number, R extends MinMaxBounds<T>> R fromJson(@Nullable JsonElement jsonElement, R minMaxBounds, BiFunction<JsonElement, String, T> biFunction, BoundsFactory<T, R> boundsFactory) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return minMaxBounds;
        }
        if (GsonHelper.isNumberValue(jsonElement)) {
            Number number = (Number)biFunction.apply(jsonElement, "value");
            return boundsFactory.create(number, number);
        }
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "value");
        Number number2 = jsonObject.has("min") ? (Number)((Number)biFunction.apply(jsonObject.get("min"), "min")) : (Number)null;
        Number number3 = jsonObject.has("max") ? (Number)((Number)biFunction.apply(jsonObject.get("max"), "max")) : (Number)null;
        return boundsFactory.create(number2, number3);
    }

    protected static <T extends Number, R extends MinMaxBounds<T>> R fromReader(StringReader stringReader, BoundsFromReaderFactory<T, R> boundsFromReaderFactory, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier, Function<T, T> function2) throws CommandSyntaxException {
        if (!stringReader.canRead()) {
            throw ERROR_EMPTY.createWithContext(stringReader);
        }
        int i = stringReader.getCursor();
        try {
            Number number2;
            Number number = (Number)MinMaxBounds.optionallyFormat(MinMaxBounds.readNumber(stringReader, function, supplier), function2);
            if (stringReader.canRead(2) && stringReader.peek() == '.' && stringReader.peek(1) == '.') {
                stringReader.skip();
                stringReader.skip();
                number2 = (Number)MinMaxBounds.optionallyFormat(MinMaxBounds.readNumber(stringReader, function, supplier), function2);
                if (number == null && number2 == null) {
                    throw ERROR_EMPTY.createWithContext(stringReader);
                }
            } else {
                number2 = number;
            }
            if (number == null && number2 == null) {
                throw ERROR_EMPTY.createWithContext(stringReader);
            }
            return boundsFromReaderFactory.create(stringReader, number, number2);
        } catch (CommandSyntaxException commandSyntaxException) {
            stringReader.setCursor(i);
            throw new CommandSyntaxException(commandSyntaxException.getType(), commandSyntaxException.getRawMessage(), commandSyntaxException.getInput(), i);
        }
    }

    @Nullable
    private static <T extends Number> T readNumber(StringReader stringReader, Function<String, T> function, Supplier<DynamicCommandExceptionType> supplier) throws CommandSyntaxException {
        int i = stringReader.getCursor();
        while (stringReader.canRead() && MinMaxBounds.isAllowedInputChat(stringReader)) {
            stringReader.skip();
        }
        String string = stringReader.getString().substring(i, stringReader.getCursor());
        if (string.isEmpty()) {
            return null;
        }
        try {
            return (T)((Number)function.apply(string));
        } catch (NumberFormatException numberFormatException) {
            throw supplier.get().createWithContext(stringReader, string);
        }
    }

    private static boolean isAllowedInputChat(StringReader stringReader) {
        char c = stringReader.peek();
        if (c >= '0' && c <= '9' || c == '-') {
            return true;
        }
        if (c == '.') {
            return !stringReader.canRead(2) || stringReader.peek(1) != '.';
        }
        return false;
    }

    @Nullable
    private static <T> T optionallyFormat(@Nullable T object, Function<T, T> function) {
        return object == null ? null : (T)function.apply(object);
    }

    @FunctionalInterface
    protected static interface BoundsFactory<T extends Number, R extends MinMaxBounds<T>> {
        public R create(@Nullable T var1, @Nullable T var2);
    }

    @FunctionalInterface
    protected static interface BoundsFromReaderFactory<T extends Number, R extends MinMaxBounds<T>> {
        public R create(StringReader var1, @Nullable T var2, @Nullable T var3) throws CommandSyntaxException;
    }

    public static class Doubles
    extends MinMaxBounds<Double> {
        public static final Doubles ANY = new Doubles(null, null);
        @Nullable
        private final Double minSq;
        @Nullable
        private final Double maxSq;

        private static Doubles create(StringReader stringReader, @Nullable Double double_, @Nullable Double double2) throws CommandSyntaxException {
            if (double_ != null && double2 != null && double_ > double2) {
                throw ERROR_SWAPPED.createWithContext(stringReader);
            }
            return new Doubles(double_, double2);
        }

        @Nullable
        private static Double squareOpt(@Nullable Double double_) {
            return double_ == null ? null : Double.valueOf(double_ * double_);
        }

        private Doubles(@Nullable Double double_, @Nullable Double double2) {
            super(double_, double2);
            this.minSq = Doubles.squareOpt(double_);
            this.maxSq = Doubles.squareOpt(double2);
        }

        public static Doubles exactly(double d) {
            return new Doubles(d, d);
        }

        public static Doubles between(double d, double e) {
            return new Doubles(d, e);
        }

        public static Doubles atLeast(double d) {
            return new Doubles(d, null);
        }

        public static Doubles atMost(double d) {
            return new Doubles(null, d);
        }

        public boolean matches(double d) {
            if (this.min != null && (Double)this.min > d) {
                return false;
            }
            return this.max == null || !((Double)this.max < d);
        }

        public boolean matchesSqr(double d) {
            if (this.minSq != null && this.minSq > d) {
                return false;
            }
            return this.maxSq == null || !(this.maxSq < d);
        }

        public static Doubles fromJson(@Nullable JsonElement jsonElement) {
            return Doubles.fromJson(jsonElement, ANY, GsonHelper::convertToDouble, Doubles::new);
        }

        public static Doubles fromReader(StringReader stringReader) throws CommandSyntaxException {
            return Doubles.fromReader(stringReader, double_ -> double_);
        }

        public static Doubles fromReader(StringReader stringReader, Function<Double, Double> function) throws CommandSyntaxException {
            return Doubles.fromReader(stringReader, Doubles::create, Double::parseDouble, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidDouble, function);
        }
    }

    public static class Ints
    extends MinMaxBounds<Integer> {
        public static final Ints ANY = new Ints(null, null);
        @Nullable
        private final Long minSq;
        @Nullable
        private final Long maxSq;

        private static Ints create(StringReader stringReader, @Nullable Integer integer, @Nullable Integer integer2) throws CommandSyntaxException {
            if (integer != null && integer2 != null && integer > integer2) {
                throw ERROR_SWAPPED.createWithContext(stringReader);
            }
            return new Ints(integer, integer2);
        }

        @Nullable
        private static Long squareOpt(@Nullable Integer integer) {
            return integer == null ? null : Long.valueOf(integer.longValue() * integer.longValue());
        }

        private Ints(@Nullable Integer integer, @Nullable Integer integer2) {
            super(integer, integer2);
            this.minSq = Ints.squareOpt(integer);
            this.maxSq = Ints.squareOpt(integer2);
        }

        public static Ints exactly(int i) {
            return new Ints(i, i);
        }

        public static Ints between(int i, int j) {
            return new Ints(i, j);
        }

        public static Ints atLeast(int i) {
            return new Ints(i, null);
        }

        public static Ints atMost(int i) {
            return new Ints(null, i);
        }

        public boolean matches(int i) {
            if (this.min != null && (Integer)this.min > i) {
                return false;
            }
            return this.max == null || (Integer)this.max >= i;
        }

        public boolean matchesSqr(long l) {
            if (this.minSq != null && this.minSq > l) {
                return false;
            }
            return this.maxSq == null || this.maxSq >= l;
        }

        public static Ints fromJson(@Nullable JsonElement jsonElement) {
            return Ints.fromJson(jsonElement, ANY, GsonHelper::convertToInt, Ints::new);
        }

        public static Ints fromReader(StringReader stringReader) throws CommandSyntaxException {
            return Ints.fromReader(stringReader, integer -> integer);
        }

        public static Ints fromReader(StringReader stringReader, Function<Integer, Integer> function) throws CommandSyntaxException {
            return Ints.fromReader(stringReader, Ints::create, Integer::parseInt, CommandSyntaxException.BUILT_IN_EXCEPTIONS::readerInvalidInt, function);
        }
    }
}

