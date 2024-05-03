package net.minecraft.client.gui.screens.options;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public abstract class OptionsSubScreen extends Screen {
	protected final Screen lastScreen;
	protected final Options options;
	@Nullable
	protected OptionsList list;
	public final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

	public OptionsSubScreen(Screen screen, Options options, Component component) {
		super(component);
		this.lastScreen = screen;
		this.options = options;
	}

	@Override
	protected void init() {
		this.addTitle();
		this.addContents();
		this.addFooter();
		this.layout.visitWidgets(guiEventListener -> {
			AbstractWidget var10000 = this.addRenderableWidget(guiEventListener);
		});
		this.repositionElements();
	}

	protected void addTitle() {
		this.layout.addTitleHeader(this.title, this.font);
	}

	protected void addContents() {
		this.list = this.layout.addToContents(new OptionsList(this.minecraft, this.width, this));
		this.addOptions();
	}

	protected abstract void addOptions();

	protected void addFooter() {
		this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
	}

	@Override
	protected void repositionElements() {
		this.layout.arrangeElements();
		if (this.list != null) {
			this.list.updateSize(this.width, this.layout);
		}
	}

	@Override
	public void removed() {
		this.minecraft.options.save();
	}

	@Override
	public void onClose() {
		if (this.list != null) {
			this.list.applyUnsavedChanges();
		}

		this.minecraft.setScreen(this.lastScreen);
	}
}
