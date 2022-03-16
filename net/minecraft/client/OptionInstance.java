/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class OptionInstance<T>
extends Option {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Function<Minecraft, Option.TooltipSupplier<T>> tooltip;
    final Function<T, Component> toString;
    private final ValueSet<T> values;
    private final T initialValue;
    private final Consumer<T> onValueUpdate;
    T value;

    public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
        return OptionInstance.createBoolean(string, OptionInstance.noTooltip(), bl, consumer);
    }

    public static OptionInstance<Boolean> createBoolean(String string, Function<Minecraft, Option.TooltipSupplier<Boolean>> function, boolean bl) {
        return OptionInstance.createBoolean(string, function, bl, boolean_ -> {});
    }

    public static OptionInstance<Boolean> createBoolean(String string, Function<Minecraft, Option.TooltipSupplier<Boolean>> function, boolean bl, Consumer<Boolean> consumer) {
        return new OptionInstance<Boolean>(string, function, boolean_ -> boolean_ != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF, new Enum<Boolean>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE)), bl, consumer);
    }

    public OptionInstance(String string, Function<Minecraft, Option.TooltipSupplier<T>> function, Function<T, Component> function2, ValueSet<T> valueSet, T object, Consumer<T> consumer) {
        super(string);
        this.tooltip = function;
        this.toString = function2;
        this.values = valueSet;
        this.initialValue = object;
        this.onValueUpdate = consumer;
        this.value = this.initialValue;
    }

    @Override
    public AbstractWidget createButton(Options options, int i, int j, int k) {
        Option.TooltipSupplier<T> tooltipSupplier = this.tooltip.apply(Minecraft.getInstance());
        return this.values.createButton(tooltipSupplier, options, i, j, k).apply(this);
    }

    public T get() {
        return this.value;
    }

    public void set(T object) {
        if (!this.values.validValue(object)) {
            LOGGER.error("Illegal option value " + object + " for " + this.getCaption());
            this.value = this.initialValue;
        }
        if (!Minecraft.getInstance().isRunning()) {
            this.value = object;
            return;
        }
        if (!Objects.equals(this.value, object)) {
            this.value = object;
            this.onValueUpdate.accept(this.value);
        }
    }

    public ValueSet<T> values() {
        return this.values;
    }

    @Environment(value=EnvType.CLIENT)
    public record Enum<T>(List<T> values) implements ValueSet<T>
    {
        @Override
        public Function<OptionInstance<T>, AbstractWidget> createButton(Option.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> CycleButton.builder(optionInstance.toString).withValues((Collection<T>)this.values).withTooltip(tooltipSupplier).withInitialValue(optionInstance.value).create(i, j, k, 20, optionInstance.getCaption(), (cycleButton, object) -> {
                optionInstance.set(object);
                options.save();
            });
        }

        @Override
        public boolean validValue(T object) {
            return this.values.contains(object);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static interface ValueSet<T> {
        public Function<OptionInstance<T>, AbstractWidget> createButton(Option.TooltipSupplier<T> var1, Options var2, int var3, int var4, int var5);

        public boolean validValue(T var1);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum UnitDouble implements SliderableValueSet<Double>
    {
        INSTANCE;


        @Override
        public Function<OptionInstance<Double>, AbstractWidget> createButton(Option.TooltipSupplier<Double> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> new OptionInstanceSliderButton<Double>(options, i, j, k, 20, (OptionInstance<Double>)optionInstance, this, tooltipSupplier);
        }

        @Override
        public boolean validValue(Double double_) {
            return double_ >= 0.0 && double_ <= 1.0;
        }

        @Override
        public double toSliderValue(Double double_) {
            return double_;
        }

        @Override
        public Double fromSliderValue(double d) {
            return d;
        }

        @Override
        public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }
    }

    @Environment(value=EnvType.CLIENT)
    public record IntRange(int minInclusive, int maxInclusive) implements SliderableValueSet<Integer>
    {
        @Override
        public Function<OptionInstance<Integer>, AbstractWidget> createButton(Option.TooltipSupplier<Integer> tooltipSupplier, Options options, int i, int j, int k) {
            return optionInstance -> new OptionInstanceSliderButton<Integer>(options, i, j, k, 20, (OptionInstance<Integer>)optionInstance, this, tooltipSupplier);
        }

        @Override
        public boolean validValue(Integer integer) {
            return integer.compareTo(this.minInclusive) >= 0 && integer.compareTo(this.maxInclusive) <= 0;
        }

        @Override
        public double toSliderValue(Integer integer) {
            return Mth.map(integer.intValue(), this.minInclusive, this.maxInclusive, 0.0f, 1.0f);
        }

        @Override
        public Integer fromSliderValue(double d) {
            return Mth.floor(Mth.map(d, 0.0, 1.0, (double)this.minInclusive, (double)this.maxInclusive));
        }

        public <R> SliderableValueSet<R> xmap(final IntFunction<? extends R> intFunction, final ToIntFunction<? super R> toIntFunction) {
            return new SliderableValueSet<R>(){

                @Override
                public Function<OptionInstance<R>, AbstractWidget> createButton(Option.TooltipSupplier<R> tooltipSupplier, Options options, int i, int j, int k) {
                    return optionInstance -> new OptionInstanceSliderButton(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
                }

                @Override
                public boolean validValue(R object) {
                    return this.validValue(toIntFunction.applyAsInt(object));
                }

                @Override
                public double toSliderValue(R object) {
                    return this.toSliderValue(toIntFunction.applyAsInt(object));
                }

                @Override
                public R fromSliderValue(double d) {
                    return intFunction.apply(this.fromSliderValue(d));
                }
            };
        }

        @Override
        public /* synthetic */ Object fromSliderValue(double d) {
            return this.fromSliderValue(d);
        }
    }

    @Environment(value=EnvType.CLIENT)
    static final class OptionInstanceSliderButton<N>
    extends AbstractOptionSliderButton
    implements TooltipAccessor {
        private final OptionInstance<N> instance;
        private final SliderableValueSet<N> values;
        private final Option.TooltipSupplier<N> tooltip;

        OptionInstanceSliderButton(Options options, int i, int j, int k, int l, OptionInstance<N> optionInstance, SliderableValueSet<N> sliderableValueSet, Option.TooltipSupplier<N> tooltipSupplier) {
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
    static interface SliderableValueSet<T>
    extends ValueSet<T> {
        public double toSliderValue(T var1);

        public T fromSliderValue(double var1);
    }
}

