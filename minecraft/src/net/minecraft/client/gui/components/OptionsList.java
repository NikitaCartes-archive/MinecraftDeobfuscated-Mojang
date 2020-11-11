package net.minecraft.client.gui.components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.events.GuiEventListener;

@Environment(EnvType.CLIENT)
public class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
	public OptionsList(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
		this.centerListVertically = false;
	}

	public int addBig(Option option) {
		return this.addEntry(OptionsList.Entry.big(this.minecraft.options, this.width, option));
	}

	public void addSmall(Option option, @Nullable Option option2) {
		this.addEntry(OptionsList.Entry.small(this.minecraft.options, this.width, option, option2));
	}

	public void addSmall(Option[] options) {
		for (int i = 0; i < options.length; i += 2) {
			this.addSmall(options[i], i < options.length - 1 ? options[i + 1] : null);
		}
	}

	@Override
	public int getRowWidth() {
		return 400;
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 32;
	}

	@Nullable
	public AbstractWidget findOption(Option option) {
		for (OptionsList.Entry entry : this.children()) {
			AbstractWidget abstractWidget = (AbstractWidget)entry.options.get(option);
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
	public static class Entry extends ContainerObjectSelectionList.Entry<OptionsList.Entry> {
		private final Map<Option, AbstractWidget> options;
		private final List<AbstractWidget> children;

		private Entry(Map<Option, AbstractWidget> map) {
			this.options = map;
			this.children = ImmutableList.copyOf(map.values());
		}

		public static OptionsList.Entry big(Options options, int i, Option option) {
			return new OptionsList.Entry(ImmutableMap.of(option, option.createButton(options, i / 2 - 155, 0, 310)));
		}

		public static OptionsList.Entry small(Options options, int i, Option option, @Nullable Option option2) {
			AbstractWidget abstractWidget = option.createButton(options, i / 2 - 155, 0, 150);
			return option2 == null
				? new OptionsList.Entry(ImmutableMap.of(option, abstractWidget))
				: new OptionsList.Entry(ImmutableMap.of(option, abstractWidget, option2, option2.createButton(options, i / 2 - 155 + 160, 0, 150)));
		}

		@Override
		public void render(PoseStack poseStack, int i, int j, int k, int l, int m, int n, int o, boolean bl, float f) {
			this.children.forEach(abstractWidget -> {
				abstractWidget.y = j;
				abstractWidget.render(poseStack, n, o, f);
			});
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return this.children;
		}
	}
}
