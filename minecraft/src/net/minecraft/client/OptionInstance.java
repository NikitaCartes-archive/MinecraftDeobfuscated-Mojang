package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public class OptionInstance<T> extends Option {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Function<Minecraft, Option.TooltipSupplier<T>> tooltip;
	final Function<T, Component> toString;
	private final OptionInstance.ValueSet<T> values;
	private final T initialValue;
	private final Consumer<T> onValueUpdate;
	T value;

	public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
		return createBoolean(string, noTooltip(), bl, consumer);
	}

	public static OptionInstance<Boolean> createBoolean(String string, Function<Minecraft, Option.TooltipSupplier<Boolean>> function, boolean bl) {
		return createBoolean(string, function, bl, boolean_ -> {
		});
	}

	public static OptionInstance<Boolean> createBoolean(
		String string, Function<Minecraft, Option.TooltipSupplier<Boolean>> function, boolean bl, Consumer<Boolean> consumer
	) {
		return new OptionInstance<>(
			string,
			function,
			boolean_ -> boolean_ ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF,
			new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE)),
			bl,
			consumer
		);
	}

	public OptionInstance(
		String string,
		Function<Minecraft, Option.TooltipSupplier<T>> function,
		Function<T, Component> function2,
		OptionInstance.ValueSet<T> valueSet,
		T object,
		Consumer<T> consumer
	) {
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
		Option.TooltipSupplier<T> tooltipSupplier = (Option.TooltipSupplier<T>)this.tooltip.apply(Minecraft.getInstance());
		return (AbstractWidget)this.values.createButton(tooltipSupplier, options, i, j, k).apply(this);
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
		} else {
			if (!Objects.equals(this.value, object)) {
				this.value = object;
				this.onValueUpdate.accept(this.value);
			}
		}
	}

	public OptionInstance.ValueSet<T> values() {
		return this.values;
	}

	@Environment(EnvType.CLIENT)
	public static record Enum<T>(List<T> values) implements OptionInstance.ValueSet<T> {
		@Override
		public Function<OptionInstance<T>, AbstractWidget> createButton(Option.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k) {
			return optionInstance -> CycleButton.<T>builder(optionInstance.toString)
					.withValues(this.values)
					.withTooltip(tooltipSupplier)
					.withInitialValue(optionInstance.value)
					.create(i, j, k, 20, optionInstance.getCaption(), (cycleButton, object) -> {
						optionInstance.set(object);
						options.save();
					});
		}

		@Override
		public boolean validValue(T object) {
			return this.values.contains(object);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.SliderableValueSet<Integer> {
		@Override
		public Function<OptionInstance<Integer>, AbstractWidget> createButton(Option.TooltipSupplier<Integer> tooltipSupplier, Options options, int i, int j, int k) {
			return optionInstance -> new OptionInstance.OptionInstanceSliderButton<>(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
		}

		public boolean validValue(Integer integer) {
			return integer.compareTo(this.minInclusive) >= 0 && integer.compareTo(this.maxInclusive) <= 0;
		}

		public double toSliderValue(Integer integer) {
			return (double)Mth.map((float)integer.intValue(), (float)this.minInclusive, (float)this.maxInclusive, 0.0F, 1.0F);
		}

		public Integer fromSliderValue(double d) {
			return Mth.floor(Mth.map(d, 0.0, 1.0, (double)this.minInclusive, (double)this.maxInclusive));
		}

		public <R> OptionInstance.SliderableValueSet<R> xmap(IntFunction<? extends R> intFunction, ToIntFunction<? super R> toIntFunction) {
			return new OptionInstance.SliderableValueSet<R>() {
				@Override
				public Function<OptionInstance<R>, AbstractWidget> createButton(Option.TooltipSupplier<R> tooltipSupplier, Options options, int i, int j, int k) {
					return optionInstance -> new OptionInstance.OptionInstanceSliderButton<>(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
				}

				@Override
				public boolean validValue(R object) {
					return IntRange.this.validValue(toIntFunction.applyAsInt(object));
				}

				@Override
				public double toSliderValue(R object) {
					return IntRange.this.toSliderValue(toIntFunction.applyAsInt(object));
				}

				@Override
				public R fromSliderValue(double d) {
					return (R)intFunction.apply(IntRange.this.fromSliderValue(d));
				}
			};
		}
	}

	@Environment(EnvType.CLIENT)
	static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton implements TooltipAccessor {
		private final OptionInstance<N> instance;
		private final OptionInstance.SliderableValueSet<N> values;
		private final Option.TooltipSupplier<N> tooltip;

		OptionInstanceSliderButton(
			Options options,
			int i,
			int j,
			int k,
			int l,
			OptionInstance<N> optionInstance,
			OptionInstance.SliderableValueSet<N> sliderableValueSet,
			Option.TooltipSupplier<N> tooltipSupplier
		) {
			super(options, i, j, k, l, sliderableValueSet.toSliderValue(optionInstance.get()));
			this.instance = optionInstance;
			this.values = sliderableValueSet;
			this.tooltip = tooltipSupplier;
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage((Component)this.instance.toString.apply(this.instance.get()));
		}

		@Override
		protected void applyValue() {
			this.instance.set(this.values.fromSliderValue(this.value));
			this.options.save();
		}

		@Override
		public List<FormattedCharSequence> getTooltip() {
			return (List<FormattedCharSequence>)this.tooltip.apply(this.values.fromSliderValue(this.value));
		}
	}

	@Environment(EnvType.CLIENT)
	interface SliderableValueSet<T> extends OptionInstance.ValueSet<T> {
		double toSliderValue(T object);

		T fromSliderValue(double d);
	}

	@Environment(EnvType.CLIENT)
	public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double> {
		INSTANCE;

		@Override
		public Function<OptionInstance<Double>, AbstractWidget> createButton(Option.TooltipSupplier<Double> tooltipSupplier, Options options, int i, int j, int k) {
			return optionInstance -> new OptionInstance.OptionInstanceSliderButton<>(options, i, j, k, 20, optionInstance, this, tooltipSupplier);
		}

		public boolean validValue(Double double_) {
			return double_ >= 0.0 && double_ <= 1.0;
		}

		public double toSliderValue(Double double_) {
			return double_;
		}

		public Double fromSliderValue(double d) {
			return d;
		}
	}

	@Environment(EnvType.CLIENT)
	interface ValueSet<T> {
		Function<OptionInstance<T>, AbstractWidget> createButton(Option.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k);

		boolean validValue(T object);
	}
}
