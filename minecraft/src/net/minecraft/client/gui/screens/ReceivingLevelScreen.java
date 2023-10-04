package net.minecraft.client.gui.screens;

import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ReceivingLevelScreen extends Screen {
	private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");
	private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 30000L;
	private final long createdAt;
	private final BooleanSupplier levelReceived;

	public ReceivingLevelScreen(BooleanSupplier booleanSupplier) {
		super(GameNarrator.NO_TITLE);
		this.levelReceived = booleanSupplier;
		this.createdAt = System.currentTimeMillis();
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
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.drawCenteredString(this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 16777215);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		this.renderDirtBackground(guiGraphics);
	}

	@Override
	public void tick() {
		if (this.levelReceived.getAsBoolean() || System.currentTimeMillis() > this.createdAt + 30000L) {
			this.onClose();
		}
	}

	@Override
	public void onClose() {
		this.minecraft.getNarrator().sayNow(Component.translatable("narrator.ready_to_play"));
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
