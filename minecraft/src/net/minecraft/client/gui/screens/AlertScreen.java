package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

@Environment(EnvType.CLIENT)
public class AlertScreen extends Screen {
	private static final int LABEL_Y = 90;
	private final Component messageText;
	private MultiLineLabel message = MultiLineLabel.EMPTY;
	private final Runnable callback;
	private final Component okButton;
	private final boolean shouldCloseOnEsc;

	public AlertScreen(Runnable runnable, Component component, Component component2) {
		this(runnable, component, component2, CommonComponents.GUI_BACK, true);
	}

	public AlertScreen(Runnable runnable, Component component, Component component2, Component component3, boolean bl) {
		super(component);
		this.callback = runnable;
		this.messageText = component2;
		this.okButton = component3;
		this.shouldCloseOnEsc = bl;
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(super.getNarrationMessage(), this.messageText);
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.messageText, this.width - 50);
		int i = this.message.getLineCount() * 9;
		int j = Mth.clamp(90 + i + 12, this.height / 6 + 96, this.height - 24);
		int k = 150;
		this.addRenderableWidget(Button.builder(this.okButton, button -> this.callback.run()).bounds((this.width - 150) / 2, j, 150, 20).build());
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 70, 16777215);
		this.message.renderCentered(poseStack, this.width / 2, 90);
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return this.shouldCloseOnEsc;
	}
}
