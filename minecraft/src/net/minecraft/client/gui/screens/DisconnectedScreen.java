package net.minecraft.client.gui.screens;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class DisconnectedScreen extends Screen {
	private final Component reason;
	private List<String> lines;
	private final Screen parent;
	private int textHeight;

	public DisconnectedScreen(Screen screen, String string, Component component) {
		super(new TranslatableComponent(string));
		this.parent = screen;
		this.reason = component;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected void init() {
		this.lines = this.font.split(this.reason.getColoredString(), this.width - 50);
		this.textHeight = this.lines.size() * 9;
		this.addButton(
			new Button(
				this.width / 2 - 100,
				Math.min(this.height / 2 + this.textHeight / 2 + 9, this.height - 30),
				200,
				20,
				I18n.get("gui.toMenu"),
				button -> this.minecraft.setScreen(this.parent)
			)
		);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
		int k = this.height / 2 - this.textHeight / 2;
		if (this.lines != null) {
			for (String string : this.lines) {
				this.drawCenteredString(this.font, string, this.width / 2, k, 16777215);
				k += 9;
			}
		}

		super.render(i, j, f);
	}
}
