package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SliderButton;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class ProgressOption extends Option {
	protected final float steps;
	protected final double minValue;
	protected double maxValue;
	private final Function<Options, Double> getter;
	private final BiConsumer<Options, Double> setter;
	private final BiFunction<Options, ProgressOption, Component> toString;
	private final Function<Minecraft, List<FormattedCharSequence>> tooltipSupplier;

	public ProgressOption(
		String string,
		double d,
		double e,
		float f,
		Function<Options, Double> function,
		BiConsumer<Options, Double> biConsumer,
		BiFunction<Options, ProgressOption, Component> biFunction,
		Function<Minecraft, List<FormattedCharSequence>> function2
	) {
		super(string);
		this.minValue = d;
		this.maxValue = e;
		this.steps = f;
		this.getter = function;
		this.setter = biConsumer;
		this.toString = biFunction;
		this.tooltipSupplier = function2;
	}

	public ProgressOption(
		String string,
		double d,
		double e,
		float f,
		Function<Options, Double> function,
		BiConsumer<Options, Double> biConsumer,
		BiFunction<Options, ProgressOption, Component> biFunction
	) {
		this(string, d, e, f, function, biConsumer, biFunction, minecraft -> ImmutableList.of());
	}

	@Override
	public AbstractWidget createButton(Options options, int i, int j, int k) {
		List<FormattedCharSequence> list = (List<FormattedCharSequence>)this.tooltipSupplier.apply(Minecraft.getInstance());
		return new SliderButton(options, i, j, k, 20, this, list);
	}

	public double toPct(double d) {
		return Mth.clamp((this.clamp(d) - this.minValue) / (this.maxValue - this.minValue), 0.0, 1.0);
	}

	public double toValue(double d) {
		return this.clamp(Mth.lerp(Mth.clamp(d, 0.0, 1.0), this.minValue, this.maxValue));
	}

	private double clamp(double d) {
		if (this.steps > 0.0F) {
			d = (double)(this.steps * (float)Math.round(d / (double)this.steps));
		}

		return Mth.clamp(d, this.minValue, this.maxValue);
	}

	public double getMinValue() {
		return this.minValue;
	}

	public double getMaxValue() {
		return this.maxValue;
	}

	public void setMaxValue(float f) {
		this.maxValue = (double)f;
	}

	public void set(Options options, double d) {
		this.setter.accept(options, d);
	}

	public double get(Options options) {
		return (Double)this.getter.apply(options);
	}

	public Component getMessage(Options options) {
		return (Component)this.toString.apply(options, this);
	}
}
