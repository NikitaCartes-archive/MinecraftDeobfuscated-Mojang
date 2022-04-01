package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class LogaritmicProgressOption extends ProgressOption {
	public LogaritmicProgressOption(
		String string,
		double d,
		double e,
		float f,
		Function<Options, Double> function,
		BiConsumer<Options, Double> biConsumer,
		BiFunction<Options, ProgressOption, Component> biFunction
	) {
		super(string, d, e, f, function, biConsumer, biFunction);
	}

	@Override
	public double toPct(double d) {
		return Math.log(d / this.minValue) / Math.log(this.maxValue / this.minValue);
	}

	@Override
	public double toValue(double d) {
		return this.minValue * Math.pow(Math.E, Math.log(this.maxValue / this.minValue) * d);
	}
}
