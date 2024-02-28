package net.minecraft.client.gui.screens.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
	private static final Component TITLE = Component.translatable("controls.title");

	public ControlsScreen(Screen screen, Options options) {
		super(screen, options, TITLE);
	}

	@Override
	protected void init() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.defaultCellSetting().paddingHorizontal(5).paddingBottom(4).alignHorizontallyCenter();
		GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
		rowHelper.addChild(
			Button.builder(Component.translatable("options.mouse_settings"), button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))).build()
		);
		rowHelper.addChild(
			Button.builder(Component.translatable("controls.keybinds"), button -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options))).build()
		);
		rowHelper.addChild(this.options.toggleCrouch().createButton(this.options));
		rowHelper.addChild(this.options.toggleSprint().createButton(this.options));
		rowHelper.addChild(this.options.autoJump().createButton(this.options));
		rowHelper.addChild(this.options.operatorItemsTab().createButton(this.options));
		this.layout.addToContents(gridLayout);
		super.init();
	}
}
