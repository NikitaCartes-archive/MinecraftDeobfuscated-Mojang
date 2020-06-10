package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TranslatableComponent;

@Environment(EnvType.CLIENT)
public class SafetyScreen extends Screen {
	private final Screen previous;
	private static final Component TITLE = new TranslatableComponent("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
	private static final Component CONTENT = new TranslatableComponent("multiplayerWarning.message");
	private static final Component CHECK = new TranslatableComponent("multiplayerWarning.check");
	private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
	private Checkbox stopShowing;
	private final List<FormattedText> lines = Lists.<FormattedText>newArrayList();

	public SafetyScreen(Screen screen) {
		super(NarratorChatListener.NO_TITLE);
		this.previous = screen;
	}

	@Override
	protected void init() {
		super.init();
		this.lines.clear();
		this.lines.addAll(this.font.split(CONTENT, this.width - 50));
		int i = (this.lines.size() + 1) * 9;
		this.addButton(new Button(this.width / 2 - 155, 100 + i, 150, 20, CommonComponents.GUI_PROCEED, button -> {
			if (this.stopShowing.selected()) {
				this.minecraft.options.skipMultiplayerWarning = true;
				this.minecraft.options.save();
			}

			this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
		}));
		this.addButton(new Button(this.width / 2 - 155 + 160, 100 + i, 150, 20, CommonComponents.GUI_BACK, button -> this.minecraft.setScreen(this.previous)));
		this.stopShowing = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, CHECK, false);
		this.addButton(this.stopShowing);
	}

	@Override
	public String getNarrationMessage() {
		return NARRATION.getString();
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		this.drawCenteredString(poseStack, this.font, TITLE, this.width / 2, 30, 16777215);
		int k = 70;

		for (FormattedText formattedText : this.lines) {
			this.drawCenteredString(poseStack, this.font, formattedText, this.width / 2, k, 16777215);
			k += 9;
		}

		super.render(poseStack, i, j, f);
	}
}
