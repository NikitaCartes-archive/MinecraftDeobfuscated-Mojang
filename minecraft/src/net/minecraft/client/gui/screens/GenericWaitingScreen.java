package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class GenericWaitingScreen extends Screen {
	private static final int TITLE_Y = 80;
	private static final int MESSAGE_Y = 120;
	private static final int MESSAGE_MAX_WIDTH = 360;
	private final Component initialButtonLabel;
	private Runnable buttonCallback;
	@Nullable
	private MultiLineLabel message;
	private Button button;
	private long disableButtonUntil;

	public GenericWaitingScreen(Component component, Component component2, Runnable runnable) {
		super(component);
		this.initialButtonLabel = component2;
		this.buttonCallback = runnable;
	}

	@Override
	protected void init() {
		super.init();
		this.initButton(this.initialButtonLabel);
	}

	@Override
	public void tick() {
		this.button.active = Util.getMillis() > this.disableButtonUntil;
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
		return false;
	}

	public void update(Component component, Runnable runnable) {
		this.update(null, component, runnable);
	}

	public void update(@Nullable Component component, Component component2, Runnable runnable) {
		this.buttonCallback = runnable;
		if (component != null) {
			this.message = MultiLineLabel.create(this.font, component, 360);
			NarratorChatListener.INSTANCE.sayNow(component);
		} else {
			this.message = null;
		}

		this.initButton(component2);
		this.disableButtonUntil = Util.getMillis() + TimeUnit.SECONDS.toMillis(1L);
	}

	private void initButton(Component component) {
		this.removeWidget(this.button);
		int i = 150;
		int j = 20;
		int k = this.message != null ? this.message.getLineCount() : 1;
		int l = Math.min(120 + (k + 4) * 9, this.height - 40);
		this.button = this.addRenderableWidget(new Button((this.width - 150) / 2, l, 150, 20, component, button -> this.buttonCallback.run()));
	}
}
