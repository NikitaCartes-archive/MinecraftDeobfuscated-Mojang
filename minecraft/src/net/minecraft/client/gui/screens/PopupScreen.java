package net.minecraft.client.gui.screens;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.TextComponent;

@Environment(EnvType.CLIENT)
public class PopupScreen extends Screen {
	private static final int BUTTON_PADDING = 20;
	private static final int BUTTON_MARGIN = 5;
	private static final int BUTTON_HEIGHT = 20;
	private final Component narrationMessage;
	private final FormattedText message;
	private final ImmutableList<PopupScreen.ButtonOption> buttonOptions;
	private MultiLineLabel messageLines = MultiLineLabel.EMPTY;
	private int contentTop;
	private int buttonWidth;

	protected PopupScreen(Component component, List<Component> list, ImmutableList<PopupScreen.ButtonOption> immutableList) {
		super(component);
		this.message = FormattedText.composite(list);
		this.narrationMessage = CommonComponents.joinForNarration(component, ComponentUtils.formatList(list, TextComponent.EMPTY));
		this.buttonOptions = immutableList;
	}

	@Override
	public Component getNarrationMessage() {
		return this.narrationMessage;
	}

	@Override
	public void init() {
		for (PopupScreen.ButtonOption buttonOption : this.buttonOptions) {
			this.buttonWidth = Math.max(this.buttonWidth, 20 + this.font.width(buttonOption.message) + 20);
		}

		int i = 5 + this.buttonWidth + 5;
		int j = i * this.buttonOptions.size();
		this.messageLines = MultiLineLabel.create(this.font, this.message, j);
		int k = this.messageLines.getLineCount() * 9;
		this.contentTop = (int)((double)this.height / 2.0 - (double)k / 2.0);
		int l = this.contentTop + k + 9 * 2;
		int m = (int)((double)this.width / 2.0 - (double)j / 2.0);

		for (PopupScreen.ButtonOption buttonOption2 : this.buttonOptions) {
			this.addRenderableWidget(new Button(m, l, this.buttonWidth, 20, buttonOption2.message, buttonOption2.onPress));
			m += i;
		}
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(0);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, this.contentTop - 9 * 2, -1);
		this.messageLines.renderCentered(poseStack, this.width / 2, this.contentTop);
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	public static final class ButtonOption {
		final Component message;
		final Button.OnPress onPress;

		public ButtonOption(Component component, Button.OnPress onPress) {
			this.message = component;
			this.onPress = onPress;
		}
	}
}
