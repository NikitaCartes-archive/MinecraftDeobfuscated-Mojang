package net.minecraft.client.gui.components.tabs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Divisor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class TabNavigationBar extends GridLayout {
	private int width;
	private final TabManager tabManager;
	private final ImmutableList<Tab> tabs;
	private final ImmutableMap<Tab, Button> tabButtons;

	public void setWidth(int i) {
		this.width = i;
	}

	public static TabNavigationBar.Builder builder(TabManager tabManager, int i) {
		return new TabNavigationBar.Builder(tabManager, i);
	}

	TabNavigationBar(int i, int j, int k, TabManager tabManager, Iterable<Tab> iterable) {
		super(i, j);
		this.width = k;
		this.tabManager = tabManager;
		this.tabs = ImmutableList.copyOf(iterable);
		ImmutableMap.Builder<Tab, Button> builder = ImmutableMap.builder();
		int l = 0;

		for (Tab tab : iterable) {
			Button button = Button.builder(tab.getTabTitle(), buttonx -> this.selectTab(Optional.of(buttonx), tab))
				.createNarration(supplier -> Component.translatable("gui.narrate.tab", tab.getTabTitle()))
				.build();
			builder.put(tab, this.addChild(button, 0, l++));
		}

		this.tabButtons = builder.build();
		this.arrangeElements();
	}

	@Override
	public void arrangeElements() {
		Divisor divisor = new Divisor(this.width, this.tabs.size());

		for (Button button : this.tabButtons.values()) {
			button.setWidth(divisor.nextInt());
		}

		super.arrangeElements();
	}

	private void selectTab(Optional<Button> optional, Tab tab) {
		this.tabButtons.values().forEach(button -> button.active = true);
		optional.ifPresent(button -> button.active = false);
		this.tabManager.setCurrentTab(tab);
	}

	public void setInitialTab(Tab tab) {
		this.selectTab(Optional.ofNullable(this.tabButtons.get(tab)), tab);
	}

	public void setInitialTab(int i) {
		this.setInitialTab((Tab)this.tabs.get(i));
	}

	@Environment(EnvType.CLIENT)
	public static class Builder {
		private int x = 0;
		private int y = 0;
		private int width;
		private final TabManager tabManager;
		private final List<Tab> tabs = new ArrayList();

		Builder(TabManager tabManager, int i) {
			this.tabManager = tabManager;
			this.width = i;
		}

		public TabNavigationBar.Builder addTab(Tab tab) {
			this.tabs.add(tab);
			return this;
		}

		public TabNavigationBar.Builder addTabs(Tab... tabs) {
			Collections.addAll(this.tabs, tabs);
			return this;
		}

		public TabNavigationBar.Builder setX(int i) {
			this.x = i;
			return this;
		}

		public TabNavigationBar.Builder setY(int i) {
			this.y = i;
			return this;
		}

		public TabNavigationBar.Builder setPosition(int i, int j) {
			return this.setX(i).setY(j);
		}

		public TabNavigationBar.Builder setWidth(int i) {
			this.width = i;
			return this;
		}

		public TabNavigationBar build() {
			return new TabNavigationBar(this.x, this.y, this.width, this.tabManager, this.tabs);
		}
	}
}
