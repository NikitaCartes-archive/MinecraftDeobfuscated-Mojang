package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;

@Environment(EnvType.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
	@Nullable
	private Component header;
	@Nullable
	private Component stage;
	private int progress;
	private boolean stop;
	private final boolean clearScreenAfterStop;

	public ProgressScreen(boolean bl) {
		super(GameNarrator.NO_TITLE);
		this.clearScreenAfterStop = bl;
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	protected boolean shouldNarrateNavigation() {
		return false;
	}

	@Override
	public void progressStartNoAbort(Component component) {
		this.progressStart(component);
	}

	@Override
	public void progressStart(Component component) {
		this.header = component;
		this.progressStage(Component.translatable("menu.working"));
	}

	@Override
	public void progressStage(Component component) {
		this.stage = component;
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		if (this.stop) {
			if (this.clearScreenAfterStop) {
				this.minecraft.setScreen(null);
			}
		} else {
			super.render(guiGraphics, i, j, f);
			if (this.header != null) {
				guiGraphics.drawCenteredString(this.font, this.header, this.width / 2, 70, 16777215);
			}

			if (this.stage != null && this.progress != 0) {
				guiGraphics.drawCenteredString(this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
			}
		}
	}
}
