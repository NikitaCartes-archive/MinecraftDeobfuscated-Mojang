package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabButton;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class TabNavigationBar extends AbstractContainerEventHandler implements Renderable, GuiEventListener, NarratableEntry {
	private static final int NO_TAB = -1;
	private static final int MAX_WIDTH = 400;
	private static final int HEIGHT = 24;
	private static final int MARGIN = 14;
	private static final Component USAGE_NARRATION = Component.translatable("narration.tab_navigation.usage");
	private final GridLayout layout;
	private int width;
	private final TabManager tabManager;
	private final ImmutableList<Tab> tabs;
	private final ImmutableList<TabButton> tabButtons;

	TabNavigationBar(int i, TabManager tabManager, Iterable<Tab> iterable) {
		this.width = i;
		this.tabManager = tabManager;
		this.tabs = ImmutableList.copyOf(iterable);
		this.layout = new GridLayout(0, 0);
		this.layout.defaultCellSetting().alignHorizontallyCenter();
		ImmutableList.Builder<TabButton> builder = ImmutableList.builder();
		int j = 0;

		for (Tab tab : iterable) {
			builder.add(this.layout.addChild(new TabButton(tabManager, tab, 0, 24), 0, j++));
		}

		this.tabButtons = builder.build();
	}

	public static TabNavigationBar.Builder builder(TabManager tabManager, int i) {
		return new TabNavigationBar.Builder(tabManager, i);
	}

	public void setWidth(int i) {
		this.width = i;
	}

	@Override
	public void setFocused(boolean bl) {
		super.setFocused(bl);
		if (this.getFocused() != null) {
			this.getFocused().setFocused(bl);
		}
	}

	@Override
	public void setFocused(@Nullable GuiEventListener guiEventListener) {
		super.setFocused(guiEventListener);
		if (guiEventListener instanceof TabButton tabButton) {
			this.tabManager.setCurrentTab(tabButton.tab(), true);
		}
	}

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
		if (!this.isFocused()) {
			TabButton tabButton = this.currentTabButton();
			if (tabButton != null) {
				return ComponentPath.path(this, ComponentPath.leaf(tabButton));
			}
		}

		return focusNavigationEvent instanceof FocusNavigationEvent.TabNavigation ? null : super.nextFocusPath(focusNavigationEvent);
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return this.tabButtons;
	}

	@Override
	public NarratableEntry.NarrationPriority narrationPriority() {
		return (NarratableEntry.NarrationPriority)this.tabButtons
			.stream()
			.map(AbstractWidget::narrationPriority)
			.max(Comparator.naturalOrder())
			.orElse(NarratableEntry.NarrationPriority.NONE);
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		Optional<TabButton> optional = this.tabButtons.stream().filter(AbstractWidget::isHovered).findFirst().or(() -> Optional.ofNullable(this.currentTabButton()));
		optional.ifPresent(tabButton -> {
			this.narrateListElementPosition(narrationElementOutput.nest(), tabButton);
			tabButton.updateNarration(narrationElementOutput);
		});
		if (this.isFocused()) {
			narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
		}
	}

	protected void narrateListElementPosition(NarrationElementOutput narrationElementOutput, TabButton tabButton) {
		if (this.tabs.size() > 1) {
			int i = this.tabButtons.indexOf(tabButton);
			if (i != -1) {
				narrationElementOutput.add(NarratedElementType.POSITION, Component.translatable("narrator.position.tab", i + 1, this.tabs.size()));
			}
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		guiGraphics.fill(0, 0, this.width, 24, -16777216);
		guiGraphics.blit(CreateWorldScreen.HEADER_SEPERATOR, 0, this.layout.getY() + this.layout.getHeight() - 2, 0.0F, 0.0F, this.width, 2, 32, 2);

		for (TabButton tabButton : this.tabButtons) {
			tabButton.render(guiGraphics, i, j, f);
		}
	}

	@Override
	public ScreenRectangle getRectangle() {
		return this.layout.getRectangle();
	}

	public void arrangeElements() {
		int i = Math.min(400, this.width) - 28;
		int j = Mth.roundToward(i / this.tabs.size(), 2);

		for (TabButton tabButton : this.tabButtons) {
			tabButton.setWidth(j);
		}

		this.layout.arrangeElements();
		this.layout.setX(Mth.roundToward((this.width - i) / 2, 2));
		this.layout.setY(0);
	}

	public void selectTab(int i, boolean bl) {
		if (this.isFocused()) {
			this.setFocused((GuiEventListener)this.tabButtons.get(i));
		} else {
			this.tabManager.setCurrentTab((Tab)this.tabs.get(i), bl);
		}
	}

	public boolean keyPressed(int i) {
		if (Screen.hasControlDown()) {
			int j = this.getNextTabIndex(i);
			if (j != -1) {
				this.selectTab(Mth.clamp(j, 0, this.tabs.size() - 1), true);
				return true;
			}
		}

		return false;
	}

	private int getNextTabIndex(int i) {
		if (i >= 49 && i <= 57) {
			return i - 49;
		} else {
			if (i == 258) {
				int j = this.currentTabIndex();
				if (j != -1) {
					int k = Screen.hasShiftDown() ? j - 1 : j + 1;
					return Math.floorMod(k, this.tabs.size());
				}
			}

			return -1;
		}
	}

	private int currentTabIndex() {
		Tab tab = this.tabManager.getCurrentTab();
		int i = this.tabs.indexOf(tab);
		return i != -1 ? i : -1;
	}

	@Nullable
	private TabButton currentTabButton() {
		int i = this.currentTabIndex();
		return i != -1 ? (TabButton)this.tabButtons.get(i) : null;
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private final int width;
		private final TabManager tabManager;
		private final List<Tab> tabs = new ArrayList();

		Builder(TabManager tabManager, int i) {
			this.tabManager = tabManager;
			this.width = i;
		}

		public TabNavigationBar.Builder addTabs(Tab... tabs) {
			Collections.addAll(this.tabs, tabs);
			return this;
		}

		public TabNavigationBar build() {
			return new TabNavigationBar(this.width, this.tabManager, this.tabs);
		}
	}
}
