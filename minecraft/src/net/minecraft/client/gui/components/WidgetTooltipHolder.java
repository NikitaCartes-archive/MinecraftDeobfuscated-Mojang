package net.minecraft.client.gui.components;

import java.time.Duration;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.MenuTooltipPositioner;

@Environment(EnvType.CLIENT)
public class WidgetTooltipHolder {
	@Nullable
	private Tooltip tooltip;
	private Duration delay = Duration.ZERO;
	private long displayStartTime;
	private boolean wasDisplayed;

	public void setDelay(Duration duration) {
		this.delay = duration;
	}

	public void set(@Nullable Tooltip tooltip) {
		this.tooltip = tooltip;
	}

	@Nullable
	public Tooltip get() {
		return this.tooltip;
	}

	public void refreshTooltipForNextRenderPass(boolean bl, boolean bl2, ScreenRectangle screenRectangle) {
		if (this.tooltip == null) {
			this.wasDisplayed = false;
		} else {
			boolean bl3 = bl || bl2 && Minecraft.getInstance().getLastInputType().isKeyboard();
			if (bl3 != this.wasDisplayed) {
				if (bl3) {
					this.displayStartTime = Util.getMillis();
				}

				this.wasDisplayed = bl3;
			}

			if (bl3 && Util.getMillis() - this.displayStartTime > this.delay.toMillis()) {
				Screen screen = Minecraft.getInstance().screen;
				if (screen != null) {
					screen.setTooltipForNextRenderPass(this.tooltip, this.createTooltipPositioner(screenRectangle, bl, bl2), bl2);
				}
			}
		}
	}

	private ClientTooltipPositioner createTooltipPositioner(ScreenRectangle screenRectangle, boolean bl, boolean bl2) {
		return (ClientTooltipPositioner)(!bl && bl2 && Minecraft.getInstance().getLastInputType().isKeyboard()
			? new BelowOrAboveWidgetTooltipPositioner(screenRectangle)
			: new MenuTooltipPositioner(screenRectangle));
	}

	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		if (this.tooltip != null) {
			this.tooltip.updateNarration(narrationElementOutput);
		}
	}
}
