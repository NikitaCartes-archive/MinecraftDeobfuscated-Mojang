package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionButton;

@Environment(EnvType.CLIENT)
public class CycleOption extends Option {
	private final BiConsumer<Options, Integer> setter;
	private final BiFunction<Options, CycleOption, String> toString;

	public CycleOption(String string, BiConsumer<Options, Integer> biConsumer, BiFunction<Options, CycleOption, String> biFunction) {
		super(string);
		this.setter = biConsumer;
		this.toString = biFunction;
	}

	public void toggle(Options options, int i) {
		this.setter.accept(options, i);
		options.save();
	}

	@Override
	public AbstractWidget createButton(Options options, int i, int j, int k) {
		return new OptionButton(i, j, k, 20, this, this.getMessage(options), button -> {
			this.toggle(options, 1);
			button.setMessage(this.getMessage(options));
		});
	}

	public String getMessage(Options options) {
		return (String)this.toString.apply(options, this);
	}
}
