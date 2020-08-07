package net.minecraft.client.gui.components;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class OptionButton extends Button implements TooltipAccessor {
	private final Option option;

	public OptionButton(int i, int j, int k, int l, Option option, Component component, Button.OnPress onPress) {
		super(i, j, k, l, component, onPress);
		this.option = option;
	}

	public Option getOption() {
		return this.option;
	}

	@Override
	public Optional<List<FormattedCharSequence>> getTooltip() {
		return this.option.getTooltip();
	}
}
