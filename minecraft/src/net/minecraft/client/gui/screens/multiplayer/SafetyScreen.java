package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class SafetyScreen extends Screen {
	private final Screen previous;
	private final Component title = new TranslatableComponent("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
	private final Component content = new TranslatableComponent("multiplayerWarning.message");
	private final Component check = new TranslatableComponent("multiplayerWarning.check");
	private final Component proceed = new TranslatableComponent("gui.proceed");
	private final Component back = new TranslatableComponent("gui.back");
	private Checkbox stopShowing;
	private final List<String> lines = Lists.<String>newArrayList();

	public SafetyScreen(Screen screen) {
		super(NarratorChatListener.NO_TITLE);
		this.previous = screen;
	}

	@Override
	protected void init() {
		super.init();
		this.lines.clear();
		this.lines.addAll(this.font.split(this.content.getColoredString(), this.width - 50));
		int i = (this.lines.size() + 1) * 9;
		this.addButton(new Button(this.width / 2 - 155, 100 + i, 150, 20, this.proceed.getColoredString(), button -> {
			if (this.stopShowing.selected()) {
				this.minecraft.options.skipMultiplayerWarning = true;
				this.minecraft.options.save();
			}

			this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
		}));
		this.addButton(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, this.back.getColoredString(), button -> this.minecraft.setScreen(this.previous)));
		this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, this.check.getColoredString(), false);
		this.addButton(this.stopShowing);
	}

	@Override
	public String getNarrationMessage() {
		return this.title.getString() + "\n" + this.content.getString();
	}

	@Override
	public void render(int i, int j, float f) {
		this.renderDirtBackground(0);
		this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 30, 16777215);
		int k = 70;

		for (String string : this.lines) {
			this.drawCenteredString(this.font, string, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(i, j, f);
	}
}
