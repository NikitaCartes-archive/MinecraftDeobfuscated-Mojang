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
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public final class OptionInstance<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Enum<Boolean> BOOLEAN_VALUES = new Enum<Boolean>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
    private final Function<Minecraft, TooltipSupplier<T>> tooltip;
    final Function<T, Component> toString;
    private final ValueSet<T> values;
    private final Codec<T> codec;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    private final Component caption;
    T value;

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, Function<Minecraft, TooltipSupplier<Boolean>> function, boolean bl) {
        return OptionInstance.createBoolean(string, function, bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, Function<Minecraft, TooltipSupplier<Boolean>> function, boolean bl, Consumer<Boolean> consumer) {
        return new OptionInstance<Boolean>(string, function, boolean_ -> boolean_ != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, BOOLEAN_VALUES, bl, consumer);
    }

    public OptionInstance(String string, Function<Minecraft, TooltipSupplier<T>> function, Function<T, Component> function2, ValueSet<T> valueSet, T object, Consumer<T> consumer) {
        this(string, function, function2, valueSet, valueSet.codec(), object, consumer);
    }

    public OptionInstance(String string, Function<Minecraft, TooltipSupplier<T>> function, Function<T, Component> function2, ValueSet<T> valueSet, Codec<T> codec, T object, Consumer<T> consumer) {
        this.caption = new TranslatableComponent(string);
        this.tooltip = function;
        this.toString = function2;
        this.values = valueSet;
        this.codec = codec;
        this.initialValue = object;
        this.onValueUpdate = consumer;
        this.value = this.initialValue;
    }

    public static <T> Function<Minecraft, TooltipSupplier<T>> noTooltip() {
        return minecraft -> object -> ImmutableList.of();
    }

    public AbstractWidget createButton(Options options, int i, int j, int k) {
        TooltipSupplier<T> tooltipSupplier = this.tooltip.apply(Minecraft.getInstance());
        return this.values.createButton(tooltipSupplier, options, i, j, k).apply(this);
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
            LOGGER.error("Illegal option value " + object + " for " + this.getCaption());
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

    protected Component getCaption() {
        return this.caption;
    }

    public static ValueSet<Integer> clampingLazyMax(final int i, final IntSupplier intSupplier) {
        return new IntRangeBase(){

            @Override
            public Optional<Integer> validateValue(Integer integer) {
                return Optional.of(Mth.clamp(integer, this.minInclusive(), this.maxInclusive()));
            }

            @Override
            public int minInclusive() {
                return i;
            }

            @Override
            public int maxInclusive() {
                return intSupplier.getAsInt();
            }

            @Override
            public Codec<Integer> codec() {
                Function<Integer, DataResult> function = integer -> {
                    int j = intSupplier.getAsInt() + 1;
                    if (integer.compareTo(i) >= 0 && integer.compareTo(j) <= 0) {
                        return DataResult.success(integer);
                    }
                    return DataResult.error("Value " + integer + " outside of range [" + i + ":" + j + "]", integer);
                };
                return Codec.INT.flatXmap(function, function);
            }
        };
    }

    @Environment(value=EnvType.CLIENT)
    public record Enum<T>(List<T> values, Codec<T> codec) implements ValueSet<T>
    {
        @Override
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> CycleButton.builder(optionInstance.toString).withValues((Collection<T>)this.values).withTooltip(tooltipSupplier).withInitialValue(optionInstance.value).create(i, j, k, 20, optionInstance.getCaption(), (cycleButton, object) -> {
                optionInstance.set(object);
                options.save();
            });
        }

        @Override
        public Optional<T> validateValue(T object) {
            return this.values.contains(object) ? Optional.of(object) : Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface ValueSet<T> {
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5);

        public Optional<T> validateValue(T var1);

        public Codec<T> codec();
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    public static interface TooltipSupplier<T>
    extends Function<T, List<FormattedCharSequence>> {
    }

    @Environment(value=EnvType.CLIENT)
    public static enum UnitDouble implements SliderableValueSet<Double>
    {
        INSTANCE;


        @Override
        public Function<OptionInstance<Double>, AbstractWidget> createButton(TooltipSupplier<Double> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> new OptionInstanceSliderButton<Double>(options, i, j, k, 20, (OptionInstance<Double>)optionInstance, this, tooltipSupplier);
        }

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
                public Function<OptionInstance<R>, AbstractWidget> createButton(TooltipSupplier<R> tooltipSupplier, Options options, int i, int j, int k) {
                    return optionInstance -> new OptionInstanceSliderButton(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
                }

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
        default public Function<OptionInstance<Integer>, AbstractWidget> createButton(TooltipSupplier<Integer> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> new OptionInstanceSliderButton<Integer>(options, i, j, k, 20, (OptionInstance<Integer>)optionInstance, this, tooltipSupplier);
        }

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
                public Function<OptionInstance<R>, AbstractWidget> createButton(TooltipSupplier<R> tooltipSupplier, Options options, int i, int j, int k) {
                    return optionInstance -> new OptionInstanceSliderButton(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
                }

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
    extends AbstractOptionSliderButton
    implements TooltipAccessor {
        private final OptionInstance<N> instance;
        private final SliderableValueSet<N> values;
        private final TooltipSupplier<N> tooltip;

        OptionInstanceSliderButton(Options options, int i, int j, int k, int l, OptionInstance<N> optionInstance, SliderableValueSet<N> sliderableValueSet, TooltipSupplier<N> tooltipSupplier) {
            super(options, i, j, k, l, sliderableValueSet.toSliderValue(optionInstance.get()));
            this.instance = optionInstance;
            this.values = sliderableValueSet;
            this.tooltip = tooltipSupplier;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            this.setMessage(this.instance.toString.apply(this.instance.get()));
        }

        @Override
        protected void applyValue() {
            this.instance.set(this.values.fromSliderValue(this.value));
            this.options.save();
        }

        @Override
        public List<FormattedCharSequence> getTooltip() {
            return (List)this.tooltip.apply(this.values.fromSliderValue(this.value));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec) implements ValueSet<T>
    {
        @Override
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> CycleButton.builder(optionInstance.toString).withValues((Collection)this.values.get()).withTooltip(tooltipSupplier).withInitialValue(optionInstance.value).create(i, j, k, 20, optionInstance.getCaption(), (cycleButton, object) -> {
                optionInstance.set(object);
                options.save();
            });
        }

        @Override
        public Optional<T> validateValue(T object) {
            return this.validateValue.apply(object);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record AltEnum<T>(List<T> values, List<T> altValues, BooleanSupplier altCondition, AltSetter<T> altSetter, Codec<T> codec) implements ValueSet<T>
    {
        @Override
        public Function<OptionInstance<T>, AbstractWidget> createButton(TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> CycleButton.builder(optionInstance.toString).withValues(this.altCondition, this.values, this.altValues).withTooltip(tooltipSupplier).withInitialValue(optionInstance.value).create(i, j, k, 20, optionInstance.getCaption(), (cycleButton, object) -> {
                this.altSetter.set((OptionInstance<Object>)optionInstance, object);
                options.save();
            });
        }

        @Override
        public Optional<T> validateValue(T object) {
            return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(object) ? Optional.of(object) : Optional.empty();
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface AltSetter<T> {
        public void set(OptionInstance<T> var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    static interface SliderableValueSet<T>
    extends ValueSet<T> {
        public double toSliderValue(T var1);

        public T fromSliderValue(double var1);
    }
}

