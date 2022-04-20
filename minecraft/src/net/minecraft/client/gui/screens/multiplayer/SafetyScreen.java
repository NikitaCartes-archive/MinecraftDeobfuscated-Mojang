package net.minecraft.client.gui.screens.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class SafetyScreen extends WarningScreen {
	private static final Component TITLE = Component.translatable("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
	private static final Component CONTENT = Component.translatable("multiplayerWarning.message");
	private static final Component CHECK = Component.translatable("multiplayerWarning.check");
	private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);

	public SafetyScreen(Screen screen) {
		super(TITLE, CONTENT, CHECK, NARRATION, screen);
	}

	@Override
	protected void initButtons(int i) {
		this.addRenderableWidget(new Button(this.width / 2 - 155, 100 + i, 150, 20, CommonComponents.GUI_PROCEED, button -> {
			if (this.stopShowing.selected()) {
				this.minecraft.options.skipMultiplayerWarning = true;
				this.minecraft.options.save();
			}

			this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
		}));
		this.addRenderableWidget(
			new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.previous))
		);
	}
}
