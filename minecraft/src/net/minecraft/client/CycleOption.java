package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class CycleOption<T> extends Option {
	private final CycleOption.OptionSetter<T> setter;
	private final Function<Options, T> getter;
	private final Supplier<CycleButton.Builder<T>> buttonSetup;
	private Function<Minecraft, CycleButton.TooltipSupplier<T>> tooltip = minecraft -> object -> ImmutableList.of();

	private CycleOption(String string, Function<Options, T> function, CycleOption.OptionSetter<T> optionSetter, Supplier<CycleButton.Builder<T>> supplier) {
		super(string);
		this.getter = function;
		this.setter = optionSetter;
		this.buttonSetup = supplier;
	}

	public static <T> CycleOption<T> create(
		String string, List<T> list, Function<T, Component> function, Function<Options, T> function2, CycleOption.OptionSetter<T> optionSetter
	) {
		return new CycleOption<>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues(list));
	}

	public static <T> CycleOption<T> create(
		String string, Supplier<List<T>> supplier, Function<T, Component> function, Function<Options, T> function2, CycleOption.OptionSetter<T> optionSetter
	) {
		return new CycleOption<>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues((List<T>)supplier.get()));
	}

	public static <T> CycleOption<T> create(
		String string,
		List<T> list,
		List<T> list2,
		BooleanSupplier booleanSupplier,
		Function<T, Component> function,
		Function<Options, T> function2,
		CycleOption.OptionSetter<T> optionSetter
	) {
		return new CycleOption<>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues(booleanSupplier, list, list2));
	}

	public static <T> CycleOption<T> create(
		String string, T[] objects, Function<T, Component> function, Function<Options, T> function2, CycleOption.OptionSetter<T> optionSetter
	) {
		return new CycleOption<>(string, function2, optionSetter, () -> CycleButton.builder(function).withValues(objects));
	}

	public static CycleOption<Boolean> createBinaryOption(
		String string, Component component, Component component2, Function<Options, Boolean> function, CycleOption.OptionSetter<Boolean> optionSetter
	) {
		return new CycleOption<>(string, function, optionSetter, () -> CycleButton.booleanBuilder(component, component2));
	}

	public static CycleOption<Boolean> createOnOff(String string, Function<Options, Boolean> function, CycleOption.OptionSetter<Boolean> optionSetter) {
		return new CycleOption<>(string, function, optionSetter, CycleButton::onOffBuilder);
	}

	public static CycleOption<Boolean> createOnOff(
		String string, Component component, Function<Options, Boolean> function, CycleOption.OptionSetter<Boolean> optionSetter
	) {
		return createOnOff(string, function, optionSetter).setTooltip(minecraft -> {
			List<FormattedCharSequence> list = minecraft.font.split(component, 200);
			return boolean_ -> list;
		});
	}

	public CycleOption<T> setTooltip(Function<Minecraft, CycleButton.TooltipSupplier<T>> function) {
		this.tooltip = function;
		return this;
	}

	@Override
	public AbstractWidget createButton(Options options, int i, int j, int k) {
		CycleButton.TooltipSupplier<T> tooltipSupplier = (CycleButton.TooltipSupplier<T>)this.tooltip.apply(Minecraft.getInstance());
		return ((CycleButton.Builder)this.buttonSetup.get())
			.withTooltip(tooltipSupplier)
			.withInitialValue((T)this.getter.apply(options))
			.create(i, j, k, 20, this.getCaption(), (cycleButton, object) -> {
				this.setter.accept(options, this, object);
				options.save();
			});
	}

	@FunctionalInterface
	@Environment(EnvType.CLIENT)
	public interface OptionSetter<T> {
		void accept(Options options, Option option, T object);
	}
}
