package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Option;
import net.minecraft.client.Options;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class AccessibilityOptionsScreen extends OptionsSubScreen {
	private static final Option[] OPTIONS = new Option[]{
		Option.NARRATOR,
		Option.SHOW_SUBTITLES,
		Option.TEXT_BACKGROUND_OPACITY,
		Option.TEXT_BACKGROUND,
		Option.CHAT_OPACITY,
		Option.CHAT_LINE_SPACING,
		Option.CHAT_DELAY,
		Option.AUTO_JUMP,
		Option.TOGGLE_CROUCH,
		Option.TOGGLE_SPRINT
	};
	private AbstractWidget narratorButton;

	public AccessibilityOptionsScreen(Screen screen, Options options) {
		super(screen, options, new TranslatableComponent("options.accessibility.title"));
	}

	@Override
	protected void init() {
		int i = 0;

		for (Option option : OPTIONS) {
			int j = this.width / 2 - 155 + i % 2 * 160;
			int k = this.height / 6 + 24 * (i >> 1);
			AbstractWidget abstractWidget = this.addButton(option.createButton(this.minecraft.options, j, k, 150));
			if (option == Option.NARRATOR) {
				this.narratorButton = abstractWidget;
				abstractWidget.active = NarratorChatListener.INSTANCE.isActive();
			}

			i++;
		}

		this.addButton(
			new Button(this.width / 2 - 100, this.height / 6 + 144, 200, 20, CommonComponents.GUI_DONE, button -> this.minecraft.setScreen(this.lastScreen))
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.drawCenteredString(poseStack, this.font, this.title, this.width / 2, 20, 16777215);
		super.render(poseStack, i, j, f);
	}

	public void updateNarratorButton() {
		this.narratorButton.setMessage(Option.NARRATOR.getMessage(this.options));
	}
}
