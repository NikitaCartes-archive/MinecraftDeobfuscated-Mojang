package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class CycleButton<T> extends AbstractButton implements TooltipAccessor {
	static final BooleanSupplier DEFAULT_ALT_LIST_SELECTOR = Screen::hasAltDown;
	private static final List<Boolean> BOOLEAN_OPTIONS = ImmutableList.of(Boolean.TRUE, Boolean.FALSE);
	private final Component name;
	private int index;
	private T value;
	private final CycleButton.ValueListSupplier<T> values;
	private final Function<T, Component> valueStringifier;
	private final Function<CycleButton<T>, MutableComponent> narrationProvider;
	private final CycleButton.OnValueChange<T> onValueChange;
	private final Option.TooltipSupplier<T> tooltipSupplier;
	private final boolean displayOnlyValue;

	CycleButton(
		int i,
		int j,
		int k,
		int l,
		Component component,
		Component component2,
		int m,
		T object,
		CycleButton.ValueListSupplier<T> valueListSupplier,
		Function<T, Component> function,
		Function<CycleButton<T>, MutableComponent> function2,
		CycleButton.OnValueChange<T> onValueChange,
		Option.TooltipSupplier<T> tooltipSupplier,
		boolean bl
	) {
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
		T object = (T)list.get(this.index);
		this.updateValue(object);
		this.onValueChange.onValueChange(this, object);
	}

	private T getCycledValue(int i) {
		List<T> list = this.values.getSelectedList();
		return (T)list.get(Mth.positiveModulo(this.index + i, list.size()));
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
		return (Component)(this.displayOnlyValue ? (Component)this.valueStringifier.apply(object) : this.createFullName(object));
	}

	private MutableComponent createFullName(T object) {
		return CommonComponents.optionNameValue(this.name, (Component)this.valueStringifier.apply(object));
	}

	public T getValue() {
		return this.value;
	}

	@Override
	protected MutableComponent createNarrationMessage() {
		return (MutableComponent)this.narrationProvider.apply(this);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
		if (this.active) {
			T object = this.getCycledValue(1);
			Component component = this.createLabelForValue(object);
			if (this.isFocused()) {
				narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.cycle_button.usage.focused", component));
			} else {
				narrationElementOutput.add(NarratedElementType.USAGE, new TranslatableComponent("narration.cycle_button.usage.hovered", component));
			}
		}
	}

	public MutableComponent createDefaultNarrationMessage() {
		return wrapDefaultNarrationMessage((Component)(this.displayOnlyValue ? this.createFullName(this.value) : this.getMessage()));
	}

	@Override
	public List<FormattedCharSequence> getTooltip() {
		return (List<FormattedCharSequence>)this.tooltipSupplier.apply(this.value);
	}

	public static <T> CycleButton.Builder<T> builder(Function<T, Component> function) {
		return new CycleButton.Builder<>(function);
	}

	public static CycleButton.Builder<Boolean> booleanBuilder(Component component, Component component2) {
		return new CycleButton.Builder<Boolean>(boolean_ -> boolean_ ? component : component2).withValues(BOOLEAN_OPTIONS);
	}

	public static CycleButton.Builder<Boolean> onOffBuilder() {
		return new CycleButton.Builder<Boolean>(boolean_ -> boolean_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF).withValues(BOOLEAN_OPTIONS);
	}

	public static CycleButton.Builder<Boolean> onOffBuilder(boolean bl) {
		return onOffBuilder().withInitialValue(bl);
	}

	@Environment(EnvType.CLIENT)
	public static class Builder<T> {
		private int initialIndex;
		@Nullable
		private T initialValue;
		private final Function<T, Component> valueStringifier;
		private Option.TooltipSupplier<T> tooltipSupplier = object -> ImmutableList.of();
		private Function<CycleButton<T>, MutableComponent> narrationProvider = CycleButton::createDefaultNarrationMessage;
		private CycleButton.ValueListSupplier<T> values = CycleButton.ValueListSupplier.create(ImmutableList.<T>of());
		private boolean displayOnlyValue;

		public Builder(Function<T, Component> function) {
			this.valueStringifier = function;
		}

		public CycleButton.Builder<T> withValues(Collection<T> collection) {
			this.values = CycleButton.ValueListSupplier.create(collection);
			return this;
		}

		@SafeVarargs
		public final CycleButton.Builder<T> withValues(T... objects) {
			return this.withValues(ImmutableList.<T>copyOf(objects));
		}

		public CycleButton.Builder<T> withValues(List<T> list, List<T> list2) {
			this.values = CycleButton.ValueListSupplier.create(CycleButton.DEFAULT_ALT_LIST_SELECTOR, list, list2);
			return this;
		}

		public CycleButton.Builder<T> withValues(BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
			this.values = CycleButton.ValueListSupplier.create(booleanSupplier, list, list2);
			return this;
		}

		public CycleButton.Builder<T> withTooltip(Option.TooltipSupplier<T> tooltipSupplier) {
			this.tooltipSupplier = tooltipSupplier;
			return this;
		}

		public CycleButton.Builder<T> withInitialValue(T object) {
			this.initialValue = object;
			int i = this.values.getDefaultList().indexOf(object);
			if (i != -1) {
				this.initialIndex = i;
			}

			return this;
		}

		public CycleButton.Builder<T> withCustomNarration(Function<CycleButton<T>, MutableComponent> function) {
			this.narrationProvider = function;
			return this;
		}

		public CycleButton.Builder<T> displayOnlyValue() {
			this.displayOnlyValue = true;
			return this;
		}

		public CycleButton<T> create(int i, int j, int k, int l, Component component) {
			return this.create(i, j, k, l, component, (cycleButton, object) -> {
			});
		}

		public CycleButton<T> create(int i, int j, int k, int l, Component component, CycleButton.OnValueChange<T> onValueChange) {
			List<T> list = this.values.getDefaultList();
			if (list.isEmpty()) {
				throw new IllegalStateException("No values for cycle button");
			} else {
				T object = (T)(this.initialValue != null ? this.initialValue : list.get(this.initialIndex));
				Component component2 = (Component)this.valueStringifier.apply(object);
				Component component3 = (Component)(this.displayOnlyValue ? component2 : CommonComponents.optionNameValue(component, component2));
				return new CycleButton<>(
					i,
					j,
					k,
					l,
					component3,
					component,
					this.initialIndex,
					object,
					this.values,
					this.valueStringifier,
					this.narrationProvider,
					onValueChange,
					this.tooltipSupplier,
					this.displayOnlyValue
				);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public interface OnValueChange<T> {
		void onValueChange(CycleButton<T> cycleButton, T object);
	}

	@Environment(EnvType.CLIENT)
	interface ValueListSupplier<T> {
		List<T> getSelectedList();

		List<T> getDefaultList();

		static <T> CycleButton.ValueListSupplier<T> create(Collection<T> collection) {
			final List<T> list = ImmutableList.copyOf(collection);
			return new CycleButton.ValueListSupplier<T>() {
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

		static <T> CycleButton.ValueListSupplier<T> create(BooleanSupplier booleanSupplier, List<T> list, List<T> list2) {
			final List<T> list3 = ImmutableList.copyOf(list);
			final List<T> list4 = ImmutableList.copyOf(list2);
			return new CycleButton.ValueListSupplier<T>() {
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
}
