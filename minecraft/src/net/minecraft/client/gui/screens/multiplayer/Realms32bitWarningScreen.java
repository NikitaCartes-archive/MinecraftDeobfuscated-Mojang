package net.minecraft.client.gui.screens.multiplayer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class Realms32bitWarningScreen extends WarningScreen {
	private static final Component TITLE = Component.translatable("title.32bit.deprecation.realms.header").withStyle(ChatFormatting.BOLD);
	private static final Component CONTENT = Component.translatable("title.32bit.deprecation.realms");
	private static final Component CHECK = Component.translatable("title.32bit.deprecation.realms.check");
	private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
	private final Screen previous;

	public Realms32bitWarningScreen(Screen screen) {
		super(TITLE, CONTENT, CHECK, NARRATION);
		this.previous = screen;
	}

	@Override
	protected void initButtons(int i) {
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> {
			if (this.stopShowing.selected()) {
				this.minecraft.options.skipRealms32bitWarning = true;
				this.minecraft.options.save();
			}

			this.minecraft.setScreen(this.previous);
		}).bounds(this.width / 2 - 75, 100 + i, 150, 20).build());
	}
}
