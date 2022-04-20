/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class CycleButton<T>
extends AbstractButton
implements TooltipAccessor {
    static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
    private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
    private final Component name;
    private int index;
    private T value;
    private final ValueListSupplier<T> values;
    private final Function<T, Component> valueStringifier;
    private final Function<CycleButton<T>, MutableComponent> narrationProvider;
    private final OnValueChange<T> onValueChange;
    private final OptionInstance.TooltipSupplier<T> tooltipSupplier;
    private final boolean displayOnlyValue;

    CycleButton(int i, int j, int k, int l, Component component, Component component2, int m, T object, ValueListSupplier<T> valueListSupplier, Function<T, Component> function, Function<CycleButton<T>, MutableComponent> function2, OnValueChange<T> onValueChange, OptionInstance.TooltipSupplier<T> tooltipSupplier, boolean bl) {
        super(i, j, k, l, component);
        this.name = component2;
        this.index = m;
        this.value = object;
        this.values = valueListSupplier;
        this.valueStringifier = function;
        this.narrationProvider = function2;
        this.onValueChange = onValueChange;
        this.tooltipSupplier = tooltipSupplier;
        this.displayOnlyValue = bl;
    }

    @Override
    public void onPress() {
        if (Screen.hasShiftDown()) {
            this.cycleValue(-1);
        } else {
            this.cycleValue(1);
        }
    }

    private void cycleValue(int i) {
        List<T> list = this.values.getSelectedList();
        this.index = Mth.positiveModulo(this.index + i, list.size());
        T object = list.get(this.index);
        this.updateValue(object);
        this.onValueChange.onValueChange(this, object);
    }

    private T getCycledValue(int i) {
        List<T> list = this.values.getSelectedList();
        return list.get(Mth.positiveModulo(this.index + i, list.size()));
    }

    @Override
    public boolean mouseScrolled(double d, double e, double f) {
        if (f > 0.0) {
            this.cycleValue(-1);
        } else if (f < 0.0) {
            this.cycleValue(1);
        }
        return true;
    }

    public void setValue(T object) {
        List<T> list = this.values.getSelectedList();
        int i = list.indexOf(object);
        if (i != -1) {
            this.index = i;
        }
        this.updateValue(object);
    }

    private void updateValue(T object) {
        Component component = this.createLabelForValue(object);
        this.setMessage(component);
        this.value = object;
    }

    private Component createLabelForValue(T object) {
        return this.displayOnlyValue ? this.valueStringifier.apply(object) : this.createFullName(object);
    }

    private MutableComponent createFullName(T object) {
        return CommonComponents.optionNameValue(this.name, this.valueStringifier.apply(object));
    }

    public T getValue() {
        return this.value;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return this.narrationProvider.apply(this);
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            T object = this.getCycledValue(1);
            Component component = this.createLabelForValue(object);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }
    }

    public MutableComponent createDefaultNarrationMessage() {
        return CycleButton.wrapDefaultNarrationMessage(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage());
    }

    @Override
    public List<FormattedCharSequence> getTooltip() {
        return (List)this.tooltipSupplier.apply(this.value);
    }

    public static <T> Builder<T> builder(Function<T, Component> function) {
        return new Builder<T>(function);
    }

    public static Builder<Boolean> booleanBuilder(Component component, Component component2) {
        return new Builder<Boolean>(boolean_ -> boolean_ != false ? component : component2).withValues((Collection<Boolean>)BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder() {
        return new Builder<Boolean>(boolean_ -> boolean_ != false ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).withValues((Collection<Boolean>)BOOLEAN_OPTIONS);
    }

    public static Builder<Boolean> onOffBuilder(boolean bl) {
        return CycleButton.onOffBuilder().withInitialValue(bl);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface ValueListSupplier<T> {
        public List<T> getSelectedList();

        public List<T> getDefaultList();

        public static <T> ValueListSupplier<T> create(Collection<T> collection) {
            final ImmutableList<T> list = ImmutableList.copyOf(collection);
            return new ValueListSupplier<T>(){

                @Override
                public List<T> getSelectedList() {
                    return list;
                }

                @Override
                public List<T> getDefaultList() {
                    return list;
                }
            };
        }

        public static <T> ValueListSupplier<T> create(final BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
            final ImmutableList<T> list3 = ImmutableList.copyOf(list);
            final ImmutableList<T> list4 = ImmutableList.copyOf(list2);
            return new ValueListSupplier<T>(){

                @Override
                public List<T> getSelectedList() {
                    return booleanSupplier.getAsBoolean() ? list4 : list3;
                }

                @Override
                public List<T> getDefaultList() {
                    return list3;
                }
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static interface OnValueChange<T> {
        public void onValueChange(CycleButton<T> var1, T var2);
    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder<T> {
        private int initialIndex;
        @Nullable
        private T initialValue;
        private final Function<T, Component> valueStringifier;
        private OptionInstance.TooltipSupplier<T> tooltipSupplier = object -> ImmutableList.of();
        private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
        private ValueListSupplier<T> values = ValueListSupplier.create(ImmutableList.of());
        private boolean displayOnlyValue;

        public Builder(Function<T, Component> function) {
            this.valueStringifier = function;
        }

        public Builder<T> withValues(Collection<T> collection) {
            return this.withValues(ValueListSupplier.create(collection));
        }

        @SafeVarargs
        public final Builder<T> withValues(T ... objects) {
            return this.withValues((Collection<T>)ImmutableList.copyOf(objects));
        }

        public Builder<T> withValues(List<T> list, List<T> list2) {
            return this.withValues(ValueListSupplier.create(DEFAULT_ALT_LIST_SELECTOR, list, list2));
        }

        public Builder<T> withValues(BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
            return this.withValues(ValueListSupplier.create(booleanSupplier, list, list2));
        }

        public Builder<T> withValues(ValueListSupplier<T> valueListSupplier) {
            this.values = valueListSupplier;
            return this;
        }

        public Builder<T> withTooltip(OptionInstance.TooltipSupplier<T> tooltipSupplier) {
            this.tooltipSupplier = tooltipSupplier;
            return this;
        }

        public Builder<T> withInitialValue(T object) {
            this.initialValue = object;
            int i = this.values.getDefaultList().indexOf(object);
            if (i != -1) {
                this.initialIndex = i;
            }
            return this;
        }

        public Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> function) {
            this.narrationProvider = function;
            return this;
        }

        public Builder<T> displayOnlyValue() {
            this.displayOnlyValue = true;
            return this;
        }

        public CycleButton<T> create(int i, int j, int k, int l, Component component) {
            return this.create(i, j, k, l, component, (cycleButton, object) -> {});
        }

        public CycleButton<T> create(int i, int j, int k, int l, Component component, OnValueChange<T> onValueChange) {
            List<T> list = this.values.getDefaultList();
            if (list.isEmpty()) {
                throw new IllegalStateException("No values for cycle button");
            }
            T object = this.initialValue != null ? this.initialValue : list.get(this.initialIndex);
            Component component2 = this.valueStringifier.apply(object);
            Component component3 = this.displayOnlyValue ? component2 : CommonComponents.optionNameValue(component, component2);
            return new CycleButton<T>(i, j, k, l, component3, component, this.initialIndex, object, this.values, this.valueStringifier, this.narrationProvider, onValueChange, this.tooltipSupplier, this.displayOnlyValue);
        }
    }
}

