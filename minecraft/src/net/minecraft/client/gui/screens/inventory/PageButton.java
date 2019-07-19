package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvents;

@Environment(EnvType.CLIENT)
public class PageButton extends Button {
	private final boolean isForward;
	private final boolean playTurnSound;

	public PageButton(int i, int j, boolean bl, Button.OnPress onPress, boolean bl2) {
		super(i, j, 23, 13, "", onPress);
		this.isForward = bl;
		this.playTurnSound = bl2;
	}

	@Override
	public void renderButton(int i, int j, float f) {
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getInstance().getTextureManager().bind(BookViewScreen.BOOK_LOCATION);
		int k = 0;
		int l = 192;
		if (this.isHovered()) {
			k += 23;
		}

		if (!this.isForward) {
			l += 13;
		}

		this.blit(this.x, this.y, k, l, 23, 13);
	}

	@Override
	public void playDownSound(SoundManager soundManager) {
		if (this.playTurnSound) {
			soundManager.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
		}
	}
}
