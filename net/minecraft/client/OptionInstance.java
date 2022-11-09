/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class OptionInstance<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Enum<Boolean> BOOLEAN_VALUES = new Enum<Boolean>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
    private final TooltipSupplier<T> tooltip;
    final Function<T, Component> toString;
    private final ValueSet<T> values;
    private final Codec<T> codec;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    final Component caption;
    T value;

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, boolean bl) {
        return OptionInstance.createBoolean(string, tooltipSupplier, bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, TooltipSupplier<Boolean> tooltipSupplier, boolean bl, Consumer<Boolean> consumer) {
        return new OptionInstance<Boolean>(string, tooltipSupplier, (component, boolean_) -> boolean_ != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, BOOLEAN_VALUES, bl, consumer);
    }

    public OptionInstance(String string, TooltipSupplier<T> tooltipSupplier, CaptionBasedToString<T> captionBasedToString, ValueSet<T> valueSet, T object, Consumer<T> consumer) {
        this(string, tooltipSupplier, captionBasedToString, valueSet, valueSet.codec(), object, consumer);
    }

    public OptionInstance(String string, TooltipSupplier<T> tooltipSupplier, CaptionBasedToString<T> captionBasedToString, ValueSet<T> valueSet, Codec<T> codec, T object2, Consumer<T> consumer) {
        this.caption = Component.translatable(string);
        this.tooltip = tooltipSupplier;
        this.toString = object -> captionBasedToString.toString(this.caption, object);
        this.values = valueSet;
        this.codec = codec;
        this.initialValue = object2;
        this.onValueUpdate = consumer;
        this.value = this.initialValue;
    }

    public static <T> TooltipSupplier<T> noTooltip() {
        return object -> null;
    }

    public static <T> TooltipSupplier<T> cachedConstantTooltip(Component component) {
        return object -> Tooltip.create(component);
    }

    public static <T extends OptionEnum> CaptionBasedToString<T> forOptionEnum() {
        return (component, optionEnum) -> optionEnum.getCaption();
    }

    public AbstractWidget createButton(Options options, int i, int j, int k) {
        return this.values.createButton(this.tooltip, options, i, j, k).apply(this);
    }

    public T get() {
        return this.value;
    }

    public Codec<T> codec() {
        return this.codec;
    }

    public String toString() {
        return this.caption.getString();
    }

    public void set(T object) {
        Object object2 = this.values.validateValue(object).orElseGet(() -> {
            LOGGER.error("Illegal option value " + object + " for " + this.caption);
            return this.initialValue;
        });
        if (!Minecraft.getInstance().isRunning()) {
            this.value = object2;
            return;
        }
        if (!Objects.equals(this.value, object2)) {
            this.value = object2;
            this.onValueUpdate.accept(this.value);
        }
    }

    public ValueSet<T> values() {
        return this.values;
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface TooltipSupplier<T> {
        @Nullable
        public Tooltip apply(T var1);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface CaptionBasedToString<T> {
        public Component toString(Component var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    public record Enum<T>(List<T> values, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public Optional<T> validateValue(T object) {
            return this.values.contains(object) ? Optional.of(object) : Optional.empty();
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.values);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface ValueSet<T> {
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5);

        public Optional<T> validateValue(T var1);

        public Codec<T> codec();
    }

    @Environment(value=EnvType.CLIENT)
    public static enum UnitDouble implements SliderableValueSet<Double>
    {
        INSTANCE;


        @Override
        public Optional<Double> validateValue(Double double_) {
            return double_ >= 0.0 && double_ <= 1.0 ? Optional.of(double_) : Optional.empty();
        }

        @Override
        public double toSliderValue(Double double_) {
            return double_;
        }

        @Override
        public Double fromSliderValue(double d) {
            return d;
        }

        public <R> SliderableValueSet<R> xmap(final DoubleFunction<? extends R> doubleFunction, final ToDoubleFunction<? super R> toDoubleFunction) {
            return new SliderableValueSet<R>(){

                @Override
                public Optional<R> validateValue(R object) {
                    return this.validateValue(toDoubleFunction.applyAsDouble(object)).map(doubleFunction::apply);
                }

                @Override
                public double toSliderValue(R object) {
                    return this.toSliderValue(toDoubleFunction.applyAsDouble(object));
                }

                @Override
                public R fromSliderValue(double d) {
                    return doubleFunction.apply(this.fromSliderValue(d));
                }

                @Override
                public Codec<R> codec() {
                    return this.codec().xmap(doubleFunction::apply, toDoubleFunction::applyAsDouble);
                }
            };
        }

        @Override
        public Codec<Double> codec() {
            return Codec.either(Codec.doubleRange(0.0, 1.0), Codec.BOOL).xmap((? super A either) -> either.map(double_ -> double_, boolean_ -> boolean_ != false ? 1.0 : 0.0), Either::left);
        }

        @Override
        public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier) implements IntRangeBase,
    SliderableOrCyclableValueSet<Integer>
    {
        @Override
        public Optional<Integer> validateValue(Integer integer) {
            return Optional.of(Mth.clamp(integer, this.minInclusive(), this.maxInclusive()));
        }

        @Override
        public int maxInclusive() {
            return this.maxSupplier.getAsInt();
        }

        @Override
        public Codec<Integer> codec() {
            Function<Integer, DataResult> function = integer -> {
                int i = this.maxSupplier.getAsInt() + 1;
                if (integer.compareTo(this.minInclusive) >= 0 && integer.compareTo(i) <= 0) {
                    return DataResult.success(integer);
                }
                return DataResult.error("Value " + integer + " outside of range [" + this.minInclusive + ":" + i + "]", integer);
            };
            return Codec.INT.flatXmap(function, function);
        }

        @Override
        public boolean createCycleButton() {
            return true;
        }

        @Override
        public CycleButton.ValueListSupplier<Integer> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(IntStream.range(this.minInclusive, this.maxInclusive() + 1).boxed().toList());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record IntRange(int minInclusive, int maxInclusive) implements IntRangeBase
    {
        @Override
        public Optional<Integer> validateValue(Integer integer) {
            return integer.compareTo(this.minInclusive()) >= 0 && integer.compareTo(this.maxInclusive()) <= 0 ? Optional.of(integer) : Optional.empty();
        }

        @Override
        public Codec<Integer> codec() {
            return Codec.intRange(this.minInclusive, this.maxInclusive + 1);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface IntRangeBase
    extends SliderableValueSet<Integer> {
        public int minInclusive();

        public int maxInclusive();

        @Override
        default public double toSliderValue(Integer integer) {
            return Mth.map(integer.intValue(), this.minInclusive(), this.maxInclusive(), 0.0f, 1.0f);
        }

        @Override
        default public Integer fromSliderValue(double d) {
            return Mth.floor(Mth.map(d, 0.0, 1.0, (double)this.minInclusive(), (double)this.maxInclusive()));
        }

        default public <R> SliderableValueSet<R> xmap(final IntFunction<? extends R> intFunction, final ToIntFunction<? super R> toIntFunction) {
            return new SliderableValueSet<R>(){

                @Override
                public Optional<R> validateValue(R object) {
                    return this.validateValue(toIntFunction.applyAsInt(object)).map(intFunction::apply);
                }

                @Override
                public double toSliderValue(R object) {
                    return this.toSliderValue(toIntFunction.applyAsInt(object));
                }

                @Override
                public R fromSliderValue(double d) {
                    return intFunction.apply(this.fromSliderValue(d));
                }

                @Override
                public Codec<R> codec() {
                    return this.codec().xmap(intFunction::apply, toIntFunction::applyAsInt);
                }
            };
        }

        @Override
        default public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class OptionInstanceSliderButton<N>
    extends AbstractOptionSliderButton {
        private final OptionInstance<N> instance;
        private final SliderableValueSet<N> values;
        private final TooltipSupplier<N> tooltipSupplier;

        OptionInstanceSliderButton(Options options, int i, int j, int k, int l, OptionInstance<N> optionInstance, SliderableValueSet<N> sliderableValueSet, TooltipSupplier<N> tooltipSupplier) {
            super(options, i, j, k, l, sliderableValueSet.toSliderValue(optionInstance.get()));
            this.instance = optionInstance;
            this.values = sliderableValueSet;
            this.tooltipSupplier = tooltipSupplier;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.instance.toString.apply(this.instance.get()));
            this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
        }

        @Override
        protected void applyValue() {
            this.instance.set(this.values.fromSliderValue(this.value));
            this.options.save();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public Optional<T> validateValue(T object) {
            return this.validateValue.apply(object);
        }

        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create((Collection)this.values.get());
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec) implements CycleableValueSet<T>
    {
        @Override
        public CycleButton.ValueListSupplier<T> valueListSupplier() {
            return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
        }

        @Override
        public Optional<T> validateValue(T object) {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(object) ? Optional.of(object) : Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface SliderableOrCyclableValueSet<T>
    extends CycleableValueSet<T>,
    SliderableValueSet<T> {
        public boolean createCycleButton();

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            if (this.createCycleButton()) {
                return CycleableValueSet.super.createButton(tooltipSupplier, options, i, j, k);
            }
            return SliderableValueSet.super.createButton(tooltipSupplier, options, i, j, k);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface CycleableValueSet<T>
    extends ValueSet<T> {
        public CycleButton.ValueListSupplier<T> valueListSupplier();

        default public ValueSetter<T> valueSetter() {
            return OptionInstance::set;
        }

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> CycleButton.builder(optionInstance.toString).withValues(this.valueListSupplier()).withTooltip(tooltipSupplier).withInitialValue(optionInstance.value).create(i, j, k, 20, optionInstance.caption, (cycleButton, object) -> {
                this.valueSetter().set((OptionInstance<Object>)optionInstance, object);
                options.save();
            });
        }

        @Environment(value=EnvType.CLIENT)
        public static interface ValueSetter<T> {
            public void set(OptionInstance<T> var1, T var2);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface SliderableValueSet<T>
    extends ValueSet<T> {
        public double toSliderValue(T var1);

        public T fromSliderValue(double var1);

        @Override
        default public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> new OptionInstanceSliderButton(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
        }
    }
}

