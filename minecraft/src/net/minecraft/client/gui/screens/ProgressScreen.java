package net.minecraft.client.gui.screens;

import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ProgressListener;

@Environment(EnvType.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
	private String title = "";
	private String stage = "";
	private int progress;
	private boolean stop;

	public ProgressScreen() {
		super(NarratorChatListener.NO_TITLE);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public void progressStartNoAbort(Component component) {
		this.progressStart(component);
	}

	@Override
	public void progressStart(Component component) {
		this.title = component.getColoredString();
		this.progressStage(new TranslatableComponent("progress.working"));
	}

	@Override
	public void progressStage(Component component) {
		this.stage = component.getColoredString();
		this.progressStagePercentage(0);
	}

	@Override
	public void progressStagePercentage(int i) {
		this.progress = i;
	}

	@Override
	public void stop() {
		this.stop = true;
	}

	@Override
	public void render(int i, int j, float f) {
		if (this.stop) {
			if (!this.minecraft.isConnectedToRealms()) {
				this.minecraft.setScreen(null);
			}
		} else {
			this.renderBackground();
			this.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
			if (!Objects.equals(this.stage, "") && this.progress != 0) {
				this.drawCenteredString(this.font, this.stage + " " + this.progress + "%", this.width / 2, 90, 16777215);
			}

			super.render(i, j, f);
		}
	}
}
