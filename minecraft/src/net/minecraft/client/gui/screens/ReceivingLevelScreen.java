package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public class ReceivingLevelScreen extends Screen {
	private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");
	private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 30000L;
	private boolean loadingPacketsReceived = false;
	private boolean oneTickSkipped = false;
	private final long createdAt = System.currentTimeMillis();

	public ReceivingLevelScreen() {
		super(GameNarrator.NO_TITLE);
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
	public void render(PoseStack poseStack, int i, int j, float f) {
		this.renderDirtBackground(poseStack);
		drawCenteredString(poseStack, this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 16777215);
		super.render(poseStack, i, j, f);
	}

	@Override
	public void tick() {
		if (System.currentTimeMillis() > this.createdAt + 30000L) {
			this.onClose();
		} else {
			if (this.oneTickSkipped) {
				if (this.minecraft.player == null) {
					return;
				}

				BlockPos blockPos = this.minecraft.player.blockPosition();
				boolean bl = this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(blockPos.getY());
				if (bl || this.minecraft.levelRenderer.isChunkCompiled(blockPos) || this.minecraft.player.isSpectator() || !this.minecraft.player.isAlive()) {
					this.onClose();
				}
			} else {
				this.oneTickSkipped = this.loadingPacketsReceived;
			}
		}
	}

	@Override
	public void onClose() {
		this.minecraft.getNarrator().sayNow(Component.translatable("narrator.ready_to_play"));
		super.onClose();
	}

	public void loadingPacketsReceived() {
		this.loadingPacketsReceived = true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
