package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.OptionButton;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public class SkinCustomizationScreen extends OptionsSubScreen {
	public SkinCustomizationScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.skinCustomisation.title"));
	}

	@Override
	protected void init() {
		int i = 0;

		for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
			this.addButton(new Button(this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150, 20, this.getMessage(playerModelPart), button -> {
				this.options.toggleModelPart(playerModelPart);
				button.setMessage(this.getMessage(playerModelPart));
			}));
			i++;
		}

		this.addButton(
			new OptionButton(
				this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150, 20, Option.MAIN_HAND, Option.MAIN_HAND.getMessage(this.options), button -> {
					Option.MAIN_HAND.toggle(this.options, 1);
					this.options.save();
					button.setMessage(Option.MAIN_HAND.getMessage(this.options));
					this.options.broadcastOptions();
				}
			)
		);
		if (++i % 2 == 1) {
			i++;
		}

		this.addButton(
			new Button(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20, I18n.get("gui.done"), button -> this.minecraft.setScreen(this.lastScreen))
		);
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderBackground();
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 16777215);
		super.render(i, j, f);
	}

	private String getMessage(PlayerModelPart playerModelPart) {
		String string;
		if (this.options.getModelParts().contains(playerModelPart)) {
			string = I18n.get("options.on");
		} else {
			string = I18n.get("options.off");
		}

		return playerModelPart.getName().getColoredString() + ": " + string;
	}
}
