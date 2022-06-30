package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GenericWaitingScreen extends Screen {
	private static final int TITLE_Y = 80;
	private static final int MESSAGE_Y = 120;
	private static final int MESSAGE_MAX_WIDTH = 360;
	@Nullable
	private final Component messageText;
	private final Component buttonLabel;
	private final Runnable buttonCallback;
	@Nullable
	private MultiLineLabel message;
	private Button button;
	private int disableButtonTicks;

	public static GenericWaitingScreen createWaiting(Component component, Component component2, Runnable runnable) {
		return new GenericWaitingScreen(component, null, component2, runnable, 0);
	}

	public static GenericWaitingScreen createCompleted(Component component, Component component2, Component component3, Runnable runnable) {
		return new GenericWaitingScreen(component, component2, component3, runnable, 20);
	}

	protected GenericWaitingScreen(Component component, @Nullable Component component2, Component component3, Runnable runnable, int i) {
		super(component);
		this.messageText = component2;
		this.buttonLabel = component3;
		this.buttonCallback = runnable;
		this.disableButtonTicks = i;
	}

	@Override
	protected void init() {
		super.init();
		if (this.messageText != null) {
			this.message = MultiLineLabel.create(this.font, this.messageText, 360);
		}

		int i = 150;
		int j = 20;
		int k = this.message != null ? this.message.getLineCount() : 1;
		int l = Math.max(k, 5) * 9;
		int m = Math.min(120 + l, this.height - 40);
		this.button = this.addRenderableWidget(new Button((this.width - 150) / 2, m, 150, 20, this.buttonLabel, button -> this.onClose()));
	}

	@Override
	public void tick() {
		if (this.disableButtonTicks > 0) {
			this.disableButtonTicks--;
		}

		this.button.active = this.disableButtonTicks == 0;
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		drawCenteredString(poseStack, this.font, this.title, this.width / 2, 80, 16777215);
		if (this.message == null) {
			String string = LoadingDotsText.get(Util.getMillis());
			drawCenteredString(poseStack, this.font, string, this.width / 2, 120, 10526880);
		} else {
			this.message.renderCentered(poseStack, this.width / 2, 120);
		}

		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return this.message != null && this.button.active;
	}

	@Override
	public void onClose() {
		this.buttonCallback.run();
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(this.title, this.messageText != null ? this.messageText : CommonComponents.EMPTY);
	}
}
