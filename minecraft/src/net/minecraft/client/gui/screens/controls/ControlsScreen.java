package net.minecraft.client.gui.screens.controls;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionsList;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("controls.title");
	@Nullable
	private OptionsList list;

	private static OptionInstance<?>[] options(Options options) {
		return new OptionInstance[]{options.toggleCrouch(), options.toggleSprint(), options.autoJump(), options.operatorItemsTab()};
	}

	public ControlsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		this.list = this.addRenderableWidget(new OptionsList(this.minecraft, this.width, this.height, this));
		this.list
			.addSmall(
				Button.builder(Component.translatable("options.mouse_settings"), button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))).build(),
				Button.builder(Component.translatable("controls.keybinds"), button -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options))).build()
			);
		this.list.addSmall(options(this.options));
		super.init();
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		if (this.list != null) {
			this.list.updateSize(this.width, this.layout);
		}
	}
}
