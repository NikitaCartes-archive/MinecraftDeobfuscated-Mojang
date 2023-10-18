package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class DatapackLoadFailureScreen extends Screen {
	private MultiLineLabel message = MultiLineLabel.EMPTY;
	private final Runnable cancelCallback;
	private final Runnable safeModeCallback;

	public DatapackLoadFailureScreen(Runnable runnable, Runnable runnable2) {
		super(Component.translatable("datapackFailure.title"));
		this.cancelCallback = runnable;
		this.safeModeCallback = runnable2;
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.getTitle(), this.width - 50);
		this.addRenderableWidget(
			Button.builder(Component.translatable("datapackFailure.safeMode"), button -> this.safeModeCallback.run())
				.bounds(this.width / 2 - 155, this.height / 6 + 96, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_BACK, button -> this.cancelCallback.run()).bounds(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20).build()
		);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		this.message.renderCentered(guiGraphics, this.width / 2, 70);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
