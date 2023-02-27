package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DatapackLoadFailureScreen extends Screen {
	private MultiLineLabel message = MultiLineLabel.EMPTY;
	private final Runnable callback;

	public DatapackLoadFailureScreen(Runnable runnable) {
		super(Component.translatable("datapackFailure.title"));
		this.callback = runnable;
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.getTitle(), this.width - 50);
		this.addRenderableWidget(
			Button.builder(Component.translatable("datapackFailure.safeMode"), button -> this.callback.run())
				.bounds(this.width / 2 - 155, this.height / 6 + 96, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_TO_TITLE, button -> this.minecraft.setScreen(null))
				.bounds(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20)
				.build()
		);
	}

	@Override
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderBackground(poseStack);
		this.message.renderCentered(poseStack, this.width / 2, 70);
		super.render(poseStack, i, j, f);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
