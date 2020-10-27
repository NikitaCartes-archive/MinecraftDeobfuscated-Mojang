package net.minecraft.client;

import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BooleanOption extends Option {
	private final Predicate<Options> getter;
	private final BiConsumer<Options, Boolean> setter;
	@Nullable
	private final Component tooltipText;

	public BooleanOption(String string, Predicate<Options> predicate, BiConsumer<Options, Boolean> biConsumer) {
		this(string, null, predicate, biConsumer);
	}

	public BooleanOption(String string, @Nullable Component component, Predicate<Options> predicate, BiConsumer<Options, Boolean> biConsumer) {
		super(string);
		this.getter = predicate;
		this.setter = biConsumer;
		this.tooltipText = component;
	}

	public void set(Options options, String string) {
		this.set(options, "true".equals(string));
	}

	public void toggle(Options options) {
		this.set(options, !this.get(options));
		options.save();
	}

	private void set(Options options, boolean bl) {
		this.setter.accept(options, bl);
	}

	public boolean get(Options options) {
		return this.getter.test(options);
	}

	@Override
	public AbstractWidget createButton(Options options, int i, int j, int k) {
		if (this.tooltipText != null) {
			this.setTooltip(Minecraft.getInstance().font.split(this.tooltipText, 200));
		}

		return new OptionButton(i, j, k, 20, this, this.getMessage(options), button -> {
			this.toggle(options);
			button.setMessage(this.getMessage(options));
		});
	}

	public Component getMessage(Options options) {
		return CommonComponents.optionStatus(this.getCaption(), this.get(options));
	}
}
