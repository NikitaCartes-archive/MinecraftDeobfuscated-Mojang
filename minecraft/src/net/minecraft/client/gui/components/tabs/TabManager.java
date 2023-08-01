package net.minecraft.client.gui.components.tabs;

import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

@Environment(EnvType.CLIENT)
public class TabManager {
	private final Consumer<AbstractWidget> addWidget;
	private final Consumer<AbstractWidget> removeWidget;
	@Nullable
	private Tab currentTab;
	@Nullable
	private ScreenRectangle tabArea;

	public TabManager(Consumer<AbstractWidget> consumer, Consumer<AbstractWidget> consumer2) {
		this.addWidget = consumer;
		this.removeWidget = consumer2;
	}

	public void setTabArea(ScreenRectangle screenRectangle) {
		this.tabArea = screenRectangle;
		Tab tab = this.getCurrentTab();
		if (tab != null) {
			tab.doLayout(screenRectangle);
		}
	}

	public void setCurrentTab(Tab tab, boolean bl) {
		if (!Objects.equals(this.currentTab, tab)) {
			if (this.currentTab != null) {
				this.currentTab.visitChildren(this.removeWidget);
			}

			this.currentTab = tab;
			tab.visitChildren(this.addWidget);
			if (this.tabArea != null) {
				tab.doLayout(this.tabArea);
			}

			if (bl) {
				Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
		}
	}

	@Nullable
	public Tab getCurrentTab() {
		return this.currentTab;
	}
}
