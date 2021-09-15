package net.minecraft.client.gui.screens.controls;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.MouseSettingsScreen;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class ControlsScreen extends OptionsSubScreen {
	public ControlsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("controls.title"));
	}

	@Override
	protected void init() {
		super.init();
		int i = this.width / 2 - 155;
		int j = i + 160;
		int k = this.height / 6 - 12;
		this.addRenderableWidget(
			new Button(
				i, k, 150, 20, new TranslatableComponent("options.mouse_settings"), button -> this.minecraft.setScreen(new MouseSettingsScreen(this, this.options))
			)
		);
		this.addRenderableWidget(
			new Button(j, k, 150, 20, new TranslatableComponent("controls.keybinds"), button -> this.minecraft.setScreen(new KeyBindsScreen(this, this.options)))
		);
		k += 24;
		this.addRenderableWidget(Option.TOGGLE_CROUCH.createButton(this.options, i, k, 150));
		this.addRenderableWidget(Option.TOGGLE_SPRINT.createButton(this.options, j, k, 150));
		k += 24;
		this.addRenderableWidget(Option.AUTO_JUMP.createButton(this.options, i, k, 150));
		k += 24;
		this.addRenderableWidget(
			new Button(this.width / 2 - 100, this.height - 29, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 15, 16777215);
		super.render(poseStack, i, j, f);
	}
}
