package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.PlayerModelPart;

@Environment(EnvType.CLIENT)
public class SkinCustomizationScreen extends OptionsSubScreen {
	public SkinCustomizationScreen(Screen screen, Options options) {
		super(screen, options, Component.translatable("options.skinCustomisation.title"));
	}

	@Override
	protected void init() {
		int i = 0;

		for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
			this.addRenderableWidget(
				CycleButton.onOffBuilder(this.options.isModelPartEnabled(playerModelPart))
					.create(
						this.width / 2 - 155 + i % 2 * 160,
						this.height / 6 + 24 * (i >> 1),
						150,
						20,
						playerModelPart.getName(),
						(cycleButton, boolean_) -> this.options.toggleModelPart(playerModelPart, boolean_)
					)
			);
			i++;
		}

		this.addRenderableWidget(this.options.mainHand().createButton(this.options, this.width / 2 - 155 + i % 2 * 160, this.height / 6 + 24 * (i >> 1), 150));
		if (++i % 2 == 1) {
			i++;
		}

		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
				.bounds(this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20)
				.build()
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 16777215);
	}
}
