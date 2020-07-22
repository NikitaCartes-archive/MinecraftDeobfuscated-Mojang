package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ProgressListener;

@Environment(EnvType.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
	@Nullable
	private Component header;
	@Nullable
	private Component stage;
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
		this.header = component;
		this.progressStage(new TranslatableComponent("progress.working"));
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		if (this.stop) {
			if (!this.minecraft.isConnectedToRealms()) {
				this.minecraft.setScreen(null);
			}
		} else {
			this.renderBackground(poseStack);
			if (this.header != null) {
				drawCenteredString(poseStack, this.font, this.header, this.width / 2, 70, 16777215);
			}

			if (this.stage != null && this.progress != 0) {
				drawCenteredString(poseStack, this.font, new TextComponent("").append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
			}

			super.render(poseStack, i, j, f);
		}
	}
}
