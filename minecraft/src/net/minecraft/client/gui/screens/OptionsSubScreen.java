package net.minecraft.client.gui.screens;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class OptionsSubScreen extends Screen {
	protected final Screen lastScreen;
	protected final Options options;

	public OptionsSubScreen(Screen screen, Options options, Component component) {
		super(component);
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	public void removed() {
		this.minecraft.options.save();
	}

	@Override
	public void onClose() {
		this.minecraft.setScreen(this.lastScreen);
	}

	@Nullable
	public static List<FormattedCharSequence> tooltipAt(OptionsList optionsList, int i, int j) {
		Optional<AbstractWidget> optional = optionsList.getMouseOver((double)i, (double)j);
		if (optional.isPresent() && optional.get() instanceof TooltipAccessor) {
			Optional<List<FormattedCharSequence>> optional2 = ((TooltipAccessor)optional.get()).getTooltip();
			return (List<FormattedCharSequence>)optional2.orElse(null);
		} else {
			return null;
		}
	}
}
