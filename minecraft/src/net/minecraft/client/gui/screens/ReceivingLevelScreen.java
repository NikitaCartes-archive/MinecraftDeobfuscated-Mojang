package net.minecraft.client.gui.screens;

import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;

@Environment(EnvType.CLIENT)
public class ReceivingLevelScreen extends Screen {
	private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");
	private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 30000L;
	private final long createdAt;
	private final BooleanSupplier levelReceived;
	private final ReceivingLevelScreen.Reason reason;
	@Nullable
	private TextureAtlasSprite cachedNetherPortalSprite;

	public ReceivingLevelScreen(BooleanSupplier booleanSupplier, ReceivingLevelScreen.Reason reason) {
		super(GameNarrator.NO_TITLE);
		this.levelReceived = booleanSupplier;
		this.reason = reason;
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
		switch (this.reason) {
			case NETHER_PORTAL:
				guiGraphics.blit(0, 0, -90, guiGraphics.guiWidth(), guiGraphics.guiHeight(), this.getNetherPortalSprite());
				break;
			case END_PORTAL:
				guiGraphics.fillRenderType(RenderType.endPortal(), 0, 0, this.width, this.height, 0);
				break;
			case OTHER:
				this.renderPanorama(guiGraphics, f);
				this.renderBlurredBackground(f);
				this.renderMenuBackground(guiGraphics);
		}
	}

	private TextureAtlasSprite getNetherPortalSprite() {
		if (this.cachedNetherPortalSprite != null) {
			return this.cachedNetherPortalSprite;
		} else {
			this.cachedNetherPortalSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
			return this.cachedNetherPortalSprite;
		}
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

	@Environment(EnvType.CLIENT)
	public static enum Reason {
		NETHER_PORTAL,
		END_PORTAL,
		OTHER;
	}
}
