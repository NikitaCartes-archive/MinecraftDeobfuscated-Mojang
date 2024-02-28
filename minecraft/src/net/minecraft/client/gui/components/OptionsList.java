package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.OptionsSubScreen;

@Environment(EnvType.CLIENT)
public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
	private static final int BIG_BUTTON_WIDTH = 310;
	private static final int DEFAULT_ITEM_HEIGHT = 25;
	private final OptionsSubScreen screen;

	public OptionsList(Minecraft minecraft, int i, int j, OptionsSubScreen optionsSubScreen) {
		super(minecraft, i, optionsSubScreen.layout.getContentHeight(), optionsSubScreen.layout.getHeaderHeight(), 25);
		this.centerListVertically = false;
		this.screen = optionsSubScreen;
	}

	public void addBig(OptionInstance<?> optionInstance) {
		this.addEntry(OptionsList.Entry.big(this.minecraft.options, optionInstance, this.screen));
	}

	public void addSmall(OptionInstance<?> optionInstance, @Nullable OptionInstance<?> optionInstance2) {
		this.addEntry(OptionsList.Entry.small(this.minecraft.options, optionInstance, optionInstance2, this.screen));
	}

	public void addSmall(OptionInstance<?>[] optionInstances) {
		for (int i = 0; i < optionInstances.length; i += 2) {
			this.addSmall(optionInstances[i], i < optionInstances.length - 1 ? optionInstances[i + 1] : null);
		}
	}

	@Override
	public int getRowWidth() {
		return 310;
	}

	@Nullable
	public AbstractWidget findOption(OptionInstance<?> optionInstance) {
		for (OptionsList.Entry entry : this.children()) {
			AbstractWidget abstractWidget = (AbstractWidget)entry.options.get(optionInstance);
			if (abstractWidget != null) {
				return abstractWidget;
			}
		}

		return null;
	}

	public Optional<AbstractWidget> getMouseOver(double d, double e) {
		for (OptionsList.Entry entry : this.children()) {
			for (AbstractWidget abstractWidget : entry.children) {
				if (abstractWidget.isMouseOver(d, e)) {
					return Optional.of(abstractWidget);
				}
			}
		}

		return Optional.empty();
	}

	@Environment(EnvType.CLIENT)
	protected static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
		final Map<OptionInstance<?>, AbstractWidget> options;
		final List<AbstractWidget> children;
		private static final int OPTION_X_OFFSET = 160;
		private final OptionsSubScreen screen;

		private Entry(Map<OptionInstance<?>, AbstractWidget> map, OptionsSubScreen optionsSubScreen) {
			this.options = map;
			this.children = ImmutableList.copyOf(map.values());
			this.screen = optionsSubScreen;
		}

		public static OptionsList.Entry big(Options options, OptionInstance<?> optionInstance, OptionsSubScreen optionsSubScreen) {
			return new OptionsList.Entry(ImmutableMap.of(optionInstance, optionInstance.createButton(options, 0, 0, 310)), optionsSubScreen);
		}

		public static OptionsList.Entry small(
			Options options, OptionInstance<?> optionInstance, @Nullable OptionInstance<?> optionInstance2, OptionsSubScreen optionsSubScreen
		) {
			AbstractWidget abstractWidget = optionInstance.createButton(options);
			return optionInstance2 == null
				? new OptionsList.Entry(ImmutableMap.of(optionInstance, abstractWidget), optionsSubScreen)
				: new OptionsList.Entry(ImmutableMap.of(optionInstance, abstractWidget, optionInstance2, optionInstance2.createButton(options)), optionsSubScreen);
		}

		@Override
		public void render(GuiGraphics guiGraphics, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			int p = 0;
			int q = this.screen.width / 2 - 155;

			for (AbstractWidget abstractWidget : this.children) {
				abstractWidget.setPosition(q + p, j);
				abstractWidget.render(guiGraphics, n, o, f);
				p += 160;
			}
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.children;
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return this.children;
		}
	}
}
