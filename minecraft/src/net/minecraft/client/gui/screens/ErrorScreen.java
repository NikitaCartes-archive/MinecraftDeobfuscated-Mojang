package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ErrorScreen extends Screen {
	private final String message;

	public ErrorScreen(Component component, String string) {
		super(component);
		this.message = string;
	}

	@Override
	protected void init() {
		super.init();
		this.addButton(new Button(this.width / 2 - 100, 140, 200, 20, I18n.get("gui.cancel"), button -> this.minecraft.setScreen(null)));
	}

	@Override
	public void render(int i, int j, float f) {
		this.fillGradient(0, 0, this.width, this.height, -12574688, -11530224);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 90, 16777215);
		this.drawCenteredString(this.font, this.message, this.width / 2, 110, 16777215);
		super.render(i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
