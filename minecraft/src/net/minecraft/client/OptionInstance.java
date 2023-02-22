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
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import org.slf4j.Logger;

@Environment(EnvType.CLIENT)
public final class OptionInstance<T> {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final OptionInstance.Enum<Boolean> BOOLEAN_VALUES = new OptionInstance.Enum<>(ImmutableList.of(Boolean.TRUE, Boolean.FALSE), Codec.BOOL);
	public static final OptionInstance.CaptionBasedToString<Boolean> BOOLEAN_TO_STRING = (component, boolean_) -> boolean_
			? CommonComponents.OPTION_ON
			: CommonComponents.OPTION_OFF;
	private final OptionInstance.TooltipSupplier<T> tooltip;
	final Function<T, Component> toString;
	private final OptionInstance.ValueSet<T> values;
	private final Codec<T> codec;
	private final T initialValue;
	private final Consumer<T> onValueUpdate;
	final Component caption;
	T value;

	public static OptionInstance<Boolean> createBoolean(String string, boolean bl, Consumer<Boolean> consumer) {
		return createBoolean(string, noTooltip(), bl, consumer);
	}

	public static OptionInstance<Boolean> createBoolean(String string, boolean bl) {
		return createBoolean(string, noTooltip(), bl, boolean_ -> {
		});
	}

	public static OptionInstance<Boolean> createBoolean(String string, OptionInstance.TooltipSupplier<Boolean> tooltipSupplier, boolean bl) {
		return createBoolean(string, tooltipSupplier, bl, boolean_ -> {
		});
	}

	public static OptionInstance<Boolean> createBoolean(
		String string, OptionInstance.TooltipSupplier<Boolean> tooltipSupplier, boolean bl, Consumer<Boolean> consumer
	) {
		return createBoolean(string, tooltipSupplier, BOOLEAN_TO_STRING, bl, consumer);
	}

	public static OptionInstance<Boolean> createBoolean(
		String string,
		OptionInstance.TooltipSupplier<Boolean> tooltipSupplier,
		OptionInstance.CaptionBasedToString<Boolean> captionBasedToString,
		boolean bl,
		Consumer<Boolean> consumer
	) {
		return new OptionInstance<>(string, tooltipSupplier, captionBasedToString, BOOLEAN_VALUES, bl, consumer);
	}

	public OptionInstance(
		String string,
		OptionInstance.TooltipSupplier<T> tooltipSupplier,
		OptionInstance.CaptionBasedToString<T> captionBasedToString,
		OptionInstance.ValueSet<T> valueSet,
		T object,
		Consumer<T> consumer
	) {
		this(string, tooltipSupplier, captionBasedToString, valueSet, valueSet.codec(), object, consumer);
	}

	public OptionInstance(
		String string,
		OptionInstance.TooltipSupplier<T> tooltipSupplier,
		OptionInstance.CaptionBasedToString<T> captionBasedToString,
		OptionInstance.ValueSet<T> valueSet,
		Codec<T> codec,
		T object,
		Consumer<T> consumer
	) {
		this.caption = Component.translatable(string);
		this.tooltip = tooltipSupplier;
		this.toString = objectx -> captionBasedToString.toString(this.caption, (T)objectx);
		this.values = valueSet;
		this.codec = codec;
		this.initialValue = object;
		this.onValueUpdate = consumer;
		this.value = this.initialValue;
	}

	public static <T> OptionInstance.TooltipSupplier<T> noTooltip() {
		return object -> null;
	}

	public static <T> OptionInstance.TooltipSupplier<T> cachedConstantTooltip(Component component) {
		return object -> Tooltip.create(component);
	}

	public static <T extends OptionEnum> OptionInstance.CaptionBasedToString<T> forOptionEnum() {
		return (component, optionEnum) -> optionEnum.getCaption();
	}

	public AbstractWidget createButton(Options options, int i, int j, int k) {
		return this.createButton(options, i, j, k, object -> {
		});
	}

	public AbstractWidget createButton(Options options, int i, int j, int k, Consumer<T> consumer) {
		return (AbstractWidget)this.values.createButton(this.tooltip, options, i, j, k, consumer).apply(this);
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
		T object2 = (T)this.values.validateValue(object).orElseGet(() -> {
			LOGGER.error("Illegal option value " + object + " for " + this.caption);
			return this.initialValue;
		});
		if (!Minecraft.getInstance().isRunning()) {
			this.value = object2;
		} else {
			if (!Objects.equals(this.value, object2)) {
				this.value = object2;
				this.onValueUpdate.accept(this.value);
			}
		}
	}

	public OptionInstance.ValueSet<T> values() {
		return this.values;
	}

	@Environment(EnvType.CLIENT)
	public static record AltEnum<T>(
		List<T> values, List<T> altValues, BooleanSupplier altCondition, OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter, Codec<T> codec
	) implements OptionInstance.CycleableValueSet<T> {
		@Override
		public CycleButton.ValueListSupplier<T> valueListSupplier() {
			return CycleButton.ValueListSupplier.create(this.altCondition, this.values, this.altValues);
		}

		@Override
		public Optional<T> validateValue(T object) {
			return (this.altCondition.getAsBoolean() ? this.altValues : this.values).contains(object) ? Optional.of(object) : Optional.empty();
		}
	}

	@Environment(EnvType.CLIENT)
	public interface CaptionBasedToString<T> {
		Component toString(Component component, T object);
	}

	@Environment(EnvType.CLIENT)
	public static record ClampingLazyMaxIntRange(int minInclusive, IntSupplier maxSupplier)
		implements OptionInstance.IntRangeBase,
		OptionInstance.SliderableOrCyclableValueSet<Integer> {
		public Optional<Integer> validateValue(Integer integer) {
			return Optional.of(Mth.clamp(integer, this.minInclusive(), this.maxInclusive()));
		}

		@Override
		public int maxInclusive() {
			return this.maxSupplier.getAsInt();
		}

		@Override
		public Codec<Integer> codec() {
			return ExtraCodecs.validate(
				Codec.INT,
				integer -> {
					int i = this.maxSupplier.getAsInt() + 1;
					return integer.compareTo(this.minInclusive) >= 0 && integer.compareTo(i) <= 0
						? DataResult.success(integer)
						: DataResult.error(() -> "Value " + integer + " outside of range [" + this.minInclusive + ":" + i + "]", integer);
				}
			);
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

	@Environment(EnvType.CLIENT)
	interface CycleableValueSet<T> extends OptionInstance.ValueSet<T> {
		CycleButton.ValueListSupplier<T> valueListSupplier();

		default OptionInstance.CycleableValueSet.ValueSetter<T> valueSetter() {
			return OptionInstance::set;
		}

		@Override
		default Function<OptionInstance<T>, AbstractWidget> createButton(
			OptionInstance.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer
		) {
			return optionInstance -> CycleButton.<T>builder(optionInstance.toString)
					.withValues(this.valueListSupplier())
					.withTooltip(tooltipSupplier)
					.withInitialValue(optionInstance.value)
					.create(i, j, k, 20, optionInstance.caption, (cycleButton, object) -> {
						this.valueSetter().set(optionInstance, object);
						options.save();
						consumer.accept(object);
					});
		}

		@Environment(EnvType.CLIENT)
		public interface ValueSetter<T> {
			void set(OptionInstance<T> optionInstance, T object);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record Enum<T>(List<T> values, Codec<T> codec) implements OptionInstance.CycleableValueSet<T> {
		@Override
		public Optional<T> validateValue(T object) {
			return this.values.contains(object) ? Optional.of(object) : Optional.empty();
		}

		@Override
		public CycleButton.ValueListSupplier<T> valueListSupplier() {
			return CycleButton.ValueListSupplier.create(this.values);
		}
	}

	@Environment(EnvType.CLIENT)
	public static record IntRange(int minInclusive, int maxInclusive) implements OptionInstance.IntRangeBase {
		public Optional<Integer> validateValue(Integer integer) {
			return integer.compareTo(this.minInclusive()) >= 0 && integer.compareTo(this.maxInclusive()) <= 0 ? Optional.of(integer) : Optional.empty();
		}

		@Override
		public Codec<Integer> codec() {
			return Codec.intRange(this.minInclusive, this.maxInclusive + 1);
		}
	}

	@Environment(EnvType.CLIENT)
	interface IntRangeBase extends OptionInstance.SliderableValueSet<Integer> {
		int minInclusive();

		int maxInclusive();

		default double toSliderValue(Integer integer) {
			return (double)Mth.map((float)integer.intValue(), (float)this.minInclusive(), (float)this.maxInclusive(), 0.0F, 1.0F);
		}

		default Integer fromSliderValue(double d) {
			return Mth.floor(Mth.map(d, 0.0, 1.0, (double)this.minInclusive(), (double)this.maxInclusive()));
		}

		default <R> OptionInstance.SliderableValueSet<R> xmap(IntFunction<? extends R> intFunction, ToIntFunction<? super R> toIntFunction) {
			return new OptionInstance.SliderableValueSet<R>() {
				@Override
				public Optional<R> validateValue(R object) {
					return IntRangeBase.this.validateValue(Integer.valueOf(toIntFunction.applyAsInt(object))).map(intFunction::apply);
				}

				@Override
				public double toSliderValue(R object) {
					return IntRangeBase.this.toSliderValue(toIntFunction.applyAsInt(object));
				}

				@Override
				public R fromSliderValue(double d) {
					return (R)intFunction.apply(IntRangeBase.this.fromSliderValue(d));
				}

				@Override
				public Codec<R> codec() {
					return IntRangeBase.this.codec().xmap(intFunction::apply, toIntFunction::applyAsInt);
				}
			};
		}
	}

	@Environment(EnvType.CLIENT)
	public static record LazyEnum<T>(Supplier<List<T>> values, Function<T, Optional<T>> validateValue, Codec<T> codec)
		implements OptionInstance.CycleableValueSet<T> {
		@Override
		public Optional<T> validateValue(T object) {
			return (Optional<T>)this.validateValue.apply(object);
		}

		@Override
		public CycleButton.ValueListSupplier<T> valueListSupplier() {
			return CycleButton.ValueListSupplier.create((Collection<T>)this.values.get());
		}
	}

	@Environment(EnvType.CLIENT)
	static final class OptionInstanceSliderButton<N> extends AbstractOptionSliderButton {
		private final OptionInstance<N> instance;
		private final OptionInstance.SliderableValueSet<N> values;
		private final OptionInstance.TooltipSupplier<N> tooltipSupplier;
		private final Consumer<N> onValueChanged;

		OptionInstanceSliderButton(
			Options options,
			int i,
			int j,
			int k,
			int l,
			OptionInstance<N> optionInstance,
			OptionInstance.SliderableValueSet<N> sliderableValueSet,
			OptionInstance.TooltipSupplier<N> tooltipSupplier,
			Consumer<N> consumer
		) {
			super(options, i, j, k, l, sliderableValueSet.toSliderValue(optionInstance.get()));
			this.instance = optionInstance;
			this.values = sliderableValueSet;
			this.tooltipSupplier = tooltipSupplier;
			this.onValueChanged = consumer;
			this.updateMessage();
		}

		@Override
		protected void updateMessage() {
			this.setMessage((Component)this.instance.toString.apply(this.instance.get()));
			this.setTooltip(this.tooltipSupplier.apply(this.values.fromSliderValue(this.value)));
		}

		@Override
		protected void applyValue() {
			this.instance.set(this.values.fromSliderValue(this.value));
			this.options.save();
			this.onValueChanged.accept(this.instance.get());
		}
	}

	@Environment(EnvType.CLIENT)
	interface SliderableOrCyclableValueSet<T> extends OptionInstance.CycleableValueSet<T>, OptionInstance.SliderableValueSet<T> {
		boolean createCycleButton();

		@Override
		default Function<OptionInstance<T>, AbstractWidget> createButton(
			OptionInstance.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer
		) {
			return this.createCycleButton()
				? OptionInstance.CycleableValueSet.super.createButton(tooltipSupplier, options, i, j, k, consumer)
				: OptionInstance.SliderableValueSet.super.createButton(tooltipSupplier, options, i, j, k, consumer);
		}
	}

	@Environment(EnvType.CLIENT)
	interface SliderableValueSet<T> extends OptionInstance.ValueSet<T> {
		double toSliderValue(T object);

		T fromSliderValue(double d);

		@Override
		default Function<OptionInstance<T>, AbstractWidget> createButton(
			OptionInstance.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer
		) {
			return optionInstance -> new OptionInstance.OptionInstanceSliderButton<>(options, i, j, k, 20, optionInstance, this, tooltipSupplier, consumer);
		}
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface TooltipSupplier<T> {
		@Nullable
		Tooltip apply(T object);
	}

	@Environment(EnvType.CLIENT)
	public static enum UnitDouble implements OptionInstance.SliderableValueSet<Double> {
		INSTANCE;

		public Optional<Double> validateValue(Double double_) {
			return double_ >= 0.0 && double_ <= 1.0 ? Optional.of(double_) : Optional.empty();
		}

		public double toSliderValue(Double double_) {
			return double_;
		}

		public Double fromSliderValue(double d) {
			return d;
		}

		public <R> OptionInstance.SliderableValueSet<R> xmap(DoubleFunction<? extends R> doubleFunction, ToDoubleFunction<? super R> toDoubleFunction) {
			return new OptionInstance.SliderableValueSet<R>() {
				@Override
				public Optional<R> validateValue(R object) {
					return UnitDouble.this.validateValue(toDoubleFunction.applyAsDouble(object)).map(doubleFunction::apply);
				}

				@Override
				public double toSliderValue(R object) {
					return UnitDouble.this.toSliderValue(toDoubleFunction.applyAsDouble(object));
				}

				@Override
				public R fromSliderValue(double d) {
					return (R)doubleFunction.apply(UnitDouble.this.fromSliderValue(d));
				}

				@Override
				public Codec<R> codec() {
					return UnitDouble.this.codec().xmap(doubleFunction::apply, toDoubleFunction::applyAsDouble);
				}
			};
		}

		@Override
		public Codec<Double> codec() {
			return Codec.either(Codec.doubleRange(0.0, 1.0), Codec.BOOL).xmap(either -> either.map(double_ -> double_, boolean_ -> boolean_ ? 1.0 : 0.0), Either::left);
		}
	}

	@Environment(EnvType.CLIENT)
	interface ValueSet<T> {
		Function<OptionInstance<T>, AbstractWidget> createButton(
			OptionInstance.TooltipSupplier<T> tooltipSupplier, Options options, int i, int j, int k, Consumer<T> consumer
		);

		Optional<T> validateValue(T object);

		Codec<T> codec();
	}
}
