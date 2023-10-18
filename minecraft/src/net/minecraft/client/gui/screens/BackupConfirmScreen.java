package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class BackupConfirmScreen extends Screen {
	private final Runnable onCancel;
	protected final BackupConfirmScreen.Listener onProceed;
	private final Component description;
	private final boolean promptForCacheErase;
	private MultiLineLabel message = MultiLineLabel.EMPTY;
	protected int id;
	private Checkbox eraseCache;

	public BackupConfirmScreen(Runnable runnable, BackupConfirmScreen.Listener listener, Component component, Component component2, boolean bl) {
		super(component);
		this.onCancel = runnable;
		this.onProceed = listener;
		this.description = component2;
		this.promptForCacheErase = bl;
	}

	@Override
	protected void init() {
		super.init();
		this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
		int i = (this.message.getLineCount() + 1) * 9;
		this.addRenderableWidget(
			Button.builder(Component.translatable("selectWorld.backupJoinConfirmButton"), button -> this.onProceed.proceed(true, this.eraseCache.selected()))
				.bounds(this.width / 2 - 155, 100 + i, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(Component.translatable("selectWorld.backupJoinSkipButton"), button -> this.onProceed.proceed(false, this.eraseCache.selected()))
				.bounds(this.width / 2 - 155 + 160, 100 + i, 150, 20)
				.build()
		);
		this.addRenderableWidget(
			Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel.run()).bounds(this.width / 2 - 155 + 80, 124 + i, 150, 20).build()
		);
		this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + i, 150, 20, Component.translatable("selectWorld.backupEraseCache"), false);
		if (this.promptForCacheErase) {
			this.addRenderableWidget(this.eraseCache);
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
		this.message.renderCentered(guiGraphics, this.width / 2, 70);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}

	@Override
	public boolean keyPressed(int i, int j, int k) {
		if (i == 256) {
			this.onCancel.run();
			return true;
		} else {
			return super.keyPressed(i, j, k);
		}
	}

	@Environment(EnvType.CLIENT)
	public interface Listener {
		void proceed(boolean bl, boolean bl2);
	}
}
