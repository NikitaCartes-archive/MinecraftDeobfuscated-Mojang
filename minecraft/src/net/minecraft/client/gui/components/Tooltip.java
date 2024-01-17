package net.minecraft.client.gui.components;

import java.util.List;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarrationSupplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class Tooltip implements NarrationSupplier {
	private static final int MAX_WIDTH = 170;
	private final Component message;
	@Nullable
	private List<FormattedCharSequence> cachedTooltip;
	@Nullable
	private final Component narration;

	private Tooltip(Component component, @Nullable Component component2) {
		this.message = component;
		this.narration = component2;
	}

	public static Tooltip create(Component component, @Nullable Component component2) {
		return new Tooltip(component, component2);
	}

	public static Tooltip create(Component component) {
		return new Tooltip(component, component);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		if (this.narration != null) {
			narrationElementOutput.add(NarratedElementType.HINT, this.narration);
		}
	}

	public List<FormattedCharSequence> toCharSequence(Minecraft minecraft) {
		if (this.cachedTooltip == null) {
			this.cachedTooltip = splitTooltip(minecraft, this.message);
		}

		return this.cachedTooltip;
	}

	public static List<FormattedCharSequence> splitTooltip(Minecraft minecraft, Component component) {
		return minecraft.font.split(component, 170);
	}
}
