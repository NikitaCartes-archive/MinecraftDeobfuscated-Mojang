package net.minecraft.client.gui.components;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.ProgressOption;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class SliderButton extends AbstractOptionSliderButton implements TooltipAccessor {
	private final ProgressOption option;

	public SliderButton(Options options, int i, int j, int k, int l, ProgressOption progressOption) {
		super(options, i, j, k, l, (double)((float)progressOption.toPct(progressOption.get(options))));
		this.option = progressOption;
		this.updateMessage();
	}

	@Override
	protected void applyValue() {
		this.option.set(this.options, this.option.toValue(this.value));
		this.options.save();
	}

	@Override
	protected void updateMessage() {
		this.setMessage(this.option.getMessage(this.options));
	}

	@Override
	public Optional<List<FormattedCharSequence>> getTooltip() {
		return this.option.getTooltip();
	}
}
