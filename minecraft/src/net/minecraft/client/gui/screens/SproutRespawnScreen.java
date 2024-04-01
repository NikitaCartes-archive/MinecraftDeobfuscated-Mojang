package net.minecraft.client.gui.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.sounds.SoundEvents;

@Environment(EnvType.CLIENT)
public class SproutRespawnScreen extends Screen {
	private static final Component TITLE = Component.literal("potato");
	private int transparency;
	private final int delay = 20;
	private int counter;

	public SproutRespawnScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		if (this.minecraft.player != null) {
			this.minecraft.player.makeSound(SoundEvents.PLAYER_SPROUT_RESPAWN_1);
		}
	}

	@Override
	public void tick() {
		this.counter++;
		if (this.counter >= 20) {
			this.onClose();
		}
	}

	@Override
	public void render(GuiGraphics guiGraphics, int i, int j, float f) {
		super.render(guiGraphics, i, j, f);
		guiGraphics.fill(0, 0, this.width, this.height, -16777216);
		guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public void renderBackground(GuiGraphics guiGraphics, int i, int j, float f) {
		super.renderBackground(guiGraphics, i, j, f);
	}

	@Override
	public void onClose() {
		this.sproutRespawn();
	}

	private void sproutRespawn() {
		this.minecraft.setScreen(null);
		if (this.minecraft.player != null) {
			this.minecraft.player.connection.send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.SPROUT_RESPAWN));
		}
	}
}
